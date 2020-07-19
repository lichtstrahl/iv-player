package root.iv.ivplayer.game.fanorona.slot;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.ColorInt;

import root.iv.ivplayer.game.fanorona.textures.WayTextures;
import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.object.simple.Point2;
import root.iv.ivplayer.game.object.simple.Vector2;

// Фактически это прямоугольник, который будет рисовать только собственную диагональ
public class Way extends StaticObject2 {
    private Point2 dist;
    private @ColorInt int color;
    private int slotRadius;
    private WayState state;


    private Way(Point2 p1, Point2 p2, int slotRadius, @ColorInt int color) {
        super(p1, null, 0, 0);
        this.dist = p2;
        this.color = color;
        this.state = WayState.ORIGIN;
        this.slotRadius = slotRadius;
    }


    public static Way of(Point2 p1, Point2 p2, int radius, WayTextures textures) {
        Way way = new Way(p1, p2, radius, textures.getOriginColor());
        return way;
    }

    @Override
    public void render(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(5.0f);
        paint.setColor(color);
        // Полупрозрачный
        paint.setAlpha(120);

        // Делаем отступ от начала и конца отрезка
        Point2 from = Point2.point(position.x, position.y);
        from.moveOn(Vector2.between(position, dist), slotRadius);

        Point2 to = Point2.point(dist.x, dist.y);
        to.moveOn(Vector2.between(dist, position), slotRadius);

        canvas.drawLine(from.x, from.y, to.x, to.y, paint);
    }

    public void used() {
        this.state = WayState.USED;
    }

    public void release() {
        this.state = WayState.ORIGIN;
    }
}
