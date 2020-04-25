package root.iv.ivplayer.game.room;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import java.util.Objects;

import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.BlockState;
import root.iv.ivplayer.game.tictac.DrawableBlockState;
import root.iv.ivplayer.game.tictac.TicTacEngine;
import root.iv.ivplayer.game.tictac.TicTacJsonProcessor;
import root.iv.ivplayer.game.tictac.TicTacToeScene;
import root.iv.ivplayer.game.tictac.dto.TicTacDTO;
import root.iv.ivplayer.game.tictac.dto.TicTacRoomStatusDTO;
import root.iv.ivplayer.network.ws.pubnub.PNUtil;
import root.iv.ivplayer.network.ws.pubnub.callback.PNHereNowCallback;
import root.iv.ivplayer.game.tictac.dto.TicTacDTOType;
import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;
import root.iv.ivplayer.game.tictac.dto.TicTacWinDTO;
import root.iv.ivplayer.service.ChatServiceConnection;
import root.iv.ivplayer.ui.activity.MainActivity;
import timber.log.Timber;

// Комната для дуэли. Является комнатой и реализует действия для слежения за количеством
public class DuelRoom extends Room implements PlayerRoom {
    private Scene scene;
    private ChatServiceConnection serviceConnection;
    private TicTacEngine engine;
    private TicTacJsonProcessor jsonProcessor;
    @Nullable
    private Listener roomListener;
    private DrawableBlockState icons;


    public DuelRoom(ChatServiceConnection serviceConnection, TicTacTextures textures) {
        super(2);
        this.serviceConnection = serviceConnection;

        this.engine = new TicTacEngine();
        serviceConnection.hereNow(new PNHereNowCallback(this::hereNowProcess, Timber::e), MainActivity.CHANNEL_NAME);

        this.scene = new TicTacToeScene(textures, engine);
        scene.getMainController().setTouchHandler(this::touchHandler);

        this.jsonProcessor = new TicTacJsonProcessor();
        this.icons = DrawableBlockState.create(textures.getCross(), textures.getCircle());
        Timber.i("Новая комната создана");
    }

    @Override
    public void joinPlayer(String uuid) {
        Timber.i("Вход %s", PNUtil.parseLogin(uuid));

        String selfID = serviceConnection.getSelfUUID();

        if (state == RoomState.CLOSE) {
            Timber.i("Отправка сообщения о том, что комната закрыта");
            String roomStatus = jsonProcessor.buildRoomStatusDTO(selfID, RoomState.CLOSE);
            serviceConnection.publishMessageToChannel(roomStatus, MainActivity.CHANNEL_NAME, null);
        }

        if (currentPlayers < maxPlayers) {
            currentPlayers++;

            // Если игроков достаточно, пробуем перейти в состояние "GAME"
            if (currentPlayers >= minPlauers && currentPlayers <= maxPlayers) {
                changeState(RoomState.GAME);
            }


            if (!selfID.equals(uuid) && roomListener != null) {
                roomListener.updatePlayers(PNUtil.parseLogin(selfID), PNUtil.parseLogin(uuid), icons.getCross(), icons.getCircle());
            }
        }
    }

    @Override
    public void addListener(RoomListener listener) {
        this.roomListener = (Listener) listener;
    }

    @Override
    public void removeListener() {
        this.roomListener = null;
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
            if (roomListener != null)
                roomListener.updatePlayers(serviceConnection.getSelfUUID(), null,
                        icons.getIcon(BlockState.CROSS), null);
        } else {
            String uuid = channelData.getOccupants().get(0).getUuid();
            engine.setCurrentState(BlockState.CIRCLE);
            joinPlayer(uuid);
            Timber.i("В комнате уже %s", uuid);
            if (roomListener != null)
                roomListener.updatePlayers(serviceConnection.getSelfUUID(), PNUtil.parseLogin(uuid),
                        icons.getIcon(BlockState.CIRCLE), icons.getIcon(BlockState.CROSS));
        }

    }

    @Override
    public void leavePlayer(String uuid) {
        Timber.i("Выход %s", PNUtil.parseLogin(uuid));
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
        TicTacDTOType dtoType = jsonProcessor.dtoType(body);

        switch (dtoType) {
            case PROGRESS:
                TicTacProgressDTO progress = jsonProcessor.receiveProgressDTO(body);
                log("receive:", progress);
                engine.markBlock(progress.getBlockIndex(), progress.getState());
                break;

            case WIN:
                TicTacWinDTO win = jsonProcessor.receiveWinDTO(body);
                Timber.i("Игрок %s выиграл: %s", win.getUuid(), String.valueOf(win.isWin()));
                changeState(RoomState.CLOSE);
                win(win.getUuid());
                break;

            case ROOM_STATE:
                TicTacRoomStatusDTO roomStatusDTO = jsonProcessor.reciveStatusRoomDTO(body);
                Timber.i("Вход в закрытую комнату");
                if (roomStatusDTO.getRoomState() == RoomState.CLOSE && roomListener != null) {
                    roomListener.exit();
                    removeListener();
                }
                break;
        }
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    private void changeState(RoomState newState) {
            boolean transit = RoomStateJump.of(this.state).possibleTransit(newState);
            if (transit) {
                this.state = newState;
                if (roomListener != null) {
                    roomListener.changeStatus(this.state == RoomState.GAME);
                }
            }
            else
                Timber.w("Переход %s -> %s невозможен", this.state.name(), newState.name());
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
                    String jsonState = jsonProcessor.buildProgressDTO(progress);
                    serviceConnection
                            .publishMessageToChannel(jsonState, MainActivity.CHANNEL_NAME, null);
                    log("send:", progress);

                    if (engine.win()) {
                        String winMsg = jsonProcessor.buildWinDTO(selfUUID);
                        serviceConnection.publishMessageToChannel(winMsg, MainActivity.CHANNEL_NAME, null);
                        changeState(RoomState.CLOSE);
                        win(selfUUID);
                    }
                }
                break;
        }
    }

    private void win(String uuid) {
        if (roomListener != null) roomListener.win(uuid);
    }

    private void log(String prefix, TicTacProgressDTO progress) {
        Timber.i("%s Ход %s: %d %s", prefix, progress.getUuid(), progress.getBlockIndex(), progress.getState().name());
    }

    public interface Listener extends RoomListener {
        void updatePlayers(@Nullable String login1, @Nullable String login2, Drawable state1, Drawable state2);
        void win(String uuid);
        void changeStatus(boolean roomState);
        void exit();
    }
}
