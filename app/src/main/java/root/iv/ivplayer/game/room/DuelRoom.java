package root.iv.ivplayer.game.room;

import android.view.MotionEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import java.lang.reflect.Type;
import java.util.Objects;

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
            if (currentPlayers > minPlauers && currentPlayers < maxPlayers) {
                changeState(RoomState.GAME);
            }
        }
    }

    // Первый вошедший играет крестиками
    private void hereNowProcess(PNHereNowResult result, PNStatus status) {
        PNHereNowChannelData channelData = Objects.requireNonNull(
                result.getChannels().get(MainActivity.CHANNEL_NAME)
        );

        engine.setCurrentState(channelData.getOccupants().isEmpty()
                ? BlockState.CROSS
                : BlockState.CIRCLE
        );
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
                break;
        }


    }

    @Override
    public Scene getScene() {
        return scene;
    }

    private void changeState(RoomState newState) {

            boolean transit = RoomStateJump.of(this.state).possibleTransit(newState);
            if (transit)
                this.state = RoomState.GAME;
            else
                Timber.w("Переход в состояние GAME невозможен");
    }

    private void touchHandler(MotionEvent event) {
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
                    }
                }
                break;
        }
    }

    private void log(String prefix, TicTacProgressDTO progress) {
        Timber.i("%s Ход %s: %d %s", prefix, progress.getUuid(), progress.getBlockIndex(), progress.getState().name());
    }
}
