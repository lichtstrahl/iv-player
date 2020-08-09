package root.iv.ivplayer.game.room;

import root.iv.ivplayer.game.room.listeners.RoomListener;
import root.iv.ivplayer.game.view.GameView;

// Базовая комната. Является контейнером для сцены, имеющим имя
public abstract class Room {
    protected String name;

    public Room(String name) {
        this.name = name;
    }

    public abstract void addListener(RoomListener listener);
    public abstract void resize(int width, int height);
    public abstract void connect(GameView gameView);
    public abstract void exit();
    public abstract void init();
}
