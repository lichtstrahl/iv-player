package root.iv.ivplayer.game.fanorona.slot;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import lombok.Getter;
import root.iv.ivplayer.game.object.ObjectGenerator;
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
    private static final int margin = 10;

    private Circle2 bounds;
    @Getter
    private SlotState state;
    private ObjectGenerator blackGenerator;
    private ObjectGenerator whiteGenerator;

    private Slot(StaticObject2 object2, int radius) {
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

        int iconW = slot.width - margin*2;
        int iconH = slot.height - margin*2;

        slot.whiteGenerator = new ObjectGenerator();
        slot.whiteGenerator.setDrawable(chipWhite);
        slot.whiteGenerator.setTintColor(colorWhite);
        slot.whiteGenerator.setFixSize(iconW, iconH);

        slot.blackGenerator = new ObjectGenerator();
        slot.blackGenerator.setDrawable(chipBlack);
        slot.blackGenerator.setTintColor(colorBlack);
        slot.blackGenerator.setFixSize(iconW, iconH);

        return slot;
    }

    @Override
    public void render(Canvas canvas) {
        super.render(canvas);

        int x0 = Math.round(position.x) + margin;
        int y0 = Math.round(position.y) + margin;

        // Отрисовали саму ячейку, теперь рисуем её состояние
        switch (state) {
            case WHITE:
                StaticObject2 white = whiteGenerator.buildStatic(x0, y0);
                white.render(canvas);
                break;

            case BLACK:
                StaticObject2 black = blackGenerator.buildStatic(x0, y0);
                black.render(canvas);
                break;
        }
    }

    public void mark(SlotState state) {
        this.state = state;
    }
}
