package root.iv.ivplayer.game.room;

import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.scene.SceneContainer;

// Базовая комната. Является контейнером для сцены, имеющим имя
public abstract class Room extends SceneContainer {
    protected String name;

    public Room(String name, Scene scene) {
        super(scene);
        this.name = name;
    }

    abstract public void addListener(RoomListener listener);
    abstract public void exit();
    abstract public void init();
}
