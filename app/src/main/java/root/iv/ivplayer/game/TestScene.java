package root.iv.ivplayer.game;

import android.graphics.Canvas;
import android.graphics.Color;

public class TestScene implements Scene {
    private Long lastFrameMS = 0L;


    @Override
    public void render(Canvas canvas) {
        long startRender = System.currentTimeMillis();

        canvas.drawColor(Color.BLACK);

        long finishRender = System.currentTimeMillis();
        lastFrameMS = finishRender;
    }
}
