package root.iv.ivplayer.game.room;

import android.view.MotionEvent;

import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.BlockState;
import root.iv.ivplayer.game.tictac.TicTacEngine;
import root.iv.ivplayer.game.tictac.TicTacToeScene;
import root.iv.ivplayer.network.ws.pubnub.dto.TicTacStateDTO;
import root.iv.ivplayer.service.ChatServiceConnection;
import timber.log.Timber;

// Комната для дуэли. Является комнатой и реализует действия для слежения за количеством
public class DuelRoom extends Room implements PlayerRoom {
    private Scene scene;
    private ChatServiceConnection serviceConnection;
    private TicTacEngine engine;


    public DuelRoom(ChatServiceConnection serviceConnection, TicTacTextures textures) {
        super(2);
        this.serviceConnection = serviceConnection;

        this.engine = new TicTacEngine();
        engine.setCurrentState(BlockState.CROSS);

        this.scene = new TicTacToeScene(textures, engine);

        scene.getMainController().setTouchHandler(this::touchHandler);
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int oldHistorySize = engine.getHistorySize();
                engine.touchUp(event.getX(), event.getY());
                if (engine.getHistorySize() > oldHistorySize) {
                    TicTacStateDTO state = engine.getLastState();
                    state.setUuid(serviceConnection.getSelfUUID());

                    Timber.i("Ход %s: %d %s", state.getUuid(), state.getBlockIndex(), state.getState().name());
                }
                break;
        }
    }
}
