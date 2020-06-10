package root.iv.ivplayer.game.fanorona;

import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.List;

import root.iv.ivplayer.game.fanorona.slot.Slot;
import root.iv.ivplayer.game.fanorona.slot.SlotState;
import root.iv.ivplayer.game.fanorona.slot.SlotWay;
import root.iv.ivplayer.game.fanorona.slot.Way;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.object.simple.Point2;
import root.iv.ivplayer.game.object.simple.geometry.Rect2;
import root.iv.ivplayer.game.scene.SensorScene;
import root.iv.ivplayer.game.view.GameView;
import timber.log.Timber;

public class FanoronaScene extends SensorScene {
    private static final int SLOT_RADIUS = 50;
    private static final double k = 9.0/5.0;
    private static final int COUNT_ROW = 5;
    private static final int COUNT_COLUMN = 9;

    // Текстуры
    private FanoronaTextures textures;

    // Генераторы создания объектов
    private ObjectGenerator slotGenerator;
    private ObjectGenerator backgroundGenerator;

    // Группа: слоты под фишки
    private Group<Slot> slotGroup;
    // Группы: соединения слотов
    private Group<Way> wayGroup;

    // Размер матрицы
    private int countRows;
    private int countColumns;

    // Отступы от краёв экрана
    private int startMargin;
    private int topMargin;

    public FanoronaScene(FanoronaTextures textures, int countRows, int countColumns, int startMargin, int topMargin, SlotWay[] ways) {
        super(new FanoronaController());
        this.textures = textures;

        // Стандартные размеры площадки
        this.countRows = countRows;
        this.countColumns = countColumns;

        // Генератор для фона
        backgroundGenerator = new ObjectGenerator();
        backgroundGenerator.setDrawable(textures.getBackground());

        // Генератор для сетки
        slotGenerator = new ObjectGenerator();
        slotGenerator.setDrawable(textures.getSlot());
        slotGenerator.setTintColor(textures.getSlotColor());
        slotGenerator.setFixSize(SLOT_RADIUS*2, SLOT_RADIUS*2);

        this.startMargin = startMargin;
        this.topMargin = topMargin;
        slotGroup = slotsConstruct(startMargin, topMargin, 50, SLOT_RADIUS);
        wayGroup = wayConstruct(ways, textures.getSlotColor());
    }

    @Override
    public void render(Canvas canvas) {
        // Заливка фона
        if (backgroundGenerator.hasTexture()) {
            backgroundGenerator.setFixSize(canvas.getWidth(), canvas.getHeight());
            backgroundGenerator.buildStatic(0,0).render(canvas);
        } else {
            canvas.drawColor(textures.getBackgroundColor());
        }

        // Отрисовка дорожек
        wayGroup.render(canvas);

        // Отрисовка поля
        slotGroup.render(canvas);
    }

    @Override
    public void resize(int width, int height) {
        Rect2 gameView = gameSize(width, height);
        int delta = Math.round(avg(gameView.getWidth()/17.0f, gameView.getHeight()/9.0f));
        int radius = Math.round(avg(gameView.getWidth()/17.0f/2.0f, gameView.getHeight()/9.0f/2.0f));


        Timber.i("View: width %d, height %d", width, height);
        Timber.i("Game width: %d, height %d", gameView.getWidth(), gameView.getHeight());
        Timber.i("delta: %d, radius: %d", delta, radius);
        Timber.i("Horizontal: delta*8 + radius*2*9 = %d", delta*8 + radius*2*9);
        Timber.i("Vertical: delta*4 + radius*2*5 = %d", delta*4 + radius*2*5);

//        Group<Slot> resizedSlots = slotsConstruct(horizontalMargin/2,verticalMargin/2, widthElement, heightElement, size/2);
//        // Перенос старых состояний
//        int count = slotGroup.size();
//        for (int i = 0; i < count; i++) {
//            resizedSlots.getObject(i)
//                    .mark(slotGroup.getObject(i).getState());
//        }
//        slotGroup = resizedSlots;
    }


    @Override
    public void connect(GameView gameView) {
        gameView.loadScene(this);
        gameView.setOnTouchListener(sensorController);
        gameView.setOnClickListener(sensorController);
    }

    public void markSlot(int index, SlotState state) {
        slotGroup.getObject(index).mark(state);
    }


    // Смотрим какие возможны ходы
    public void viewPossibleProgress(Point2 touchPoint) {
        for (Slot slot : slotGroup.getObjects()) {
            if (slot.getBounds().contain(touchPoint)) {


            }
        }
    }

    // Пробуем выбрать слот
    @Nullable
    public Integer selectSlot(Point2 touchPoint) {
        for (int i = 0; i < slotGroup.getObjects().size(); i++) {
            Slot slot = slotGroup.getObject(i);
            if (slot.getBounds().contain(touchPoint)) {
                slot.select();
                return i;
            }
        }

        return null;
    }

    public void selectSlot(int i) {
        slotGroup.getObject(i).select();
    }

    public void progressSlot(int i) {
        slotGroup.getObject(i).progress();
    }

    public void releaseAllSlots() {
        for (Slot slot : slotGroup.getObjects())
            slot.release();
    }

    private float avg(float ... numbers) {
        float sum = 0.0f;

        for (float number : numbers)
            sum += number;

        return sum / numbers.length;
    }

    private Rect2 gameSize(int width, int height) {
        Point2 pivot = new Point2();

        width -= startMargin*2;
        height -= topMargin*2;

        int w = width;
        int h = Math.round(w/(float)k);

        if (w > width || h > height) {
            h = height;
            w = Math.round(h*(float)k);
        }

        return new Rect2(pivot, w, h);
    }

    private Group<Slot> slotsConstruct(int startMargin, int topMargin, int delta, int radius) {
        return slotsConstruct(startMargin, topMargin, delta, delta, radius);
    }

    private Group<Slot> slotsConstruct(int startMargin, int topMargin, int hDelta, int vDelta, int radius) {
        Group<Slot> slots = Group.empty();

        for (int i = 0; i < countRows; i++) {
            for (int j = 0; j < countColumns; j++) {
                int x0 = startMargin + j*(hDelta+radius*2);
                int y0 = topMargin + i*(vDelta+radius*2);

                StaticObject2 staticObject2 = slotGenerator.buildStatic(x0, y0);
                Slot slot = Slot.of(staticObject2, radius, textures.getChipWhite(), textures.getChipWhiteColor(),
                        textures.getChipBlack(), textures.getChipBlackColor());
                slots.add(slot);
            }
        }

        return slots;
    }

    private Group<Way> wayConstruct(SlotWay[] ways, @ColorInt int wayColor) {
        Group<Way> group = Group.empty();


        for (SlotWay way : ways) {
            int index1 = way.getI1()*COUNT_COLUMN + way.getJ1();
            int index2 = way.getI2()*COUNT_COLUMN + way.getJ2();

            Point2 center1 = slotGroup.getObject(index1).getBounds().getCenter();
            Point2 center2 = slotGroup.getObject(index2).getBounds().getCenter();

            group.add(Way.of(center1, center2, wayColor));
        }

        return group;
    }


}
