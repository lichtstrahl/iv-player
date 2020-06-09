package root.iv.ivplayer.game.fanorona;

import android.graphics.Canvas;

import root.iv.ivplayer.game.fanorona.slot.Slot;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.scene.SensorScene;
import root.iv.ivplayer.game.view.GameView;
import timber.log.Timber;

public class FanoronaScene extends SensorScene {
    private static final int SLOT_RADIUS = 50;

    // Текстуры
    private FanoronaTextures textures;

    // Генераторы создания объектов
    private ObjectGenerator slotGenerator;
    private ObjectGenerator backgroundGenerator;

    // Группа: слоты под фишки
    private Group<Slot> slotGroup;

    // Размер матрицы
    private int countRows;
    private int countColumns;

    public FanoronaScene(FanoronaTextures textures, int countRows, int countColumns) {
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

        slotGroup = slotsConstruct(100, 100, 50, SLOT_RADIUS);
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

        // Отрисовка поля
        slotGroup.render(canvas);
    }

    @Override
    public void resize(int width, int height) {
        double k = 9.0/5.0;
        int horizontalMargin = 20;
        int verticalMargin = 10;

        // Масштабирование поля (слотов)
        if (height > width) {
            int gameW = width - horizontalMargin*2;
            int gameH = Math.round(width/(float)k) - verticalMargin*2;

            int widthElement = gameW / 17;
            int heightElement = gameH / 9;

            int size = Math.min(widthElement, heightElement);

            Timber.i("View: width %d, height %d", width, height);
            Timber.i("Game width: %d, height %d", gameW, gameH);
            Timber.i("wElement %d, hElement %d", widthElement, heightElement);
            Timber.i("Size: %d", size);

            Group<Slot> resizedSlots = slotsConstruct(horizontalMargin/2,verticalMargin/2, widthElement, heightElement, size/2);
            // Перенос старых состояний
            int count = slotGroup.size();
            for (int i = 0; i < count; i++) {
                resizedSlots.getObject(i)
                        .mark(slotGroup.getObject(i).getState());
            }
            slotGroup = resizedSlots;
        }


    }

    @Override
    public void connect(GameView gameView) {
        gameView.loadScene(this);
        gameView.setOnTouchListener(sensorController);
        gameView.setOnClickListener(sensorController);
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
}
