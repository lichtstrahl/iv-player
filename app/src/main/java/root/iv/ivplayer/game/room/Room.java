package root.iv.ivplayer.game.room;

import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.scene.SceneContainer;

// Базовая комната. Является контейнером для сцены, имеющим имя
public abstract class Room<S extends Scene> extends SceneContainer<S> {
    protected String name;

    public Room(String name, S scene) {
        super(scene);
        this.name = name;
    }

    abstract public void addListener(RoomListener listener);
    abstract public void resize(int width, int height);
    abstract public void exit();
    abstract public void init();
}
