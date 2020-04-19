package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;

import root.iv.ivplayer.game.controller.Controller;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;

/**
     Что умеет сцена:
    1 Любая сцена должна уметь себя отрисовывать.
    2 Потому что это просто набор картинок
    3 Также она может поддерживать реакцию на вход новых игроков
    4 Кроме этого сцена возвращает главный контроллер, который отвечает за её реакцию на нажатие
    5 Добавлять отрисовываемый объект
    6 Обрабатывать пришедшую информацию о новых позициях
    7 Пермещать объект с выбранным индексом
 **/
public interface Scene {
    void render(Canvas canvas);
    void joinPlayer(String joinUUID, float x, float y);
    Controller getMainController();
    void addDrawableObject(DrawableObject object2);
    void processPlayerPositionDTO(PlayerPositionDTO position);
    void moveOnObject(int index, float dx, float dy);
}
