package root.iv.ivplayer.game.room;

import android.graphics.drawable.Drawable;

import com.google.gson.Gson;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.game.scene.MPScene;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.network.ws.pubnub.PNUtil;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;
import root.iv.ivplayer.service.ChatServiceConnection;
import root.iv.ivplayer.ui.activity.MainActivity;
import timber.log.Timber;

public class YonathanRoom extends Room implements PlayerRoom {
    private Scene scene;
    private Drawable playerDrawable;
    private ChatServiceConnection serviceConnection;

    public YonathanRoom(Drawable player, ChatServiceConnection connection) {
        super(0, 10);
        this.playerDrawable = player;
        this.serviceConnection = connection;

        ObjectGenerator objectGenerator = new ObjectGenerator();
        objectGenerator.setDrawable(playerDrawable);
        objectGenerator.setFixSize(200, 200);

        this.scene = new MPScene(this::sendPosition, objectGenerator);
    }

    @Override
    public void joinPlayer(String uuid) {
        Player newPlayer = scene.addPlayer(10, 100, uuid);

        if (serviceConnection.getSelfUUID().equalsIgnoreCase(uuid)) {
            scene.grabObjectControl(newPlayer);
        }
    }

    private void sendPosition(PlayerPositionDTO positionDTO) {
        serviceConnection.publishMessageToChannel(
                new Gson().toJson(positionDTO), MainActivity.CHANNEL_NAME, null
        );
    }

    @Override
    public void leavePlayer(String uuid) {
        scene.removePlayer(uuid);
    }

    @Override
    public void receiveMsg(PNMessageResult msg) {
        PlayerPositionDTO positionDTO = new Gson().fromJson(msg.getMessage().getAsString(), PlayerPositionDTO.class);
        Timber.i("Позиция %s изменилась", PNUtil.parseLogin(positionDTO.getUuid()));
        scene.processPlayerPositionDTO(positionDTO);
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public void addListener(RoomListener listener) {

    }

    @Override
    public void removeListener() {

    }
}
