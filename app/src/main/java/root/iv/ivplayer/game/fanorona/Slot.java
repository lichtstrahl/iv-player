package root.iv.ivplayer.game.fanorona;

import android.graphics.drawable.Drawable;

import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.object.simple.Point2;
import root.iv.ivplayer.game.object.simple.geometry.Circle2;
import root.iv.ivplayer.game.object.simple.geometry.GeometryFactory;

/**
 * Клетка поля. Хранит в себе отрисовываемую иконку
 * Обладает круговой область (границей для реакции на касания)
 */
public class Slot extends StaticObject2 {
    private Circle2 bounds;

    public Slot(Point2 position, Drawable drawable, int w, int h) {
        super(position, drawable, w, h);
        this.bounds = GeometryFactory.newFactory()
                .pivotCircle(position, w/2);
    }

    @Override
    public boolean contain(Point2 point) {
        return bounds.contain(point);
    }
}
