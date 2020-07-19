package root.iv.ivplayer.game.fanorona.slot;

import android.graphics.Canvas;
import android.graphics.Paint;

import lombok.Getter;
import root.iv.ivplayer.game.fanorona.FanoronaRole;
import root.iv.ivplayer.game.fanorona.textures.ChipTextures;
import root.iv.ivplayer.game.fanorona.textures.SlotTextures;
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
    private FanoronaRole role;
    private ObjectGenerator blackGenerator;
    private ObjectGenerator whiteGenerator;
    @Getter
    private SlotState state;

    // Paints
    private Paint paintSelect;
    private Paint paintProgress;
    private Paint paintHasProgress;
    private Paint paintFree;

    private Slot(StaticObject2 object2, int radius, SlotTextures slotTextures) {
        super(object2.getPosition(), object2.getDrawable(), object2.getWidth(), object2.getHeight());
        this.bounds = GeometryFactory.newFactory()
                .pivotCircle(position, radius);
        this.role = FanoronaRole.FREE;

        int alpha = (int)Math.round(slotTextures.getAlpha() * 255);
        this.paintSelect = new Paint();
        paintSelect.setColor(slotTextures.getSelectedColor());
        paintSelect.setAlpha(alpha);
        paintSelect.setStyle(Paint.Style.FILL);


        this.paintProgress = new Paint();
        paintProgress.setColor(slotTextures.getProgressColor());
        paintProgress.setAlpha(alpha);
        paintProgress.setStyle(Paint.Style.FILL);

        this.paintHasProgress = new Paint();
        paintHasProgress.setColor(slotTextures.getHasProgressColor());
        paintHasProgress.setAlpha(alpha);
        paintHasProgress.setStyle(Paint.Style.FILL);

        this.paintFree = new Paint();
        paintFree.setColor(slotTextures.getFreeColor());
        paintFree.setAlpha(alpha);
        paintFree.setStyle(Paint.Style.STROKE);
        paintFree.setStrokeWidth(7);

        state = SlotState.DEFAULT;
    }

    @Override
    public boolean contain(Point2 point) {
        return bounds.contain(point);
    }

    public static Slot of(StaticObject2 object2, int radius, ChipTextures chipTextures, SlotTextures slotTextures) {
        Slot slot = new Slot(object2, radius, slotTextures);

        int iconW = slot.width - margin*2;
        int iconH = slot.height - margin*2;

        slot.whiteGenerator = new ObjectGenerator();
        slot.whiteGenerator.setDrawable(chipTextures.getWhiteChip());
        slot.whiteGenerator.setFixSize(iconW, iconH);

        slot.blackGenerator = new ObjectGenerator();
        slot.blackGenerator.setDrawable(chipTextures.getBlackChip());
        slot.blackGenerator.setFixSize(iconW, iconH);

        return slot;
    }

    @Override
    public void render(Canvas canvas) {
        int x0 = Math.round(position.x) + margin;
        int y0 = Math.round(position.y) + margin;

        // Помечаем ячейку выбранной
        Point2 center = bounds.getCenter();

        // Отрисовываем соответствующее состояние ячейки (заливаем фон)
        switch (state) {
            case SELECTED:
                canvas.drawCircle(center.x, center.y, bounds.getRadius(), paintSelect);
                break;
            case PROGRESS:
                canvas.drawCircle(center.x, center.y, bounds.getRadius(), paintProgress);
                break;
            case HAS_PROGRESS:
                canvas.drawCircle(center.x, center.y, bounds.getRadius(), paintHasProgress);
                break;
        }

        // Рисуем круглую ячейку
        canvas.drawCircle(center.x, center.y, bounds.getRadius(), paintFree);

        // Рисуем Фишку игрока
        switch (role) {
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

    public void mark(FanoronaRole role) {
        this.role = role;
    }

    public void mark(Slot slot) {
        this.role = slot.role;
    }

    public void select() {
        this.state = SlotState.SELECTED;
    }

    public void progress() {
        this.state = SlotState.PROGRESS;
    }

    public void hasAgressiveProgress() {
        this.state = SlotState.HAS_PROGRESS;
    }

    public void release() {
        this.state = SlotState.DEFAULT;
    }
}
