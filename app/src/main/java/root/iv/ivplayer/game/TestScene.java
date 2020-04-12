package root.iv.ivplayer.game;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.Player;

public class TestScene implements Scene {
    private Long lastFrameMS = 0L;
    // Список объектов, которые можно отрисовать
    private List<DrawableObject> drawableObjects;
    private List<String> players;

    public TestScene() {
        drawableObjects = new ArrayList<>();
        players = new ArrayList<>();
    }

    @Override
    public void render(Canvas canvas) {
        long startRender = System.currentTimeMillis();

        canvas.drawColor(Color.BLACK);
        drawableObjects.forEach(obj -> obj.render(canvas));

        long finishRender = System.currentTimeMillis();
        lastFrameMS = finishRender;
    }

    public void addDrawableObject(DrawableObject object2) {
        drawableObjects.add(object2);

        // Если добавляется игрок, то его uuid заносится в общий список
        if (object2 instanceof Player) {
            players.add(((Player) object2).getUuid());
        }
    }

    public void movePlayer(String uuid, int x, int y) {
        // Перебираем объекты на сцене. Ищем только среди игроков
        // Выбираем нужный нам uuid и сдвигаем его в указанную позицию
        drawableObjects
                .stream()
                .filter(obj -> obj instanceof Player)
                .map(obj -> (Player)obj)
                .filter(p -> p.getUuid().equalsIgnoreCase(uuid))
                .forEach(player -> player.moveTo(x, y));
    }

    public boolean findPlayer(String uuid) {
        return players.contains(uuid);
    }
}
