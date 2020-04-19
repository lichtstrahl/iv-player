package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import root.iv.ivplayer.game.controller.Controller;
import root.iv.ivplayer.game.controller.PlayerController;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.MovableObject;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.game.object.simple.Object2;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;
import timber.log.Timber;


// Сцена для multiplayer-ных игр
public class MPScene implements Scene {

    private ObjectGenerator playerGenerator;
    private PlayerController playerController;
    // Список объектов, которые можно отрисовать
    private List<DrawableObject> drawableObjects;
    private List<String> players;


    public MPScene(Consumer<PlayerPositionDTO> sendPosition, ObjectGenerator playerGenerator) {
        this.playerGenerator = playerGenerator;
        this.playerController = new PlayerController(sendPosition);
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

            Player newPlayer = playerGenerator.buildPlayer(
                    Math.round(position.getX0()),
                    Math.round(position.getY0()),
                    position.getUuid()
            );
            addDrawableObject(newPlayer);
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
    public Player addPlayer(int x0, int y0, String uuid) {
        Player newPlayer = playerGenerator.buildPlayer(x0, y0, uuid);
        addDrawableObject(newPlayer);
        return newPlayer;
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
    public void removePlayer(String uuid) {
        Integer index = getIndexPlayer(uuid);
        if (index != null) {
            players.remove(uuid);
            drawableObjects.remove(index.intValue());
        }
    }

    @Override
    public void grabObjectControl(Object2 object) {
        playerController.grabObject(object);
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
