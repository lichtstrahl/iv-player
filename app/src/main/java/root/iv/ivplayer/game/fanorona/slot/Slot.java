package root.iv.ivplayer.game.fanorona.slot;

import android.graphics.Canvas;
import android.graphics.Paint;

import lombok.Getter;
import root.iv.ivplayer.game.fanorona.FanoronaRole;
import root.iv.ivplayer.game.fanorona.textures.ChipTextures;
import root.iv.ivplayer.game.fanorona.textures.MarkAttackTextures;
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
    private Paint paintMarkAttack;

    private Slot(StaticObject2 object2, int radius, SlotTextures slotTextures, MarkAttackTextures attackTextures) {
        super(object2.getPosition(), object2.getDrawable(), object2.getWidth(), object2.getHeight());
        this.bounds = GeometryFactory.newFactory()
                .pivotCircle(position, radius);
        this.role = FanoronaRole.FREE;

        int slotAlpha = (int)Math.round(slotTextures.getAlpha() * 255);
        this.paintSelect = new Paint();
        paintSelect.setColor(slotTextures.getSelectedColor());
        paintSelect.setAlpha(slotAlpha);
        paintSelect.setStyle(Paint.Style.FILL);


        this.paintProgress = new Paint();
        paintProgress.setColor(slotTextures.getProgressColor());
        paintProgress.setAlpha(slotAlpha);
        paintProgress.setStyle(Paint.Style.FILL);

        this.paintHasProgress = new Paint();
        paintHasProgress.setColor(slotTextures.getHasProgressColor());
        paintHasProgress.setAlpha(slotAlpha);
        paintHasProgress.setStyle(Paint.Style.FILL);

        this.paintMarkAttack = new Paint();
        paintMarkAttack.setColor(attackTextures.getColor());
        paintMarkAttack.setAlpha((int)Math.round(attackTextures.getAlpha() * 255));
        paintMarkAttack.setStyle(Paint.Style.STROKE);
        paintMarkAttack.setStrokeWidth(10);

        this.paintFree = new Paint();
        paintFree.setColor(slotTextures.getFreeColor());
        paintFree.setAlpha(slotAlpha);
        paintFree.setStyle(Paint.Style.STROKE);
        paintFree.setStrokeWidth(7);
        paintFree.setAntiAlias(true);

        state = SlotState.DEFAULT;
    }

    @Override
    public boolean contain(Point2 point) {
        return bounds.contain(point);
    }

    public static Slot of(StaticObject2 object2, int radius, ChipTextures chipTextures, SlotTextures slotTextures,
                          MarkAttackTextures attackTextures) {
        Slot slot = new Slot(object2, radius, slotTextures, attackTextures);

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

        // Помечаем ячейку как возможное направление атаки
        switch (state) {
            case MARK_FOR_ATTACK:
                int d = bounds.getRadius()*2;
                canvas.drawLine(x0, y0, x0+d, y0+d, paintMarkAttack);
                canvas.drawLine(x0, y0+d, x0+d, y0, paintMarkAttack);
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

    public void markForAttack() {
        this.state = SlotState.MARK_FOR_ATTACK;
    }
}
