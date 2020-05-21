package root.iv.ivplayer.game.object.simple.geometry;

import root.iv.ivplayer.game.object.simple.Point2;

public class GeometryFactory {
    private GeometryFactory() {}

    public static GeometryFactory newFactory() {
        return new GeometryFactory();
    }

    public Circle2 pivotCircle(Point2 pivot, int radius) {
        return new Circle2(pivot, radius);
    }

    public Circle2 centerCircle(Point2 center, int radius) {
        center.moveOn(-radius, -radius);
        return new Circle2(center, radius);
    }

    public Rect2 rectangle(Point2 pivot, int w, int h) {
        return new Rect2(pivot, w, h);
    }

    public Rect2 square(Point2 pivot, int size) {
        return new Rect2(pivot, size, size);
    }
}
