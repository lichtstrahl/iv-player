package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;

import root.iv.ivplayer.game.controller.SensorController;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.simple.Object2;
import root.iv.ivplayer.game.view.GameView;

/**
 * Самая обычная сцена:
 1  Умеет себя отрисовывать
 2. Умеет подстраивать свой размер
 3. Инициализировать собой gameView
 **/
public interface Scene {
    void render(Canvas canvas);
    void resize(int width, int height);
    void connect(GameView gameView);
}
