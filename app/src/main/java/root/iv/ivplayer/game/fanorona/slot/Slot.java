package root.iv.ivplayer.game.fanorona.slot;

import android.graphics.drawable.Drawable;

import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.object.simple.Point2;
import root.iv.ivplayer.game.object.simple.geometry.Circle2;
import root.iv.ivplayer.game.object.simple.geometry.GeometryFactory;

/**
 * Клетка поля. Хранит в себе отрисовываемую иконку
 * Обладает круговой область (границей для реакции на касания)
 * Хранит у себя список "друзей" - соседних слотов в которые возможен переход
 */
public class Slot extends StaticObject2 {
    private Circle2 bounds;
    private SlotState state;

    public Slot(Point2 position, Drawable drawable, int radius) {
        super(position, drawable, 0, 0);
        this.bounds = GeometryFactory.newFactory()
                .pivotCircle(position, radius);
        this.state = SlotState.FREE;
    }

    @Override
    public boolean contain(Point2 point) {
        return bounds.contain(point);
    }
}
