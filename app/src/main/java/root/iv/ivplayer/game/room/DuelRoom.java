package root.iv.ivplayer.game.room;

import com.google.gson.Gson;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.network.ws.pubnub.PNUtil;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;
import timber.log.Timber;

// Комната для дуэли. Является комнатой и реализует действия для слежения за количеством
public class DuelRoom extends Room implements PlayerRoom {
    private Scene scene;

    public DuelRoom(Scene scene) {
        super(2);
        this.scene = scene;
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
}
