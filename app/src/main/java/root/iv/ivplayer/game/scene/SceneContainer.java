package root.iv.ivplayer.game.scene;

import lombok.Getter;

// Контейнер для сцены хрянит внутри себя сцену и даёт доступ к ней
// Сцену заменить нельзя
public abstract class SceneContainer<T extends Scene> {
    @Getter
    private final T scene;

    public SceneContainer(T scene) {
        this.scene = scene;
    }

}
