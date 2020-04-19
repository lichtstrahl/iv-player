package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import root.iv.ivplayer.game.controller.Controller;
import root.iv.ivplayer.game.controller.PlayerController;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.MovableObject;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;
import root.iv.ivplayer.service.ChatServiceConnection;
import root.iv.ivplayer.ui.activity.MainActivity;
import timber.log.Timber;


// Сцена для multiplayer-ных игр
public class MPScene implements Scene {

    private ObjectGenerator playerGenerator;
    private ChatServiceConnection serviceConnection;
    private PlayerController playerController;
    // Список объектов, которые можно отрисовать
    private List<DrawableObject> drawableObjects;
    private List<String> players;


    public MPScene(ObjectGenerator playerGenerator, ChatServiceConnection serviceConnection) {
        this.playerGenerator = playerGenerator;
        this.serviceConnection = serviceConnection;
        this.playerController = new PlayerController(this::sendPosition);
        drawableObjects = new ArrayList<>();
        players = new ArrayList<>();
    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        drawableObjects.forEach(obj -> obj.render(canvas));
    }

    @Override
    public void joinPlayer(String joinUUID, float x, float y) {
        Player newPlayer = new Player(playerGenerator.buildActor(Math.round(x), Math.round(y)), joinUUID);
        addDrawableObject(newPlayer);

        if (serviceConnection.getSelfUUID().equalsIgnoreCase(joinUUID)) {
            playerController.grabObject(newPlayer);
        }
    }

    @Override
    public Controller getMainController() {
        return playerController;
    }

    @Override
    public void processPlayerPositionDTO(PlayerPositionDTO position) {
        // Если такой игрок существует, то сдвигаем его, иначе создаём
        if (findPlayer(position.getUuid())) {
            movePlayer(
                    position.getUuid(),
                    Math.round(position.getX0()),
                    Math.round(position.getY0())
            );
        } else {
            joinPlayer(position.getUuid(), position.getX0(), position.getY0());
        }
    }

    @Override
    public void addDrawableObject(DrawableObject object2) {
        drawableObjects.add(object2);

        // Если добавляется игрок, то его uuid заносится в общий список
        if (object2 instanceof Player) {
            players.add(((Player) object2).getUuid());
        }
    }

    @Override
    public void moveOnObject(int index, float dx, float dy) {
        DrawableObject object = drawableObjects.get(index);

        if (object instanceof MovableObject) {
            ((MovableObject)object).moveOn(dx, dy);
        } else {
            Timber.w("Объект #%d не поддерживает передвижение", index);
        }
    }

    @Override
    public void leavePlayer(String uuid) {
        Integer index = getIndexPlayer(uuid);
        if (index != null) {
            players.remove(uuid);
            drawableObjects.remove(index.intValue());
        }
    }

    private void sendPosition(PlayerPositionDTO positionDTO) {
        serviceConnection.publishMessageToChannel(
                new Gson().toJson(positionDTO), MainActivity.CHANNEL_NAME, null
        );
    }

    private void movePlayer(String uuid, int x, int y) {
        // Перебираем объекты на сцене. Ищем только среди игроков
        // Выбираем нужный нам uuid и сдвигаем его в указанную позицию
        drawableObjects
                .stream()
                .filter(obj -> obj instanceof Player)
                .map(obj -> (Player)obj)
                .filter(p -> p.getUuid().equalsIgnoreCase(uuid))
                .forEach(player -> player.moveTo(x, y));
    }

    private boolean findPlayer(String uuid) {
        return players.contains(uuid);
    }

    @Nullable
    private Integer getIndexPlayer(String uuid) {
        for (int i = 0; i < drawableObjects.size(); i++) {
            DrawableObject object = drawableObjects.get(i);
            if (object instanceof Player && ((Player)object).getUuid().equals(uuid)) {
                return i;
            }
        }

        return null;
    }
}
