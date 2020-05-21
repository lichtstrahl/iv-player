package root.iv.ivplayer.game.engine;

import root.iv.ivplayer.game.view.GameView;

/**
 * Игровой движок
 * 1. Закончилась ли игра
 * 2. Является ли ситуация победной
 * 3. Присоединение к GameView
 */
public interface Engine {
    boolean end();
    boolean win();
    void connect(GameView gameView);
}
