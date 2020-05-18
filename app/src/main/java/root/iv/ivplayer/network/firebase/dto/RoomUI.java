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
        this.emailPlayer1 = fbRoom.getEmailPlayer1();
        this.emailPlayer2 = fbRoom.getEmailPlayer2();
        this.state = fbRoom.getState();
    }
}
