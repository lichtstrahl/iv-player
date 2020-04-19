package root.iv.ivplayer.game.room;

import android.view.MotionEvent;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Consumer;

import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.BlockState;
import root.iv.ivplayer.game.tictac.TicTacEngine;
import root.iv.ivplayer.game.tictac.TicTacToeScene;
import root.iv.ivplayer.network.ws.pubnub.callback.PNHereNowCallback;
import root.iv.ivplayer.network.ws.pubnub.dto.TicTacDTO;
import root.iv.ivplayer.network.ws.pubnub.dto.TicTacDTOType;
import root.iv.ivplayer.network.ws.pubnub.dto.TicTacProgressDTO;
import root.iv.ivplayer.network.ws.pubnub.dto.TicTacWinDTO;
import root.iv.ivplayer.service.ChatServiceConnection;
import root.iv.ivplayer.ui.activity.MainActivity;
import timber.log.Timber;

// Комната для дуэли. Является комнатой и реализует действия для слежения за количеством
public class DuelRoom extends Room implements PlayerRoom {
    private Scene scene;
    private ChatServiceConnection serviceConnection;
    private TicTacEngine engine;
    private GsonBuilder gsonBuilder;
    @Nullable
    private Consumer<Boolean> changeStatusListener;
    @Nullable
    private Consumer<String> winListener;


    public DuelRoom(ChatServiceConnection serviceConnection, TicTacTextures textures) {
        super(2);
        this.serviceConnection = serviceConnection;

        this.engine = new TicTacEngine();
        serviceConnection.hereNow(new PNHereNowCallback(this::hereNowProcess, Timber::e), MainActivity.CHANNEL_NAME);

        this.scene = new TicTacToeScene(textures, engine);
        scene.getMainController().setTouchHandler(this::touchHandler);

        this.gsonBuilder = new GsonBuilder();
    }

    @Override
    public void joinPlayer(String uuid) {
        if (currentPlayers < maxPlayers) {
            currentPlayers++;

            // Если игроков достаточно, пробуем перейти в состояние "GAME"
            if (currentPlayers >= minPlauers && currentPlayers <= maxPlayers) {
                changeState(RoomState.GAME);
            }
        }
    }

    // Первый вошедший играет крестиками
    // Если игрок вошел не первым в комнату, то отыгрываем событие "второй игрок" подключился
    // Ведь он уже здесь
    private void hereNowProcess(PNHereNowResult result, PNStatus status) {
        PNHereNowChannelData channelData = Objects.requireNonNull(
                result.getChannels().get(MainActivity.CHANNEL_NAME)
        );

        if (channelData.getOccupants().isEmpty()) {
            engine.setCurrentState(BlockState.CROSS);
            Timber.i("Вход в пустую комнату");
        } else {
            String uuid = channelData.getOccupants().get(0).getUuid();
            engine.setCurrentState(BlockState.CIRCLE);
            joinPlayer(uuid);
            Timber.i("В комнате уже %s", uuid);
        }

    }

    @Override
    public void leavePlayer(String uuid) {
        if (currentPlayers > 0) {
            currentPlayers--;

            // Если вышел важный для игры игрок, то пробуем перейти в состояние"закрыта"
            if (currentPlayers < minPlauers) {
                changeState(RoomState.CLOSE);
            }
        }
    }

    @Override
    public void receiveMsg(PNMessageResult msg) {
        String body = msg.getMessage().getAsString();
        Type type = new TypeToken<TicTacDTO>(){}.getType();
        TicTacDTO dto = gsonBuilder.create().fromJson(body, type);

        switch (dto.getType()) {
            case PROGRESS:
                type = new TypeToken<TicTacDTO<TicTacProgressDTO>>(){}.getType();
                TicTacDTO<TicTacProgressDTO> progress = gsonBuilder.create().fromJson(body, type);
                log("receive:", progress.getData());
                engine.markBlock(progress.getData().getBlockIndex(), progress.getData().getState());
                break;

            case WIN:
                type = new TypeToken<TicTacDTO<TicTacWinDTO>>(){}.getType();
                TicTacDTO<TicTacWinDTO> win = gsonBuilder.create().fromJson(body, type);
                Timber.i("Игрок %s выиграл: %s",
                        win.getData().getUuid(), String.valueOf(win.getData().isWin()));
                changeState(RoomState.CLOSE);
                win(win.getData().getUuid());
                break;
        }
    }

    public void addChangeStatusListener(Consumer<Boolean> listener) {
        this.changeStatusListener = listener;
    }

    public void addWinListener(Consumer<String> winListener) {
        this.winListener = winListener;
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    private void changeState(RoomState newState) {
            boolean transit = RoomStateJump.of(this.state).possibleTransit(newState);
            if (transit) {
                this.state = newState;
                if (changeStatusListener != null) {
                    changeStatusListener.accept(this.state == RoomState.GAME);
                }
            }
            else
                Timber.w("Переход в состояние %s невозможен", newState.name());
    }

    private void touchHandler(MotionEvent event) {
        // Взаимодействие с игрой возможно только при статусе GAME
        if (state != RoomState.GAME) return;

        String selfUUID = serviceConnection.getSelfUUID();

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int oldHistorySize = engine.getHistorySize();
                engine.touchUp(event.getX(), event.getY());
                if (engine.getHistorySize() > oldHistorySize) {
                    TicTacProgressDTO progress = engine.getLastState();
                    progress.setUuid(selfUUID);
                    log("send:", progress);
                    TicTacDTO<TicTacProgressDTO> message = new TicTacDTO<>(
                            TicTacDTOType.PROGRESS, progress
                    );
                    String jsonState = gsonBuilder.create().toJson(message);
                    serviceConnection
                            .publishMessageToChannel(jsonState, MainActivity.CHANNEL_NAME, null);

                    if (engine.win()) {
                        TicTacDTO<TicTacWinDTO> winMessage = new TicTacDTO<>(
                                TicTacDTOType.WIN, new TicTacWinDTO(selfUUID, true)
                        );
                        String jsonWin = gsonBuilder.create().toJson(winMessage);
                        serviceConnection
                                .publishMessageToChannel(jsonWin, MainActivity.CHANNEL_NAME, null);
                        changeState(RoomState.CLOSE);
                        win(selfUUID);
                    }
                }
                break;
        }
    }

    private void win(String uuid) {
        if (winListener != null) winListener.accept(uuid);
    }

    private void log(String prefix, TicTacProgressDTO progress) {
        Timber.i("%s Ход %s: %d %s", prefix, progress.getUuid(), progress.getBlockIndex(), progress.getState().name());
    }
}
