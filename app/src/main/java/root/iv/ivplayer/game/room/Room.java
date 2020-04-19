package root.iv.ivplayer.game.room;

/**
    Класс комната: содержит информацию о максимальном и минимальном количестве игроков
    А также основную сцену
 */
public abstract class Room {
    protected int maxPlayers;
    protected int minPlauers;
    protected int currentPlayers;
    protected RoomState state;

    public Room(int fixPlayer) {
        this.maxPlayers = fixPlayer;
        this.minPlauers = fixPlayer;
        this.currentPlayers = 0;
        this.state = RoomState.WAIT_PLAYERS;
    }

    public Room(int maxPlayers, int minPlauers) {
        this.maxPlayers = maxPlayers;
        this.minPlauers = minPlauers;
        this.currentPlayers = 0;
        this.state = RoomState.WAIT_PLAYERS;
    }
}
