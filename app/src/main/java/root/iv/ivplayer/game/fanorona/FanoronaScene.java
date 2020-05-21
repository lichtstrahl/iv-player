package root.iv.ivplayer.game.fanorona;

import android.graphics.Canvas;

import root.iv.ivplayer.game.fanorona.slot.Slot;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.scene.SensorScene;
import root.iv.ivplayer.game.view.GameView;

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
        // TODO РЕализовать динамическое изменение размеров сцены под экран (TicTacScene)
    }

    @Override
    public void connect(GameView gameView) {
        gameView.loadScene(this);
        gameView.setOnTouchListener(sensorController);
        gameView.setOnClickListener(sensorController);
    }

    private Group<Slot> slotsConstruct(int startMargin, int topMargin, int delta, int radius) {
        Group<Slot> slots = Group.empty();

        for (int i = 0; i < countRows; i++) {
            for (int j = 0; j < countColumns; j++) {
                int x0 = startMargin + j*(delta+radius*2);
                int y0 = topMargin + i*(delta+radius*2);

                StaticObject2 staticObject2 = slotGenerator.buildStatic(x0, y0);
                Slot slot = Slot.of(staticObject2, radius, textures.getChipWhite(), textures.getChipWhiteColor(),
                        textures.getChipBlack(), textures.getChipBlackColor());
                slots.add(slot);
            }
        }

        return slots;
    }
}
