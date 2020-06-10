package root.iv.ivplayer.game.object.simple.geometry;

import lombok.Getter;
import root.iv.ivplayer.game.object.simple.Object2;
import root.iv.ivplayer.game.object.simple.Point2;

public class Circle2 extends Object2 {
    @Getter
    private int radius;

    public Circle2(Point2 position, int radius) {
        super(position);
        this.radius = radius;
    }

    @Override
    public boolean contain(Point2 point) {
        Point2 center = getCenter();
        return center.distantion(point) < radius;
    }

    public Point2 getCenter() {
        return Point2.point(position.x+radius, position.y+radius);
    }
}
