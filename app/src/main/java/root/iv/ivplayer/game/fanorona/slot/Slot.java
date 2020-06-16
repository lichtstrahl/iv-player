package root.iv.ivplayer.game.fanorona.slot;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

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

    @Getter
    private Circle2 bounds;
    @Getter
    private SlotState state;
    private ObjectGenerator blackGenerator;
    private ObjectGenerator whiteGenerator;
    @Getter
    private boolean selected;
    @Getter
    private boolean progress;
    @Getter
    private boolean hasProgress;

    // Paints
    private Paint paintSelect;
    private Paint paintProgress;
    private Paint paintHasProgress;

    private Slot(StaticObject2 object2, int radius) {
        super(object2.getPosition(), object2.getDrawable(), object2.getWidth(), object2.getHeight());
        this.bounds = GeometryFactory.newFactory()
                .pivotCircle(position, radius);
        this.state = SlotState.FREE;

        this.paintSelect = new Paint();
        paintSelect.setColor(Color.YELLOW);
        paintSelect.setAlpha(90);
        paintSelect.setStyle(Paint.Style.FILL);


        this.paintProgress = new Paint();
        paintProgress.setColor(Color.RED);
        paintProgress.setAlpha(90);
        paintProgress.setStyle(Paint.Style.FILL);

        this.paintHasProgress = new Paint();
        paintHasProgress.setColor(Color.GREEN);
        paintHasProgress.setAlpha(90);
        paintHasProgress.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean contain(Point2 point) {
        return bounds.contain(point);
    }

    public static Slot of(StaticObject2 object2, int radius,
                          Drawable chipWhite, @Nullable @ColorInt Integer colorWhite,
                          Drawable chipBlack, @Nullable @ColorInt Integer colorBlack) {
        Slot slot = new Slot(object2, radius);

        int iconW = slot.width - margin*2;
        int iconH = slot.height - margin*2;

        slot.whiteGenerator = new ObjectGenerator();
        slot.whiteGenerator.setDrawable(chipWhite);
        slot.whiteGenerator.setFixSize(iconW, iconH);
        if (colorWhite != null)
            slot.whiteGenerator.setTintColor(colorWhite);

        slot.blackGenerator = new ObjectGenerator();
        slot.blackGenerator.setDrawable(chipBlack);
        slot.blackGenerator.setFixSize(iconW, iconH);
        if (colorBlack != null)
            slot.blackGenerator.setTintColor(colorBlack);

        return slot;
    }

    @Override
    public void render(Canvas canvas) {
        int x0 = Math.round(position.x) + margin;
        int y0 = Math.round(position.y) + margin;

        // Помечаем ячейку выбранной
        Point2 center = bounds.getCenter();


        if (selected) {
            canvas.drawCircle(center.x, center.y, bounds.getRadius(), paintSelect);
        }

        if (progress) {
            canvas.drawCircle(center.x, center.y, bounds.getRadius(), paintProgress);
        }

        if (hasProgress) {
            canvas.drawCircle(center.x, center.y, bounds.getRadius(), paintHasProgress);
        }

        // Рисуем круглую ячейку
        super.render(canvas);

        // Рисуем её состояние
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

    public void select() {
        this.selected = true;
    }

    public void progress() {
        this.progress = true;
    }

    public void hasAgressiveProgress() {
        this.hasProgress = true;
    }

    public void release() {
        this.selected = false;
        this.progress = false;
    }
}
