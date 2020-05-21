package root.iv.ivplayer.game.room;

import root.iv.ivplayer.game.view.GameView;

// Базовая комната. Является контейнером для сцены, имеющим имя
public abstract class Room {
    protected String name;

    public Room(String name) {
        this.name = name;
    }

    abstract public void addListener(RoomListener listener);
    abstract public void resize(int width, int height);
    abstract public void connect(GameView gameView);
    abstract public void exit();
    abstract public void init();
}
