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
    private static final int INIT_POWER = -1;

    private Point2 dist;
    private @ColorInt int originColor;
    private int slotRadius;
    private WayState state;
    private int[] usedColors;
    private int power;



    private Way(Point2 p1, Point2 p2, int slotRadius, WayTextures textures) {
        super(p1, null, 0, 0);
        this.dist = p2;
        this.originColor = textures.getOriginColor();
        this.state = WayState.ORIGIN;
        this.power = INIT_POWER;
        this.slotRadius = slotRadius;
        this.usedColors = textures.getUsedColor();
    }


    public static Way of(Point2 p1, Point2 p2, int radius, WayTextures textures) {
        Way way = new Way(p1, p2, radius, textures);
        return way;
    }

    @Override
    public void render(Canvas canvas) {
        int currentColor = originColor;

        switch (state) {
            case USED:
                int powerIndex = Math.min(2, power);
                currentColor = usedColors[powerIndex];
                break;
        }

        Paint paint = new Paint();
        paint.setStrokeWidth(5.0f);
        paint.setColor(currentColor);
        paint.setAlpha(130);

        // Делаем отступ от начала и конца отрезка
        Point2 from = Point2.point(position.x, position.y);
        from.moveOn(Vector2.between(position, dist), slotRadius);

        Point2 to = Point2.point(dist.x, dist.y);
        to.moveOn(Vector2.between(dist, position), slotRadius);

        canvas.drawLine(from.x, from.y, to.x, to.y, paint);
    }

    public void used() {
        this.state = WayState.USED;
        this.power = 0;
    }

    public void used(int power) {
        this.state = WayState.USED;
        this.power = power;
    }

    public void release() {
        this.state = WayState.ORIGIN;
        this.power = INIT_POWER;
    }

    // Соединяет ли данная дорожка две точки
    public boolean connect(Point2 p1, Point2 p2) {
        return (position.nearly(p1) && dist.nearly(p2))
                || (position.nearly(p2) && dist.nearly(p1));
    }
}
