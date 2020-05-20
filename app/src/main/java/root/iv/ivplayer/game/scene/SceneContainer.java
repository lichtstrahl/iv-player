package root.iv.ivplayer.game.scene;

import lombok.Getter;

// Контейнер для сцены хрянит внутри себя сцену и даёт доступ к ней
// Сцену заменить нельзя
public abstract class SceneContainer {
    @Getter
    private final Scene scene;

    public SceneContainer(Scene scene) {
        this.scene = scene;
    }

    public void autoResize(int width, int height) {

    }
}
