package root.iv.ivplayer.game;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.List;

import root.iv.ivplayer.game.object.DrawableObject;

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
}
