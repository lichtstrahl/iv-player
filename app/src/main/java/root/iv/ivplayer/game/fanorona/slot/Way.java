package root.iv.ivplayer.game.fanorona.slot;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.ColorInt;

import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.object.simple.Point2;
// Фактически это прямоугольник, который будет рисовать только собственную диагональ
public class Way extends StaticObject2 {
    private Point2 dist;
    private @ColorInt int color;


    private Way(Point2 p1, Point2 p2, @ColorInt int color) {
        super(p1, null, 0, 0);
        this.dist = p2;
        this.color = color;
    }


    public static Way of (Point2 p1, Point2 p2, @ColorInt int color) {
        Way way = new Way(p1, p2, color);
        return way;
    }

    @Override
    public void render(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(5.0f);
        paint.setColor(color);
        // Полупрозрачный
        paint.setAlpha(120);

        canvas.drawLine(position.x, position.y, dist.x, dist.y, paint);
    }
}
