package root.iv.ivplayer.game.engine;

/**
 * Движок для пошаговых игр
 * 1. Количество ходов в истории
 * 2. Последний ход
 */
public interface StepEngine<P> {
    int getProgressHistorySize();
    P getLastProgress();
}
