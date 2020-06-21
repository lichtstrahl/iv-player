package root.iv.ivplayer.game.fanorona;

import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import root.iv.bot.Move;
import root.iv.ivplayer.game.fanorona.dto.FanoronaProgressDTO;
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
    private FanoronaRole[][] slots;
    private SlotWay[] slotWays;
    private FanoronaScene scene;
    @Getter
    @Setter
    private FanoronaRole currentRole;
    @Getter
    private List<FanoronaProgressDTO> progressSteps;
    @Getter
    private boolean endSteps;
    private boolean aggressiveStep;

    public FanoronaEngine(FanoronaTextures textures, Consumer<MotionEvent> touchHandler) {
        slots = new FanoronaRole[COUNT_ROW][COUNT_COLUMN];

        // Заполняем все слоты пустыми
        for (int i = 0; i < COUNT_ROW; i++) {
            Arrays.fill(slots[i], FanoronaRole.FREE);
        }

        fillWays();

        // Создаём сцену
        this.scene = new FanoronaScene(textures, COUNT_ROW, COUNT_COLUMN, 10, 10, slotWays);
        this.scene.getSensorController().setTouchHandler(touchHandler);

        // Расставляем тылы BLACK
        for (int j = 0; j < COUNT_COLUMN; j++) {
            mark(4, j, FanoronaRole.BLACK);
            mark(3, j, FanoronaRole.BLACK);
        }

        // Расставляем тылы WHITE
        for (int j = 0; j < COUNT_COLUMN; j++) {
            mark(0, j, FanoronaRole.WHITE);
            mark(1, j, FanoronaRole.WHITE);
        }

        // Линия фронта (Слева начиная с WHITE, Справа начиная с BLACK)
        for (int j = 0; j < COUNT_COLUMN; j++) {
            mark(2, j, (j%2) == 0 ? FanoronaRole.WHITE : FanoronaRole.BLACK);
        }

        for (int left = 0, right = COUNT_COLUMN-1; left != right; left++, right--) {
            mark(2, left, (left%2) == 0 ? FanoronaRole.WHITE : FanoronaRole.BLACK);
            mark(2, right, (right%2) == 0 ? FanoronaRole.BLACK : FanoronaRole.WHITE);
        }

        // Очищаем серединку
        mark(2, 4, FanoronaRole.FREE);

        // Последовательность ходов (изначально пустая)
        progressSteps = new ArrayList<>(10);
    }

    @Nullable
    public FanoronaProgressDTO touch(float x, float y) {
        this.endSteps = false;
        // Запоминаем прошлую выбранную ячейку и проверяем возможен ли ход в текущую.
        Integer selected = scene.getSelectedSlot();
        Integer touched = scene.touchSlot(Point2.point(x, y));
        boolean possibleProgress = touched != null && scene.possibleProgress(touched);

        // Если касание было вне поля и последовательность ходов завершена, то отметки сбрасываются
        if (touched == null && progressSteps.isEmpty()) {
            scene.releaseAllSlots();
            markPossibleProgress(currentRole);
            return null;
        }

        // Последовательность ходов не начата, ход невозможен.
        if (progressSteps.isEmpty() && !possibleProgress) {
            scene.releaseAllSlots();
            Timber.i("Коснулись ячейки. step=0, помечаем её как возможное начало для хода");
            prepareProgress(touched);
            return null;
        }


        // Если в прошлый раз была выбрана своя фишка, а сейчас выбрана ячейка для хода
        if (selected != null && getState(selected) == currentRole && possibleProgress) {
            FanoronaProgressDTO progressDTO = progress(selected, touched, currentRole);
            // Если это ход текущего игрока
            progressSteps.add(progressDTO);
            Timber.i("Ход, step: #%d ->%d", progressDTO.getFrom(), progressDTO.getTo());


            scene.releaseAllSlots();
            // Если после выполнения хода агрессивных ходов больше нет или сам ход был не агрессивным,
            // то завершаем последовательность ходов
            if (findAgressiveProgress(touched).isEmpty() || !aggressiveStep) {
                Timber.i("Агрессивные ходы кончились. step=0");
                this.endSteps = true;
            } else { // Если агрессивная последовательность может продолжаться, то нужно пометить
                Timber.i("Агрессивные ходы продолжаются step: %d", progressSteps.size());
                prepareProgress(touched);
            }

            return progressDTO;
        }

        return null;
    }

    // Пометить фишки с возможными ходами. Для данной роли
    public void markPossibleProgress(FanoronaRole role) {
        List<Integer> possibleSlots = hasAgressiveProgress(role)
                ? listSlotsWithAgressiveProgress(role)
                : listSlotsHasFreeFriend(role);

        for (int i : possibleSlots)
            scene.markAsPossibleProgress(i);

    }

    public Move getMove() {
        Move startMove = move(progressSteps.get(0));

        for (int i = 1; i < progressSteps.size(); i++) {
            Move next = move(progressSteps.get(i));
            startMove.next(next);
        }

        progressSteps.clear();

        return startMove;
    }

    public List<FanoronaProgressDTO> parse(Move move) {
        List<FanoronaProgressDTO> progress = new ArrayList<>();
        FanoronaRole role = enemyRoleFor(currentRole);

        for (Move cur = move; cur != null; cur = cur.cont) {
            int fromIndex = cur.y * COUNT_COLUMN + cur.x;

            int toX = cur.toX();
            int toY = cur.toY();
            int toIndex = toY * COUNT_COLUMN + toX;

            FanoronaProgressDTO p = new FanoronaProgressDTO(role, fromIndex, toIndex);
            progress.add(p);
        }

        return progress;
    }

    private Move move(FanoronaProgressDTO progress) {
        return move(progress.getFrom(), progress.getTo());
    }

    private Move move(int from, int to) {
        int row0 = row(from);
        int column0 = column(from);

        int rowTo = row(to);
        int columnTo = column(to);

        return new Move(row0, column0, rowTo-row0, columnTo-column0);
    }

    private void prepareProgress(Integer touched) {
        scene.selectSlot(touched);

        // Если это пустая ячейка или чужая для нас фишка, то больше ничего не делаем
        if (isFree(touched) || isEnemy(touched, currentRole))
            return;

        // Если рядом нет свободных друзей, то ход очевидно невозможен
        if (findFreeFriends(touched).isEmpty())
            return;

        // Пробуем нарисовать возможные агрессивные ходы:
        List<Integer> aggressiveProgress = findAgressiveProgress(touched);
        for (Integer progress : aggressiveProgress) {
            aggressiveStep = true;
            scene.progressSlot(progress);
        }

        // Если агрессивных ходов из этой позиции нет и для роли их больше не существует вообще
        // + это начало цепочки, то можно просто походить на пустую клетку
        if (aggressiveProgress.isEmpty() && progressSteps.isEmpty() && !hasAgressiveProgress(currentRole)) {
            List<Integer> freeFriends = findFreeFriends(touched);

            for (Integer free : freeFriends) {
                aggressiveStep = false;
                scene.progressSlot(free);
            }
        }
    }

    public void connect(GameView gameView) {
        scene.connect(gameView);
    }

    public void resize(int width, int height) {
        scene.resize(width, height);
        scene.resizeWay(slotWays);
    }

    // Победа, если у нас ещё есть фишки, а у соперника они кончились
    public boolean win() {
        return !listSlotsForRole(currentRole).isEmpty() && listSlotsForRole(enemyRoleFor(currentRole)).isEmpty();
    }

    // Конец игры наступает, если нет больше фишек у одного из игроков
    public boolean end() {
        return listSlotsForRole(currentRole).isEmpty() || listSlotsForRole(enemyRoleFor(currentRole)).isEmpty();
    }

    private void fillWays() {
        slotWays = new SlotWay[108];
        int index = 0;

        // Заполняем строки
        for (int i = 0; i < COUNT_ROW; i++) {
            for (int j = 0; j < COUNT_COLUMN-1; j++)
                slotWays[index++] = SlotWay.column9(i,j, i, j+1);
        }

        // Заполняем столбцы
        for (int j = 0; j < COUNT_COLUMN; j++) {
            for (int i = 0; i < COUNT_ROW-1; i++)
                slotWays[index++] = SlotWay.column9(i,j, i+1, j);
        }

        // Заполняем диагонали
        for (int i = 1; i < COUNT_ROW; i += 2) {
            for (int j = 1; j < COUNT_COLUMN; j += 2) {
                // Нужно соединить эту вершину со всеми возможными диагоналями (центр паука). Рисуем лапки
                slotWays[index++] = SlotWay.column9(i,j, i-1,j-1);
                slotWays[index++] = SlotWay.column9(i,j, i-1,j+1);
                slotWays[index++] = SlotWay.column9(i,j, i+1,j-1);
                slotWays[index++] = SlotWay.column9(i,j, i+1, j+1);
            }
        }

        if (index != 108)
            throw new IllegalStateException("Массив дорожек заполнен не до конца");
    }

    private void mark(int i, int j, FanoronaRole state) {
        slots[i][j] = state;
        scene.markSlot(i*COUNT_COLUMN + j, state);
    }

    private void mark(int globalIndex, FanoronaRole state) {
        mark(row(globalIndex), column(globalIndex), state);
    }

    private List<Integer> findFriends(int globalIndex) {
        List<Integer> friends = new LinkedList<>();

        for (SlotWay way : slotWays) {
            if (way.connect(globalIndex)) {
                friends.add(way.friend(globalIndex));
            }
        }

        return friends;
    }

    private List<Integer> findFreeFriends(int globalIndex) {
        return findFriends(globalIndex)
                .stream()
                .filter(i -> getState(i) == FanoronaRole.FREE)
                .collect(Collectors.toList());
    }

    private List<Integer> findEnemyFriends(int globalIndex) {
        return findFriends(globalIndex)
                .stream()
                .filter(i -> getState(i) == enemyRoleFor(currentRole))
                .collect(Collectors.toList());
    }

    public FanoronaProgressDTO progress(int oldIndex, int newIndex, FanoronaRole state) {
        Timber.i("Ход %s: %d -> %d  ([%d][%d])->([%d][%d])",
                state.name(), oldIndex, newIndex,
                row(oldIndex), column(oldIndex), row(newIndex), column(newIndex));
        // Сразу формируем итоговый ответ о ходе. Т.к. индексы потом будут изменяться.
        FanoronaProgressDTO progressDTO = new FanoronaProgressDTO(state, oldIndex, newIndex);

        mark(oldIndex, FanoronaRole.FREE);
        mark(newIndex, state);

        // Убираем всех соперников по линии, пока не дойдём до конца поля или не встретим пустую клетку
        for (Integer nextSlot = nextSlotForLine(oldIndex, newIndex); nextSlot != null && isEnemy(nextSlot, state); nextSlot = nextSlotForLine(oldIndex, newIndex)) {
                mark(nextSlot, FanoronaRole.FREE);
                oldIndex = newIndex;
                newIndex = nextSlot;
        }

        // Убираем всех соперников по обратной линии
        for (Integer nextSlot = nextSlotForLine(newIndex, oldIndex); nextSlot != null && isEnemy(nextSlot, state); nextSlot = nextSlotForLine(newIndex, oldIndex)) {
            mark(nextSlot, FanoronaRole.FREE);
            newIndex = oldIndex;
            oldIndex = nextSlot;
        }

        return progressDTO;
    }

    /**
        Фишка имеет агрессивные ходы: (это должна быть наша фишка)
        Два типа агрессии:
        1. Среди друзей есть фишки соперника, по обраткой линии от этого соперника есть свободная клетка
        2. Среди друзей естьсвободная клетка, по линии этой клетки есть соперник.

    */
    private List<Integer> findAgressiveProgress(int to) {
        List<Integer> aggressiveProgress = new LinkedList<>();

        if (getState(to) != currentRole)
            return aggressiveProgress;

        // Перебираем свободных друзей и смотрим есть ли на линии фишка соперника
        List<Integer> freeFriends = findFreeFriends(to);
        for (Integer freeFriend : freeFriends) {
            Integer nextSlot = nextSlotForLine(to, freeFriend);

            if (nextSlot != null && getState(nextSlot) == enemyRoleFor(currentRole)) {
                aggressiveProgress.add(freeFriend);
            }
        }

        // Перебираем фишки противника среди друзей
        List<Integer> enemyFriends = findEnemyFriends(to);
        for (Integer enemyFriend : enemyFriends) {
            Integer nextSlot = nextSlotForLine(enemyFriend, to);

            if (nextSlot != null && getState(nextSlot) == FanoronaRole.FREE) {
                aggressiveProgress.add(nextSlot);
            }
        }


        // Из всех возможных агрессивных ходов выбираем только те, что не продолжают линию (last->to)
        // И те что не содержатся среди уже совершенных ходов
        // last - ячейка, где сейчас стоит фишка.
        FanoronaProgressDTO lastProgress = progressSteps.isEmpty()
                ? null
                : progressSteps.get(progressSteps.size()-1);

        return aggressiveProgress
                .stream()
                .filter(progress -> {
                    Integer next = (lastProgress != null) ? nextSlotForLine(lastProgress.getFrom(), to) : null;
                    return next == null || !next.equals(progress);
                })
                .filter(progress ->
                        !progressSteps
                            .stream()
                            .map(FanoronaProgressDTO::getFrom)
                            .collect(Collectors.toList())
                            .contains(progress)
                )
                .collect(Collectors.toList());
    }

    @Nullable
    private Integer nextSlotForLine(int startIndex, int endIndex) {
        int deltaI = row(endIndex) - row(startIndex);
        int deltaJ = column(endIndex) - column(startIndex);

        int nextI = row(endIndex) + deltaI;
        int nextJ = column(endIndex) + deltaJ;

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

    private FanoronaRole enemyRoleFor(FanoronaRole role) {
        return (role == FanoronaRole.BLACK)
                ? FanoronaRole.WHITE
                : FanoronaRole.BLACK;
    }

    private boolean isFree(int globalIndex) {
        return getState(globalIndex) == FanoronaRole.FREE;
    }

    private boolean isEnemy(int globalIndex, FanoronaRole role) {
        return getState(globalIndex) == enemyRoleFor(role);
    }

    // Имеет ли указанная роль агрессивные ходы?
    private boolean hasAgressiveProgress(FanoronaRole role) {
        return !listSlotsWithAgressiveProgress(role).isEmpty();
    }

    // Список фишек, имеющих агрессивные ходы. Для указанной роли.
    private List<Integer> listSlotsWithAgressiveProgress(FanoronaRole role) {
        int totalCountSlots = COUNT_ROW * COUNT_COLUMN;
        List<Integer> potentialSlots = new ArrayList<>(totalCountSlots);

        // Перебираем все фишки. Ищем фишки для указанной роли и если для неё есть агрессивные ходы добавляем в итоговый список
        for (int i = 0; i < totalCountSlots; i++) {
            if (getState(i) == role && !findAgressiveProgress(i).isEmpty()) {
                potentialSlots.add(i);
            }
        }

        return potentialSlots;
    }

    // Список всех фишек для данной роли
    private List<Integer> listSlotsForRole(FanoronaRole role) {
        int totalCountSlots = COUNT_ROW * COUNT_COLUMN;
        List<Integer> slots = new ArrayList<>(totalCountSlots);

        for (int i = 0; i < totalCountSlots; i++)
            if (getState(i) == role)
                slots.add(i);

        return slots;
    }

    // Список фишек, имеющих рядом свободную клетку
    private List<Integer> listSlotsHasFreeFriend(FanoronaRole role) {
        List<Integer> allSlotsForRole = listSlotsForRole(role);

        return allSlotsForRole
                .stream()
                .filter(index -> {
                    List<Integer> friends = findFreeFriends(index);
                    return !friends.isEmpty();
                })
                .collect(Collectors.toList());
    }

    private FanoronaRole getState(int globalInex) {
        return slots[row(globalInex)][column(globalInex)];
    }

    private int row(int globalIndex) {
        return globalIndex / COUNT_COLUMN;
    }

    private int column(int globalIndex) {
        return globalIndex % COUNT_COLUMN;
    }
}
