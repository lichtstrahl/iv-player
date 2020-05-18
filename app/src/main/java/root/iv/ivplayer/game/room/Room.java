package root.iv.ivplayer.game.room;

import lombok.Getter;
import root.iv.ivplayer.game.scene.Scene;

// Базовая комната. Имеет состояние и отрисовываемую сцену, больше ничего
public abstract class Room {
    @Getter
    protected Scene scene;
    protected RoomState state;

    public Room(Scene scene) {
        this.scene = scene;
        this.state = RoomState.NEW;
    }

    abstract public void addListener(RoomListener listener);
    abstract public void exitFromRoom();
}
