package root.iv.ivplayer.network.firebase.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoomUI extends FBRoom {
    private String name;

    @Builder
    public RoomUI(String name, FBRoom fbRoom) {
        this.name = name;
        this.player1 = fbRoom.getPlayer1();
        this.player2 = fbRoom.getPlayer2();
        this.state = fbRoom.getState();
        this.gameType = fbRoom.getGameType();
    }
}
