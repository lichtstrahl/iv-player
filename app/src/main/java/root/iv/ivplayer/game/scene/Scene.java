package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;

import root.iv.ivplayer.game.controller.SensorController;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.simple.Object2;

/**
     Что умеет сцена:
    1 Любая сцена должна уметь себя отрисовывать.
 **/
public interface Scene {
    void render(Canvas canvas);

    SensorController getMainController();
    void addDrawableObject(DrawableObject object2);

    void moveOnObject(int index, float dx, float dy);
    void resize(int width, int height);
}
