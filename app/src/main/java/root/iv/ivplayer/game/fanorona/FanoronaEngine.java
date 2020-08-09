package root.iv.ivplayer.game.fanorona;

import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import root.iv.bot.Progress;
import root.iv.ivplayer.game.fanorona.board.BoardGenerator;
import root.iv.ivplayer.game.fanorona.dto.FanoronaProgressDTO;
import root.iv.ivplayer.game.fanorona.slot.PairIndex;
import root.iv.ivplayer.game.fanorona.textures.FanoronaTextures;
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
    private PairIndex[] pairIndices;
    private FanoronaScene scene;
    @Getter
    @Setter
    private FanoronaRole currentRole;
    private ProgressChain<FanoronaProgressDTO> progressChain;

    public FanoronaEngine(FanoronaTextures textures, Consumer<MotionEvent> touchHandler) {
        BoardGenerator boardGenerator = BoardGenerator.newGenerator(COUNT_COLUMN, COUNT_ROW);

        pairIndices = boardGenerator.ways();
        slots = boardGenerator.testDoubleAttack();

        // Создаём сцену
        scene = new FanoronaScene(textures, COUNT_ROW, COUNT_COLUMN, 10, 10, pairIndices);
        scene.getSensorController().setTouchHandler(touchHandler);
        scene.loadRoleState(slots);



        // Последовательность ходов (изначально пустая)
        progressChain = ProgressChain.empty();
    }

    @Nullable
    public FanoronaProgressDTO touch(float x, float y) {
        // Запоминаем прошлую выбранную ячейку и проверяем возможен ли ход в текущую.
        Integer selected = scene.getSelectedSlot();
        Integer touched = scene.findSlot(Point2.point(x, y));
        boolean possibleProgress = touched != null && scene.possibleProgress(touched);

        // Если возможен ход в двух направлениях, то касание никак не обрабатывается
        if (possibleDoubleAttack(selected, touched))
            return null;

        // Если касание было вне поля и последовательность ходов завершена, то отметки сбрасываются
        if (touched == null && progressChain.isEmpty()) {
            scene.releaseAllSlots();
            markPossibleProgress(currentRole);
            return null;
        }

        // Последовательность ходов не начата, ход невозможен.
        if (progressChain.isEmpty() && !possibleProgress) {
            scene.releaseAllSlots();
            scene.releaseAllWays();
            Timber.i("Коснулись ячейки. step=0, помечаем её как возможное начало для хода");
            prepareProgress(touched);
            return null;
        }


        // Если в прошлый раз была выбрана своя фишка, а сейчас выбрана ячейка для хода
        if (selected != null && getState(selected) == currentRole && possibleProgress) {
            return step(selected, touched, null);
        }

        return null;
    }

    // Касание при выборе направления атаки: гарантировано должен остаться с прошлого раза selected
    // Происходит выбор направления атаки (направление ход не меняется)
    public FanoronaProgressDTO selectAttackType(float x, float y) {
        int selected = Objects.requireNonNull(scene.getSelectedSlot());
        Integer touched = scene.findSlot(Point2.point(x, y));

        // Если слот найден и он помечен под атаку
        if (Objects.nonNull(touched) && scene.markedForAttack(touched)) {
            Integer friend = friendOnDirection(selected, touched);

            // Если друг свободен, значит это FORWARD-атака, иначе BACK
            // Если это FORWARD-атака, то ходим в направлении друга (selected -> friend, to (enemy))
            // Если это BACK-атака, то ходим в обратном направлении (friend(enemy), selected -> to(free))
            return isFree(friend)
                    ? step(selected, friend, AttackType.FORWARD)
                    : step(selected, nextSlotForLine(friend, selected), AttackType.BACK);
        }

        return null;
    }

    public boolean endProgressChain() {
        return progressChain.isEnd();
    }

    // Своя фишка, Такой ход допустим, Возможны два направления атаки
    public boolean possibleDoubleAttack(float x, float y) {
        Integer from = scene.getSelectedSlot();
        Integer to = scene.findSlot(Point2.point(x, y));

        return possibleDoubleAttack(from, to);
    }

    // Гарантировано есть выбранная и тронутая ячейка
    public void markSlotsForAttack(float x, float y) {
        int selected = Objects.requireNonNull(scene.getSelectedSlot());
        int to = Objects.requireNonNull(scene.findSlot(Point2.point(x, y)));

        // Перебираем все слоты по линии и помечаем их под атаку
        // (пока подряд идут фишки соперника или пока не кончится доска)
        nextSlotsForLine(selected, to, cur -> cur != null && isEnemy(cur, currentRole))
                .forEach(scene::markForAttack);


        // Перебираем все слоты по обратной линии и помечаем их под атаку
        // (пока подряд идут фишки соперника или пока не кончится доска
        nextSlotsForLine(to, selected, cur -> cur != null && isEnemy(cur, currentRole))
                .forEach(scene::markForAttack);
    }

    // Пометить фишки с возможными ходами. Для данной роли
    public void markPossibleProgress(FanoronaRole role) {
        List<Integer> possibleSlots = hasAgressiveProgress(role)
                ? listSlotsWithAgressiveProgress(role)
                : listSlotsHasFreeFriend(role);

        for (int i : possibleSlots)
            scene.markAsPossibleProgress(i);

    }

    public void processProgressChain(Consumer<FanoronaProgressDTO> consumer) {
        progressChain.process(consumer);
    }

    public List<Progress> getMove() {
        List<Progress> progresses = new ArrayList<>();

        progressChain.process(
                fp -> progresses.add(fp.export())
        );

        return progresses;
    }

    public void connect(GameView gameView) {
        scene.connect(gameView);
    }

    public void resize(int width, int height) {
        scene.resize(width, height);
        scene.resizeWay(pairIndices);
    }

    // Победа, если у нас ещё есть фишки, а у соперника они кончились

    public boolean win() {
        return !listSlotsForRole(currentRole).isEmpty() && listSlotsForRole(enemyRoleFor(currentRole)).isEmpty();
    }

    // Конец игры наступает, если нет больше фишек у одного из игроков
    public boolean end() {
        return listSlotsForRole(currentRole).isEmpty() || listSlotsForRole(enemyRoleFor(currentRole)).isEmpty();
    }

    public FanoronaProgressDTO progress(int oldIndex, int newIndex, FanoronaRole state, AttackType attack) {
        Timber.i("Ход %s: %d -> %d  ([%d][%d])->([%d][%d]) (%s)",
                state.name(), oldIndex, newIndex,
                row(oldIndex), column(oldIndex), row(newIndex), column(newIndex), attack.name());

        mark(oldIndex, FanoronaRole.FREE);
        mark(newIndex, state);
        // Т.к. мы готовим элемент для добавления в chain, текущий размер chain и есть индекс нового элемента (т.е. power)
        scene.useWay(oldIndex, newIndex, progressChain.size());


        int pFrom = oldIndex;
        int pTo = newIndex;
        switch (attack) {
            case NO:
                return FanoronaProgressDTO.passive(state, oldIndex, newIndex);

            case FORWARD:
                // Убираем всех соперников по линии, пока не дойдём до конца поля или не встретим пустую клетку
                for (Integer nextSlot = nextSlotForLine(oldIndex, newIndex); nextSlot != null && isEnemy(nextSlot, state); nextSlot = nextSlotForLine(oldIndex, newIndex)) {
                    mark(nextSlot, FanoronaRole.FREE);
                    oldIndex = newIndex;
                    newIndex = nextSlot;
                }

                return FanoronaProgressDTO.forward(state, pFrom, pTo);

            case BACK:
                // Убираем всех соперников по обратной линии
                for (Integer nextSlot = nextSlotForLine(newIndex, oldIndex); nextSlot != null && isEnemy(nextSlot, state); nextSlot = nextSlotForLine(newIndex, oldIndex)) {
                    mark(nextSlot, FanoronaRole.FREE);
                    newIndex = oldIndex;
                    oldIndex = nextSlot;
                }

                return FanoronaProgressDTO.back(state, pFrom, pTo);
            default:
                throw new IllegalStateException("Неподдерживаемый тип атаки");
        }
    }

    // Делается ход. Может выбираться приоритетное направление атаки, если это ребуется
    private FanoronaProgressDTO step(int selected, int touched, @Nullable AttackType type) {
        FanoronaProgressDTO progressDTO = buildProgressDTO(selected, touched, type);
        progressChain.step(progressDTO);
        Timber.i("Ход, step: #%d ->%d", progressDTO.getFrom(), progressDTO.getTo());

        scene.releaseAllSlots();
        // Если после выполнения хода агрессивных ходов больше нет или сам ход был не агрессивным,
        // то завершаем последовательность ходов
        if (findAgressiveProgress(touched).isEmpty() || progressDTO.getAttack() == AttackType.NO) {
            Timber.i("Агрессивные ходы кончились. step=0");
            progressChain.end();
        } else { // Если агрессивная последовательность может продолжаться, то нужно пометить
            Timber.i("Агрессивные ходы продолжаются step: %d", progressChain.size());
            prepareProgress(touched);
        }

        return progressDTO;
    }

    // Перебираем все ячейки по линии до конца доски. Содержится ли там искомый слот (начиная с to)
    private boolean onSameLine(Integer from, Integer to, Integer slot) {
        return to.equals(slot) || nextSlotsForLine(from, to, Objects::nonNull).contains(slot);
    }

    // Возвращает друга from в направлении (from -> to)
    private Integer friendOnDirection(Integer from, Integer to) {
        return findFriends(from)
                .stream()
                .filter(f -> onSameLine(from, f, to))
                .findFirst()
                .orElse(null);
    }

    private void prepareProgress(Integer touched) {
        scene.selectSlot(touched);

        // Если это пустая ячейка или чужая для нас фишка, то больше ничего не делаем
        if (isFree(touched) || isEnemy(touched, currentRole))
            return;

        // Если рядом нет свободных друзей, то ход очевидно невозможен
        if (findFriends(touched, FanoronaRole.FREE).isEmpty())
            return;

        // Пробуем нарисовать возможные агрессивные ходы:
        List<Integer> aggressiveProgress = findAgressiveProgress(touched);
        for (Integer progress : aggressiveProgress) {
            scene.progressSlot(progress);
        }

        // Если агрессивных ходов из этой позиции нет и для роли их больше не существует вообще
        // + это начало цепочки, то можно просто походить на пустую клетку
        if (aggressiveProgress.isEmpty() && progressChain.isEmpty() && !hasAgressiveProgress(currentRole)) {
            List<Integer> freeFriends = findFriends(touched, FanoronaRole.FREE);

            for (Integer free : freeFriends) {
                scene.progressSlot(free);
            }
        }
    }

    // Готовим ход в зависимости от совершенных касаний
    private FanoronaProgressDTO buildProgressDTO(int from, int to, @Nullable AttackType type) {
        boolean possibleForwardAttack = possibleAttack(from, to, AttackType.FORWARD);
        boolean possibleBackAttack = possibleAttack(from, to, AttackType.BACK);

        if (possibleForwardAttack && possibleBackAttack) {
            Timber.i("Атака в обоих направлениях. Заданное направление: %s", type);
            return progress(from, to, currentRole, (type != null) ? type : AttackType.FORWARD);
        } else if (possibleForwardAttack) {
            return progress(from, to, currentRole, AttackType.FORWARD);
        } else if (possibleBackAttack) {
            return progress(from, to, currentRole, AttackType.BACK);
        } else {
            return progress(from, to, currentRole, AttackType.NO);
        }
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

        for (PairIndex way : pairIndices) {
            if (way.connect(globalIndex)) {
                friends.add(way.friend(globalIndex));
            }
        }

        return friends;
    }

    // Найти всех друзей с указанным состоянием
    private List<Integer> findFriends(int globalIndex, FanoronaRole role) {
        return findFriends(globalIndex)
                .stream()
                .filter(i -> getState(i) == role)
                .collect(Collectors.toList());
    }

    // Есть ли возможность атаки в указанном направлении?
    // Да, если следующей фишкой по данному направлению является противник
    private boolean possibleAttack(int from, int to, AttackType attack) {
        FanoronaRole enemy = enemyRoleFor(currentRole);
        Integer next;

        switch (attack) {
            case FORWARD:
                next = nextSlotForLine(from, to);
                return next != null && getState(next) == enemy;

            case BACK:
                next = nextSlotForLine(to, from);
                return next != null && getState(next) == enemy;
            default:
                return false;
        }
    }

    /**
        Фишка имеет агрессивные ходы: (это должна быть наша фишка)
        Два типа агрессии:
        1. Среди друзей есть фишки соперника, по обраткой линии от этого соперника есть свободная клетка
        2. Среди друзей есть свободная клетка, по линии этой клетки есть соперник.

    */
    private List<Integer> findAgressiveProgress(int to) {
        List<Integer> aggressiveProgress = new LinkedList<>();

        if (getState(to) != currentRole)
            return aggressiveProgress;

        // Перебираем свободных друзей и смотрим есть ли на линии фишка соперника
        List<Integer> freeFriends = findFriends(to, FanoronaRole.FREE);
        for (Integer freeFriend : freeFriends) {
            Integer nextSlot = nextSlotForLine(to, freeFriend);

            if (nextSlot != null && getState(nextSlot) == enemyRoleFor(currentRole)) {
                aggressiveProgress.add(freeFriend);
            }
        }

        // Перебираем фишки противника среди друзей
        List<Integer> enemyFriends = findFriends(to, enemyRoleFor(currentRole));
        for (Integer enemyFriend : enemyFriends) {
            Integer nextSlot = nextSlotForLine(enemyFriend, to);

            if (nextSlot != null && getState(nextSlot) == FanoronaRole.FREE) {
                aggressiveProgress.add(nextSlot);
            }
        }


        // Из всех возможных агрессивных ходов выбираем только те, что не продолжают линию (last->to)
        // И те что не содержатся среди уже совершенных ходов
        // last - ячейка, где сейчас стоит фишка.
        FanoronaProgressDTO lastProgress = progressChain.last();

        return aggressiveProgress
                .stream()
                .filter(progress -> {
                    Integer next = (lastProgress != null) ? nextSlotForLine(lastProgress.getFrom(), to) : null;
                    return next == null || !next.equals(progress);
                })
                .filter(progress ->
                        !progressChain.asStream()
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

    // Получение фишек по линии, согласно условию
    private List<Integer> nextSlotsForLine(int startIndex, int endIndex, Predicate<Integer> predicate) {
        List<Integer> slts = new ArrayList<>();

        for (Integer cur = nextSlotForLine(startIndex, endIndex); predicate.test(cur); cur = nextSlotForLine(startIndex, endIndex)) {
            slts.add(cur);
            startIndex = endIndex;
            endIndex = Objects.requireNonNull(cur);
        }

        return slts;
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

    private boolean possibleDoubleAttack(@Nullable Integer from, @Nullable Integer to) {
        return (from != null && to != null
                && getState(from) == currentRole
                && scene.possibleProgress(to)
                && possibleAttack(from, to, AttackType.FORWARD)
                && possibleAttack(from, to, AttackType.BACK)
        );
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
        List<Integer> slotsForRole = new ArrayList<>(totalCountSlots);

        for (int i = 0; i < totalCountSlots; i++)
            if (getState(i) == role)
                slotsForRole.add(i);

        return slotsForRole;
    }

    // Список фишек, имеющих рядом свободную клетку
    private List<Integer> listSlotsHasFreeFriend(FanoronaRole role) {
        List<Integer> allSlotsForRole = listSlotsForRole(role);

        return allSlotsForRole
                .stream()
                .filter(index -> {
                    List<Integer> friends = findFriends(index, FanoronaRole.FREE);
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
