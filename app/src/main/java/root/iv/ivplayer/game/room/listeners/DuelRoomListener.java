package root.iv.ivplayer.game.room.listeners;

import androidx.annotation.Nullable;

// Слушатель комнаты для дуэлей. Ожидает двух игроков
public interface DuelRoomListener extends RoomListener {
    void updatePlayers(@Nullable String displayName1, @Nullable String displayName2);
}
