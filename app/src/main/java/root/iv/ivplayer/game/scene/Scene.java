package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;

import root.iv.ivplayer.game.controller.SensorController;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.simple.Object2;

/**
 * Самая обычная сцена:
 1  Умеет себя отрисовывать
 2. Умеет подстраивать свой размер
 **/
public interface Scene {
    void render(Canvas canvas);
    void resize(int width, int height);
}
