package root.iv.bot;

import java.util.List;

public interface BotAPI {

    /**
     * Реакция на ход соперника. Один ход, перемещение с клетки на клетку.
     * В случае с матрицей это будет [from/COUNT_COLUMN][from%COUNT_COLUMN] -> [to/COUNT_COLUMN][to%COUNT_COLUMN]
     */
    void processEnemyStep(int from, int to);

    /**
     * Когда была последовательность атакующих ходов. Она завершилась, бот должен среагировать.
     * Передвинуть у себя в голове фишки соперника, убрать свои если требуется.
     * В целом это последовательный вызов processEnemyStep
     */
    void processEnemyProgress(List<Progress> move);

    /**
     * Бот делает ход. Переставляет свою фишку (возможно несколько раз, пока агрессивные ходы продолжаются),
     * убирает фишки соперника если нужно.
     * @return Список ходов-перемещений, чтобы клиент на них среагировал.
     */
    List<Progress> progress();
}
