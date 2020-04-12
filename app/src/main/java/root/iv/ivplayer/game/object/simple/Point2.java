package root.iv.ivplayer.game.object.simple;

import android.graphics.PointF;

public class Point2 extends PointF {
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
}
