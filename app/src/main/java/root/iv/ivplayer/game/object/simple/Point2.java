package root.iv.ivplayer.game.object.simple;

import android.graphics.PointF;

public class Point2 extends PointF {
    public static final double EPS = 1e-1;

    public static Point2 point(float x, float y) {
        Point2 p = new Point2();
        p.x = x;
        p.y = y;
        return p;
    }

    public static Point2 point(int x, int y) {
        Point2 p = new Point2();
        p.x = x;
        p.y = y;
        return p;
    }

    @Override
    public boolean equals(Object o) {
        Point2 other = (Point2) o;
        return x == other.x && y == other.y;
    }

    public void moveOn(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    // Сдвиг в определённом направлении на заданное расстояние
    public void moveOn(Vector2 vector, int length) {
        float len = vector.length();

        // Коэфициент подобия
        float k = length/len;

        float dx = vector.x * k;
        float dy = vector.y * k;

        moveOn(dx, dy);
    }

    public double distantion(Point2 point) {
        float deltaX = Math.abs(point.x - x);
        float deltaY = Math.abs(point.y - y);
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
    }

    public boolean nearly(Point2 point) {
        return distantion(point) < EPS;
    }
}
