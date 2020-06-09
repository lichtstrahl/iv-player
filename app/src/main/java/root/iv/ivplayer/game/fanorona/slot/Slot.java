package root.iv.ivplayer.game.fanorona.slot;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import lombok.Getter;
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
    @Getter
    private SlotState state;

    public Slot(StaticObject2 object2, int radius) {
        super(object2.getPosition(), object2.getDrawable(), object2.getWidth(), object2.getHeight());
        this.bounds = GeometryFactory.newFactory()
                .pivotCircle(position, radius);
        this.state = SlotState.FREE;
    }

    @Override
    public boolean contain(Point2 point) {
        return bounds.contain(point);
    }

    public static Slot of(StaticObject2 object2, int radius,
                          Drawable chipWhite, @ColorInt int colorWhite,
                          Drawable chipBlack, @ColorInt int colorBlack) {
        Slot slot = new Slot(object2, radius);

        return slot;
    }

    @Override
    public void render(Canvas canvas) {
        super.render(canvas);
    }

    public void mark(SlotState state) {
        this.state = state;
    }
}
