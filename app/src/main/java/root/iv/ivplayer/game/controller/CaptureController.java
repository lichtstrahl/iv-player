package root.iv.ivplayer.game.controller;

import root.iv.ivplayer.game.object.simple.Object2;

/**
 * Интерфейс для захвата объектов:
 * 1. Захватить объект в управление
 * 2. Отпустить объект
 * 3. Узнать свободен ли контроллер
 */
public interface CaptureController {
    void grabObject(Object2 object);
    void releaseObject();
    boolean isReleased();
}
