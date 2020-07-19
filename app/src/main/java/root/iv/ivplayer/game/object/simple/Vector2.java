package root.iv.ivplayer.game.object.simple;

import android.graphics.PointF;

public class Vector2 extends PointF {

    public static Vector2 between(Point2 p1, Point2 p2) {
        Vector2 vector = new Vector2();
        vector.x = p2.x-p1.x;
        vector.y = p2.y-p1.y;

        return vector;
    }
}
