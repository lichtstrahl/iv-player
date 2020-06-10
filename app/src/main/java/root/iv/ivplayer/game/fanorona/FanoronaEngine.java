package root.iv.ivplayer.game.fanorona;

import android.view.MotionEvent;

import androidx.core.util.Consumer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import root.iv.ivplayer.game.fanorona.slot.SlotState;
import root.iv.ivplayer.game.fanorona.slot.SlotWay;
import root.iv.ivplayer.game.object.simple.Point2;
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
    @Getter
    @Setter
    private SlotState currentRole;

    public FanoronaEngine(FanoronaTextures textures, Consumer<MotionEvent> touchHandler) {
        slots = new SlotState[COUNT_ROW][COUNT_COLUMN];

        // Заполняем все слоты пустыми
        for (int i = 0; i < COUNT_ROW; i++) {
            Arrays.fill(slots[i], SlotState.FREE);
        }

        fillWays();

        // Создаём сцену
        this.scene = new FanoronaScene(textures, COUNT_ROW, COUNT_COLUMN, 10, 10, slotWays);
        this.scene.getSensorController().setTouchHandler(touchHandler);

        // Расставляем тылы BLACK
        for (int j = 0; j < COUNT_COLUMN; j++) {
            mark(4, j, SlotState.BLACK);
            mark(3, j, SlotState.BLACK);
        }

        // Расставляем тылы WHITE
        for (int j = 0; j < COUNT_COLUMN; j++) {
            mark(0, j, SlotState.WHITE);
            mark(1, j, SlotState.WHITE);
        }

        // Линия фронта (начиная с BLACK)
        for (int j = 0; j < COUNT_COLUMN; j++) {
            mark(2, j, (j%2) == 0 ? SlotState.WHITE : SlotState.BLACK);
        }

        // Очищаем серединку
        mark(2, 4, SlotState.FREE);
    }

    public void touch(float x, float y) {
        // Запоминаем прошлую выбранную ячейку и проверяем возможен ли ход в текущую.
        Integer selected = scene.getSelectedSlot();
        Integer touched = scene.touchSlot(Point2.point(x, y));
        boolean possibleProgress = (touched != null) && scene.possibleProgress(touched);

        scene.releaseAllSlots();


        // Было касание какого-то слота
        if (touched != null) {
            scene.selectSlot(touched);

            int ti = touched / COUNT_COLUMN;
            int tj = touched % COUNT_COLUMN;
            List<Integer> friends = findFriends(ti, tj);

            // Если нажатие было на свою фишку и соседняя фишка свободна, то она помечается под возможный ход
            if (slots[ti][tj] == currentRole) {
                for (int f : friends) {
                    int fi = f / COUNT_COLUMN;
                    int fj = f % COUNT_COLUMN;
                    if (slots[fi][fj] == SlotState.FREE)
                        scene.progressSlot(f);
                }
            }

            // Если в прошлый раз была выбрана своя фишка, а сейчас выбрана ячейка для хода
            if (selected != null && slots[selected/COUNT_COLUMN][selected%COUNT_COLUMN] == currentRole && possibleProgress) {
                progress(selected, touched);
            }
        }


    }

    public void connect(GameView gameView) {
        scene.connect(gameView);
    }

    public void resize(int width, int height) {
        scene.resize(width, height);
    }

    private void fillWays() {
        slotWays = new SlotWay[108];
        int index = 0;

        // Заполняем строки
        for (int i = 0; i < COUNT_ROW; i++) {
            for (int j = 0; j < COUNT_COLUMN-1; j++)
                slotWays[index++] = new SlotWay(i,j, i, j+1);
        }

        // Заполняем столбцы
        for (int j = 0; j < COUNT_COLUMN; j++) {
            for (int i = 0; i < COUNT_ROW-1; i++)
                slotWays[index++] = new SlotWay(i,j, i+1, j);
        }

        // Заполняем диагонали
        for (int i = 1; i < COUNT_ROW; i += 2) {
            for (int j = 1; j < COUNT_COLUMN; j += 2) {
                // Нужно соединить эту вершину со всеми возможными диагоналями (центр паука). Рисуем лапки
                slotWays[index++] = new SlotWay(i,j, i-1,j-1);
                slotWays[index++] = new SlotWay(i,j, i-1,j+1);
                slotWays[index++] = new SlotWay(i,j, i+1,j-1);
                slotWays[index++] = new SlotWay(i,j, i+1, j+1);
            }
        }

        if (index != 108)
            throw new IllegalStateException("Массив дорожек заполнен не до конца");
    }

    private void mark(int i, int j, SlotState state) {
        slots[i][j] = state;
        scene.markSlot(i*COUNT_COLUMN + j, state);
    }

    private void mark(int globalIndex, SlotState state) {
        mark(globalIndex / COUNT_COLUMN, globalIndex % COUNT_COLUMN, state);
    }

    // Находим соседние слоты
    private List<Integer> findFriends(int i, int j) {
        List<Integer> friends = new LinkedList<>();

        for (SlotWay way : slotWays) {
            if (way.connect(i, j)) {
                friends.add(way.iFriend(i)*COUNT_COLUMN + way.jFriend(j));
            }
        }

        return friends;
    }

    private void progress(int oldIndex, int newIndex) {
        mark(oldIndex, SlotState.FREE);
        mark(newIndex, currentRole);
    }

    @Data
    @AllArgsConstructor
    class Indexes {
        private int i;
        private int j;
    }
}
