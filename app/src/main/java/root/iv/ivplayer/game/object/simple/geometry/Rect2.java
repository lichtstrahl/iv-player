package root.iv.ivplayer.game.object.simple.geometry;

import root.iv.ivplayer.game.object.simple.Object2;
import root.iv.ivplayer.game.object.simple.Point2;

public class Rect2 extends Object2 {
    private int width;
    private int height;

    public Rect2(Point2 position, int w, int h) {
        super(position);
        this.width = w;
        this.height = h;
    }

    @Override
    public boolean contain(Point2 point) {
        boolean horizontal = point.x > position.x && point.x < (position.x + width);
        boolean vertical = point.y > position.y && point.y < (position.y + height);
        return horizontal && vertical;
    }
}
