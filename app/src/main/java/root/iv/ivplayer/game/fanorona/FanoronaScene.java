package root.iv.ivplayer.game.fanorona;

import android.graphics.Canvas;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import root.iv.ivplayer.game.fanorona.slot.Slot;
import root.iv.ivplayer.game.fanorona.slot.SlotState;
import root.iv.ivplayer.game.fanorona.slot.SlotWay;
import root.iv.ivplayer.game.fanorona.slot.Way;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.object.simple.Point2;
import root.iv.ivplayer.game.scene.SensorScene;
import root.iv.ivplayer.game.view.GameView;
import timber.log.Timber;

public class FanoronaScene extends SensorScene {
    private static final int DEFAULT_SIZE = 50;

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

        this.startMargin = startMargin;
        this.topMargin = topMargin;
        slotGroup = slotsConstruct(startMargin, topMargin, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_SIZE);
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
        Timber.i("View: width %d, height %d", width, height);

        // По горизонтали: 9 ячеек. Значит 8 отрезков
        int possibleWidth = width - startMargin*2;
        int horizontalDelta = Math.round(possibleWidth/(8.0f + 1)); // Делим как будто фишек 10 в ряду, а не 9

        // По вертикали: 5 ячеек. Значит 4 отрезка (+1)
        int possibleHeight = height - topMargin*2;
        int verticalDelta = Math.round(possibleHeight/(4.0f + 1));

        // Максимальный радиус: horizontalDelta/2
        int horizontalRadius = Math.round(horizontalDelta / 3.0f);
        int verticalRadius = Math.round(verticalDelta / 3.0f);

        int radius = Math.min(horizontalRadius, verticalRadius);

        Timber.i("PossibleWidth: %d, hDelta: %d, hRadius: %d", possibleWidth, horizontalDelta, horizontalRadius);
        Timber.i("PossibleHeight: %d, vDelta: %d, vRadius: %d", possibleHeight, verticalDelta, verticalRadius);
        Timber.i("Radius: %d", radius);


        Group<Slot> resizedSlots = slotsConstruct(startMargin,topMargin, horizontalDelta, verticalDelta, radius);
        // Перенос старых состояний
        int count = slotGroup.size();
        for (int i = 0; i < count; i++) {
            resizedSlots.getObject(i)
                    .mark(slotGroup.getObject(i).getState());
        }
        slotGroup = resizedSlots;
    }


    public void resizeWay(SlotWay[] ways) {
        wayGroup = wayConstruct(ways, textures.getSlotColor());
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

    // Пробуем выбрать слот
    @Nullable
    public Integer touchSlot(Point2 touchPoint) {
        for (int i = 0; i < slotGroup.getObjects().size(); i++) {
            Slot slot = slotGroup.getObject(i);
            if (slot.getBounds().contain(touchPoint)) {
                return i;
            }
        }

        return null;
    }

    public void selectSlot(int i) {
        slotGroup.getObject(i).select();
    }

    public void markAsPossibleProgress(int i) {
        slotGroup.getObject(i).hasAgressiveProgress();
    }

    @Nullable
    public Integer getSelectedSlot() {
        for (int i = 0; i < slotGroup.size(); i++) {
            Slot slot = slotGroup.getObject(i);
            if (slot.isSelected())
                return i;
        }

        return null;
    }

    public boolean possibleProgress(int i) {
        return slotGroup.getObject(i).isProgress();
    }

    public void progressSlot(int i) {
        slotGroup.getObject(i).progress();
    }

    public void releaseAllSlots() {
        for (Slot slot : slotGroup.getObjects())
            slot.release();
    }

    private Group<Slot> slotsConstruct(int startMargin, int topMargin, int hDelta, int vDelta, int radius) {
        Group<Slot> slots = Group.empty();

        for (int i = 0; i < countRows; i++) {
            for (int j = 0; j < countColumns; j++) {
                int x0 = startMargin + j*hDelta;
                int y0 = topMargin + i*vDelta;

                slotGenerator.setFixSize(radius*2, radius*2);
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
            Point2 center1 = slotGroup.getObject(way.getFrom())
                    .getBounds()
                    .getCenter();
            Point2 center2 = slotGroup.getObject(way.getTo())
                    .getBounds()
                    .getCenter();

            group.add(Way.of(center1, center2, wayColor));
        }

        return group;
    }


}
