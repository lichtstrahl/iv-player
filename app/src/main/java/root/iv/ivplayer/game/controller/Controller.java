package root.iv.ivplayer.game.controller;

import root.iv.ivplayer.game.object.simple.Object2;

/**
 * Контроллер может брать под управление какой-либо объект
 * Может отпускать его и сообщать информацию о текущем состоянии (занят/свободен)
 */
public interface Controller {
    void grabObject(Object2 object);
    void releaseObject();
    boolean isReleased();
}
