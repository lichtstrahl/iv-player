package root.iv.ivplayer.game.fanorona;

import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import root.iv.ivplayer.game.fanorona.dto.FanoronaProgressDTO;
import root.iv.ivplayer.game.fanorona.slot.SlotState;
import root.iv.ivplayer.game.fanorona.slot.SlotWay;
import root.iv.ivplayer.game.object.simple.Point2;
import root.iv.ivplayer.game.view.GameView;
import timber.log.Timber;

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
    private int progressStep;

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

    @Nullable
    public FanoronaProgressDTO touch(float x, float y) {
        // Запоминаем прошлую выбранную ячейку и проверяем возможен ли ход в текущую.
        Integer selected = scene.getSelectedSlot();
        Integer touched = scene.touchSlot(Point2.point(x, y));
        boolean possibleProgress = touched != null && scene.possibleProgress(touched);

        // Если касание было вне поля и последовательность ходов завершена, то отметки сбрасываются
        if (touched == null && progressStep == 0) {
            scene.releaseAllSlots();
            return null;
        }

        // Было касание какого-то слота, последовательность ходов не начата, ход невозможен.
        if (progressStep == 0 && !possibleProgress) {
            scene.releaseAllSlots();
            Timber.i("Коснулись ячейки. step=0, помечаем её как возможное начало для хода");
            prepareProgress(null, touched);
        }


        // Если в прошлый раз была выбрана своя фишка, а сейчас выбрана ячейка для хода
        if (selected != null && slots[selected/COUNT_COLUMN][selected%COUNT_COLUMN] == currentRole && possibleProgress) {
            progress(selected, touched, currentRole);


            scene.releaseAllSlots();
            // Если после выполнения хода агрессивных ходов больше нет, то завершаем последовательность ходов
            if (findAgressiveProgress(selected, touched).isEmpty()) {
                Timber.i("Агрессивные ходы кончились. step=0");
                progressStep = 0;
            } else { // Если агрессивная последовательность может продолжаться, то нужно пометить
                Timber.i("Агрессивные ходы продолжаются step: %d", progressStep);
                prepareProgress(selected, touched);
            }
        }

        return null;
    }

    private void prepareProgress(@Nullable Integer selected, Integer touched) {
        scene.selectSlot(touched);
        // Пробуем нарисовать возможные агрессивные ходы:
        List<Integer> aggressiveProgress = findAgressiveProgress(selected, touched);
        for (Integer progress : aggressiveProgress) {
            scene.progressSlot(progress);
        }

        // Если агрессивных ходов нет, а это начало цепочки, то можно просто походить на пустую клетку
        if (aggressiveProgress.isEmpty() && progressStep == 0) {
            List<Integer> freeFriends = findFreeFriends(touched);

            for (Integer free : freeFriends)
                scene.progressSlot(free);
        }
    }

    public void connect(GameView gameView) {
        scene.connect(gameView);
    }

    public void resize(int width, int height) {
        scene.resize(width, height);
    }

    public boolean win() {
        return false;
    }

    public boolean end() {
        return false;
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

    private List<Integer> findFriends(int globalIndex) {
        return findFriends(globalIndex/COUNT_COLUMN, globalIndex%COUNT_COLUMN);
    }

    private List<Integer> findFreeFriends(int globalIndex) {
        return findFriends(globalIndex)
                .stream()
                .filter(i -> slots[i/COUNT_COLUMN][i%COUNT_COLUMN] == SlotState.FREE)
                .collect(Collectors.toList());
    }

    private List<Integer> findEnemyFriends(int globalIndex) {
        return findFriends(globalIndex)
                .stream()
                .filter(i -> slots[i/COUNT_COLUMN][i%COUNT_COLUMN] == enemyRoleFor(currentRole))
                .collect(Collectors.toList());
    }

    public FanoronaProgressDTO progress(int oldIndex, int newIndex, SlotState state) {
        mark(oldIndex, SlotState.FREE);
        mark(newIndex, state);

        // Если это ход текущего игрока
        if (state == currentRole) {
            progressStep++;
            Timber.i("Ход, step:  %d", progressStep);
        }

        // Убираем всех соперников по линии, пока не дойдём до конца поля или не встретим пустую клетку
        for (Integer nextSlot = nextSlotForLine(oldIndex, newIndex); nextSlot != null && isEnemy(nextSlot, state); nextSlot = nextSlotForLine(oldIndex, newIndex)) {
                mark(nextSlot, SlotState.FREE);
                oldIndex = newIndex;
                newIndex = nextSlot;
        }

        // Убираем всех соперников по обратной линии
        for (Integer nextSlot = nextSlotForLine(newIndex, oldIndex); nextSlot != null && isEnemy(nextSlot, state); nextSlot = nextSlotForLine(newIndex, oldIndex)) {
            mark(nextSlot, SlotState.FREE);
            newIndex = oldIndex;
            oldIndex = nextSlot;
        }

        return new FanoronaProgressDTO(state, oldIndex, newIndex);
    }

    /**
        Фишка имеет агрессивные ходы: (это должна быть наша фишка)
        Два типа агрессии:
        1. Среди друзей есть фишки соперника, по обраткой линии от этого соперника есть свободная клетка
        2. Среди друзей естьсвободная клетка, по линии этой клетки есть соперник.
    */
    private List<Integer> findAgressiveProgress(@Nullable Integer from, int to) {
        List<Integer> aggressiveProgress = new LinkedList<>();

        if (slots[to/COUNT_COLUMN][to%COUNT_COLUMN] != currentRole)
            return aggressiveProgress;

        // Перебираем свободных друзей и смотрим есть ли на линии фишка соперника
        List<Integer> freeFriends = findFreeFriends(to);
        for (Integer freeFriend : freeFriends) {
            Integer nextSlot = nextSlotForLine(to, freeFriend);

            if (nextSlot != null && slots[nextSlot/COUNT_COLUMN][nextSlot%COUNT_COLUMN] == enemyRoleFor(currentRole)) {
                aggressiveProgress.add(freeFriend);
            }
        }

        // Перебираем фишки противника среди друзей
        List<Integer> enemyFriends = findEnemyFriends(to);
        for (Integer enemyFriend : enemyFriends) {
            Integer nextSlot = nextSlotForLine(enemyFriend, to);

            if (nextSlot != null && slots[nextSlot/COUNT_COLUMN][nextSlot%COUNT_COLUMN] == SlotState.FREE) {
                aggressiveProgress.add(nextSlot);
            }
        }


        // Из всех возможных агрессивных ходов выбираем только те, что не продолжают линию (from->to)
        return aggressiveProgress
                .stream()
                .filter(progress -> {
                    Integer next = (from != null) ? nextSlotForLine(from, to) : null;
                    return next == null || !next.equals(progress);
                })
                .collect(Collectors.toList());
    }

    @Nullable
    private Integer nextSlotForLine(int startIndex, int endIndex) {
        int deltaI = endIndex/COUNT_COLUMN - startIndex/COUNT_COLUMN;
        int deltaJ = endIndex%COUNT_COLUMN - startIndex%COUNT_COLUMN;

        int nextI = endIndex/COUNT_COLUMN + deltaI;
        int nextJ = endIndex%COUNT_COLUMN + deltaJ;

        if (correctI(nextI) && correctJ(nextJ))
            return nextI*COUNT_COLUMN + nextJ;
        else
            return null;
    }

    private boolean correctI(int i) {
        return i >= 0 && i < COUNT_ROW;
    }

    private boolean correctJ(int j) {
        return j >= 0 && j < COUNT_COLUMN;
    }

    private SlotState enemyRoleFor(SlotState role) {
        return (role == SlotState.BLACK)
                ? SlotState.WHITE
                : SlotState.BLACK;
    }

    private boolean isFree(int globalIndex) {
        return slots[globalIndex/COUNT_COLUMN][globalIndex%COUNT_COLUMN] == SlotState.FREE;
    }

    private boolean isEnemy(int globalIndex, SlotState role) {
        return slots[globalIndex/COUNT_COLUMN][globalIndex%COUNT_COLUMN] == enemyRoleFor(role);
    }

    @Data
    @AllArgsConstructor
    class Indexes {
        private int i;
        private int j;
    }
}
