package root.iv.ivplayer.game.fanorona;

import android.graphics.Canvas;

import androidx.annotation.Nullable;

import java.util.Objects;

import root.iv.ivplayer.game.fanorona.slot.Slot;
import root.iv.ivplayer.game.fanorona.slot.SlotState;
import root.iv.ivplayer.game.fanorona.slot.PairIndex;
import root.iv.ivplayer.game.fanorona.slot.Way;
import root.iv.ivplayer.game.fanorona.textures.FanoronaTextures;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.object.simple.Point2;
import root.iv.ivplayer.game.scene.SensorScene;
import root.iv.ivplayer.game.view.GameView;
import timber.log.Timber;

public class FanoronaScene extends SensorScene {
    private static final int DEFAULT_SIZE = 50;
    private static final int DEFAULT_RADIUS = 50;

    // Текстуры
    private FanoronaTextures textures;

    // Генераторы создания объектов
    private ObjectGenerator slotGenerator;
    private ObjectGenerator backgroundGenerator;
    private ObjectGenerator boardGenerator;

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

    public FanoronaScene(FanoronaTextures textures, int countRows, int countColumns, int startMargin, int topMargin, PairIndex[] ways) {
        super(new FanoronaController());
        this.textures = textures;

        // Стандартные размеры площадки
        this.countRows = countRows;
        this.countColumns = countColumns;

        // Генератор для фона
        backgroundGenerator = new ObjectGenerator();
        backgroundGenerator.setDrawable(textures.getBackgroundTextures().getBackground());

        boardGenerator = new ObjectGenerator();
        boardGenerator.setDrawable(textures.getBackgroundTextures().getBoard());

        // Генератор для сетки
        slotGenerator = new ObjectGenerator();
        slotGenerator.setDrawable(textures.getSlotTextures().getDrawable());
        slotGenerator.setTintColor(textures.getSlotTextures().getFreeColor());

        this.startMargin = startMargin;
        this.topMargin = topMargin;
        slotGroup = slotsConstruct(startMargin, topMargin, DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_RADIUS);
        wayGroup = wayConstruct(ways);
    }

    @Override
    public void render(Canvas canvas) {
        // Заливка фона
        if (backgroundGenerator.hasTexture()) {
            backgroundGenerator.setFixSize(canvas.getWidth(), canvas.getHeight());
            backgroundGenerator.buildStatic(0,0).render(canvas);

            // Поверх зелёного фона рисуем доску
            int boardStartMargin = 15;
            int boardTopMargin = 15;
            boardGenerator.setFixSize(canvas.getWidth() - 2*boardStartMargin, canvas.getHeight() - 2*boardTopMargin);
            boardGenerator.buildStatic(boardStartMargin,boardTopMargin).render(canvas);
        } else {
            canvas.drawColor(textures.getBackgroundTextures().getColor());
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
                    .mark(slotGroup.getObject(i));
        }
        slotGroup = resizedSlots;
    }


    public void resizeWay(PairIndex[] ways) {
        wayGroup = wayConstruct(ways);
    }

    // Загрузка в сцену состояния поля
    public void loadRoleState(FanoronaRole[][] roles) {
        int countRows = roles.length;
        int countColumns = roles[0].length;

        for (int i = 0; i < countRows; i++) {
            for (int j = 0; j < countColumns; j++) {
                markSlot(i*countColumns + j, roles[i][j]);
            }
        }
    }

    @Override
    public void connect(GameView gameView) {
        gameView.loadScene(this);
        gameView.setOnTouchListener(sensorController);
        gameView.setOnClickListener(sensorController);
    }

    public void markSlot(int index, FanoronaRole state) {
        slotGroup.getObject(index).mark(state);
    }

    // Пробуем выбрать слот
    @Nullable
    public Integer findSlot(Point2 touchPoint) {
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

    public void markForAttack(int i) {
        slotGroup.getObject(i).markForAttack();
    }

    public boolean markedForAttack(int i) {
        return slotGroup.getObject(i).getState() == SlotState.MARK_FOR_ATTACK;
    }

    @Nullable
    public Integer getSelectedSlot() {
        for (int i = 0; i < slotGroup.size(); i++) {
            Slot slot = slotGroup.getObject(i);
            if (slot.getState() == SlotState.SELECTED)
                return i;
        }

        return null;
    }

    public boolean possibleProgress(int i) {
        return slotGroup.getObject(i).getState() == SlotState.PROGRESS;
    }

    public void progressSlot(int i) {
        slotGroup.getObject(i).progress();
    }

    // Помечаем дорожку между двумя слотами
    public void useWay(int slot1, int slot2, int power) {
        Integer wayIndex = findWay(slotGroup.getObject(slot1), slotGroup.getObject(slot2));

        if (wayIndex != null) {
            Way way = wayGroup.getObject(wayIndex);
            way.used(power);
        }

    }

    public void releaseAllSlots() {
        slotGroup.getObjects()
                .forEach(Slot::release);
    }

    public void releaseAllWays() {
        wayGroup.getObjects().forEach(Way::release);
    }

    private Group<Slot> slotsConstruct(int startMargin, int topMargin, int hDelta, int vDelta, int radius) {
        Group<Slot> slots = Group.empty();

        for (int i = 0; i < countRows; i++) {
            for (int j = 0; j < countColumns; j++) {
                int x0 = startMargin + j*hDelta;
                int y0 = topMargin + i*vDelta;

                slotGenerator.setFixSize(radius*2, radius*2);
                StaticObject2 staticObject2 = slotGenerator.buildStatic(x0, y0);
                Slot slot = Slot.of(staticObject2, radius, textures.getChipTextures(), textures.getSlotTextures(), textures.getMarkAttackTextures());
                slots.add(slot);
            }
        }

        return slots;
    }

    private Group<Way> wayConstruct(PairIndex[] pairs) {
        Group<Way> group = Group.empty();

        for (PairIndex pair : pairs) {
            Point2 center1 = slotGroup.getObject(pair.getFrom())
                    .getBounds()
                    .getCenter();
            Point2 center2 = slotGroup.getObject(pair.getTo())
                    .getBounds()
                    .getCenter();

            int radius = slotGroup.getObject(0).getBounds().getRadius();

            group.add(Way.of(center1, center2, radius, textures.getWayTextures()));
        }

        return group;
    }

    // Ищем дорожку, соединяющую слоты
    @Nullable
    private Integer findWay(Slot slot1, Slot slot2) {

        for (int i = 0; i < wayGroup.size(); i++) {
            Way way = wayGroup.getObject(i);

            if (way.connect(slot1.getBounds().getCenter(), slot2.getBounds().getCenter()))
                return i;
        }


        return null;
    }
}
