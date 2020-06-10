package root.iv.ivplayer.game.fanorona;

import android.view.MotionEvent;

import androidx.core.util.Consumer;

import java.util.Arrays;

import root.iv.ivplayer.game.fanorona.slot.SlotState;
import root.iv.ivplayer.game.fanorona.slot.SlotWay;
import root.iv.ivplayer.game.view.GameView;

/**
 * Игровой движок фанороны
 */
public class FanoronaEngine {
    private static final int COUNT_ROW = 5;
    private static final int COUNT_COLUMN = 9;
    private SlotState[][] slots;
    private SlotWay[] slotWays;
    private FanoronaScene scene;

    public FanoronaEngine(FanoronaTextures textures, Consumer<MotionEvent> touchHandler) {
        slots = new SlotState[COUNT_ROW][COUNT_COLUMN];

        // Заполняем все слоты пустыми
        for (int i = 0; i < COUNT_ROW; i++) {
            Arrays.fill(slots[i], SlotState.FREE);
        }

        // Пустой массив связей
        slotWays = new SlotWay[0];

        // Создаём сцену
        this.scene = new FanoronaScene(textures, COUNT_ROW, COUNT_COLUMN, 10, 10);
        this.scene.getSensorController().setTouchHandler(touchHandler);

        // Первая строка BLACK
        for (int j = 0; j < COUNT_COLUMN; j++)
            mark(0, j, SlotState.BLACK);

        // Третья строка WHITE
        for (int j = 0; j < COUNT_COLUMN; j++)
            mark(2, j, SlotState.WHITE);
    }

    public void connect(GameView gameView) {
        scene.connect(gameView);
    }

    public void resize(int width, int height) {
        scene.resize(width, height);
    }

    private void mark(int i, int j, SlotState state) {
        slots[i][j] = state;
        scene.markSlot(i*COUNT_COLUMN + j, state);
    }
}
