package root.iv.ivplayer.game.scene;

import root.iv.ivplayer.game.controller.SensorController;

/**
 * Сенсорная сцена - обычная сцена, имеющая контроллер касаний
 */
public abstract class SensorScene implements Scene {
    protected SensorController sensorController;

    public SensorScene(SensorController controller) {
        sensorController = controller;
    }
}
