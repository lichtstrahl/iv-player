package root.iv.ivplayer.game.engine;

/**
 * Игровой движок
 * 1. Закончилась ли игра
 * 2. Является ли ситуация победной
 */
public interface Engine {
    boolean end();
    boolean win();
}
