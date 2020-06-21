package root.iv.ivplayer.game.fanorona.room;

import androidx.annotation.Nullable;

import root.iv.ivplayer.game.room.RoomState;

public interface FanoronaRoomListener {
    void changeStatus(RoomState roomState);
    void updatePlayers(@Nullable String displayName1, @Nullable String displayName2);
}
