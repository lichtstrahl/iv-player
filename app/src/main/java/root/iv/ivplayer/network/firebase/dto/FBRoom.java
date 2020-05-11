package root.iv.ivplayer.network.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.iv.ivplayer.game.room.RoomState;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FBRoom {
    private String emailPlayer1;
    private String emailPlayer2;
    private RoomState state;

    public int countPlayer() {
        int count = 0;

        count += (emailPlayer1.isEmpty()) ? 0 : 1;
        count += (emailPlayer2.isEmpty()) ? 0 : 1;

        return count;
    }
}
