package root.iv.ivplayer.game;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.List;

import root.iv.ivplayer.game.object.DrawableObject2;

public class TestScene implements Scene {
    private Long lastFrameMS = 0L;
    private List<DrawableObject2> drawableObjects;

    public TestScene(List<DrawableObject2> objects) {
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

    public void addObject(DrawableObject2 object2) {
        drawableObjects.add(object2);
    }
}
