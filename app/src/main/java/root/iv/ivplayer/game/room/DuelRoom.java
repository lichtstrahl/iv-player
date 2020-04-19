package root.iv.ivplayer.game.room;

import android.view.MotionEvent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.BlockState;
import root.iv.ivplayer.game.tictac.TicTacEngine;
import root.iv.ivplayer.game.tictac.TicTacToeScene;
import root.iv.ivplayer.network.ws.pubnub.dto.TicTacProgressDTO;
import root.iv.ivplayer.service.ChatServiceConnection;
import root.iv.ivplayer.ui.activity.MainActivity;
import timber.log.Timber;

// Комната для дуэли. Является комнатой и реализует действия для слежения за количеством
public class DuelRoom extends Room implements PlayerRoom {
    private Scene scene;
    private ChatServiceConnection serviceConnection;
    private TicTacEngine engine;
    private Gson gson;


    public DuelRoom(ChatServiceConnection serviceConnection, TicTacTextures textures) {
        super(2);
        this.serviceConnection = serviceConnection;

        this.engine = new TicTacEngine();
        engine.setCurrentState(BlockState.CROSS);

        this.scene = new TicTacToeScene(textures, engine);
        scene.getMainController().setTouchHandler(this::touchHandler);

        this.gson = new Gson();
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
        String body = msg.getMessage().getAsString();
        TicTacProgressDTO progress = gson.fromJson(body, TicTacProgressDTO.class);
        log("receive:", progress);
        engine.markBlock(progress.getBlockIndex(), progress.getState());
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
                    TicTacProgressDTO progress = engine.getLastState();
                    progress.setUuid(serviceConnection.getSelfUUID());
                    log("send:", progress);
                    String jsonState = gson.toJson(progress);
                    serviceConnection.publishMessageToChannel(jsonState, MainActivity.CHANNEL_NAME, null);
                }
                break;
        }
    }

    private void log(String prefix, TicTacProgressDTO progress) {
        Timber.i("%s Ход %s: %d %s", prefix, progress.getUuid(), progress.getBlockIndex(), progress.getState().name());
    }
}
