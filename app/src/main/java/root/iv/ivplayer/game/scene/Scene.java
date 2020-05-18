package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;

import root.iv.ivplayer.game.controller.Controller;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.game.object.simple.Object2;
import root.iv.ivplayer.game.tictac.dto.PlayerPositionDTO;

/**
     Что умеет сцена:
    1 Любая сцена должна уметь себя отрисовывать.
    2 Кроме этого сцена возвращает главный контроллер, который отвечает за её реакцию на нажатие
    3 Добавлять отрисовываемый объект
    4 Пермещать объект с выбранным индексом
    5 Захват объекта для управления
 **/
public interface Scene {
    void render(Canvas canvas);

    Controller getMainController();
    void addDrawableObject(DrawableObject object2);

    void moveOnObject(int index, float dx, float dy);

    void grabObjectControl(Object2 object);
}
