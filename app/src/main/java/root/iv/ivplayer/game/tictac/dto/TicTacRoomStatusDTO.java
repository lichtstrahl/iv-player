package root.iv.ivplayer.game.tictac.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.iv.ivplayer.game.room.RoomState;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicTacRoomStatusDTO {
    private String uuid;
    private RoomState roomState;
}
