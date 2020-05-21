package root.iv.ivplayer.game.fanorona;

import android.graphics.Canvas;

import root.iv.ivplayer.game.fanorona.slot.Slot;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.scene.SensorScene;
import root.iv.ivplayer.game.view.GameView;

public class FanoronaScene extends SensorScene {
    // Текстуры
    private FanoronaTextures textures;

    // Генераторы создания объектов
    private ObjectGenerator slotGenerator;
    private ObjectGenerator backgroundGenerator;

    // Группа: слоты под фишки
    private Group<Slot> slotGroup;

    public FanoronaScene(FanoronaTextures textures) {
        super(new FanoronaController());
        this.textures = textures;

        // Генератор для фона
        backgroundGenerator = new ObjectGenerator();
        backgroundGenerator.setDrawable(textures.getBackground());
        backgroundGenerator.setTintColor(textures.getBackgroundColor());

        // Генератор для сетки
        slotGenerator = new ObjectGenerator();
        slotGenerator.setDrawable(textures.getSlot());
        slotGenerator.setTintColor(textures.getSlotColor());
        slotGenerator.setFixSize(100, 100);
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

    }

    @Override
    public void connect(GameView gameView) {

    }

    private Group<Slot> slotsConstruct(int startMargin, int topMargin, int delta, int radius) {
        Group<Slot> slots = Group.empty();


        return slots;
    }
}
