package root.iv.ivplayer.game.room.listeners;

import root.iv.ivplayer.game.room.RoomState;

// Слушатель для комнаты обязан реагировать на изменение её статуса
public interface RoomListener {
    void changeStatus(RoomState roomState);
}
