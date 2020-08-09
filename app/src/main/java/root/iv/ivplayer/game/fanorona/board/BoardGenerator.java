package root.iv.ivplayer.game.fanorona.board;


import java.util.Arrays;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import root.iv.ivplayer.game.fanorona.FanoronaRole;
import root.iv.ivplayer.game.fanorona.FanoronaScene;
import root.iv.ivplayer.game.fanorona.slot.PairIndex;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardGenerator {
    private final int COUNT_COLUMN;
    private final int COUNT_ROW;

    public static BoardGenerator newGenerator(int countColumns, int countRows) {
        return new BoardGenerator(countColumns, countRows);
    }

    public FanoronaRole[][] empty() {
        FanoronaRole[][] slots = new FanoronaRole[COUNT_ROW][COUNT_COLUMN];

        // Заполняем все слоты пустыми
        for (int i = 0; i < COUNT_ROW; i++) {
            Arrays.fill(slots[i], FanoronaRole.FREE);
        }

        return slots;
    }

    // Стандартный стартовый расклад фишек
    public FanoronaRole[][] start() {
        FanoronaRole[][] slots = empty();

        // Расставляем тылы BLACK
        for (int j = 0; j < COUNT_COLUMN; j++) {
            slots[0][j] = FanoronaRole.BLACK;
            slots[1][j] = FanoronaRole.BLACK;
        }

        // Расставляем тылы WHITE
        for (int j = 0; j < COUNT_COLUMN; j++) {
            slots[3][j] = FanoronaRole.WHITE;
            slots[4][j] =  FanoronaRole.WHITE;
        }

        // Линия фронта (Слева начиная с BLACK, Справа начиная с BLACK)

        for (int left = 0, right = COUNT_COLUMN-1; left != right; left++, right--) {
            slots[2][left]  = (left%2)  == 0 ? FanoronaRole.BLACK : FanoronaRole.WHITE;
            slots[2][right] = (right%2) == 0 ? FanoronaRole.WHITE : FanoronaRole.BLACK;
        }

        return slots;
    }

    // Белая фишка окружена черными. Для тестирования цепочки ходов
    public FanoronaRole[][] testInner() {
        FanoronaRole[][] slots = empty();

        int blackI = 2;
        int blackJ = 2;

        // Ставим белую
        slots[blackI][blackJ] = FanoronaRole.WHITE;

        // Ставим черные сверху и снизу на одну клетку
        for (int j = 0; j < 3; j++) {
            slots[blackI-2][blackJ-2 + j*2] = slots[blackI+2][blackJ-2 + j*2] = FanoronaRole.BLACK;
        }

        // Ставим слева и справа
        slots[blackI][blackJ-2] = slots[blackI][blackJ+2] = FanoronaRole.BLACK;

        return slots;
    }

    // Тест на двойное направление атаки
    public FanoronaRole[][] testDoubleAttack() {
        FanoronaRole[][] slots = empty();

        slots[0][0] = FanoronaRole.BLACK;
        slots[0][1] = FanoronaRole.BLACK;

        slots[0][2] = FanoronaRole.WHITE;

        slots[0][4] = FanoronaRole.BLACK;
        slots[0][5] = FanoronaRole.BLACK;

        return slots;
    }

    // Заполнение переходов между слотами. Стандартная доска.
    public PairIndex[] ways() {
        PairIndex[] pairIndices = new PairIndex[108];
        int index = 0;

        // Заполняем строки
        for (int i = 0; i < COUNT_ROW; i++) {
            for (int j = 0; j < COUNT_COLUMN-1; j++)
                pairIndices[index++] = PairIndex.column9(i,j, i, j+1);
        }

        // Заполняем столбцы
        for (int j = 0; j < COUNT_COLUMN; j++) {
            for (int i = 0; i < COUNT_ROW-1; i++)
                pairIndices[index++] = PairIndex.column9(i,j, i+1, j);
        }

        // Заполняем диагонали
        for (int i = 1; i < COUNT_ROW; i += 2) {
            for (int j = 1; j < COUNT_COLUMN; j += 2) {
                // Нужно соединить эту вершину со всеми возможными диагоналями (центр паука). Рисуем лапки
                pairIndices[index++] = PairIndex.column9(i,j, i-1,j-1);
                pairIndices[index++] = PairIndex.column9(i,j, i-1,j+1);
                pairIndices[index++] = PairIndex.column9(i,j, i+1,j-1);
                pairIndices[index++] = PairIndex.column9(i,j, i+1, j+1);
            }
        }

        if (index != pairIndices.length)
            throw new IllegalStateException("Массив дорожек заполнен не до конца");

        return pairIndices;
    }
}
