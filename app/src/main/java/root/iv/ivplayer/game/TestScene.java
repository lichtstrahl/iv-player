package root.iv.ivplayer.game;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.List;

import root.iv.ivplayer.game.controller.MoveController;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.Player;

public class TestScene implements Scene {
    private Long lastFrameMS = 0L;
    // Список объектов, которые можно отрисовать
    private List<DrawableObject> drawableObjects;

    public TestScene(List<DrawableObject> objects) {
        drawableObjects = objects;
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
}
