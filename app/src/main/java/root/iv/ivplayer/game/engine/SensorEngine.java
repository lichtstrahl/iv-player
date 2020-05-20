package root.iv.ivplayer.game.engine;

/**
 * Движок, реагирующий на касания. Т.е. управление не кнопками а через экран
 * 1. Реакция на отрыв пальца от определённых координат (касание)
 */
public interface SensorEngine {
    void touchUp(float x, float y);
}
