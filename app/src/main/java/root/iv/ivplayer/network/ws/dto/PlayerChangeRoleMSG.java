package root.iv.ivplayer.network.ws.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerChangeRoleMSG extends BaseMessageWS {
    private String login;
    private String roomName;
    private UserRole role;

    public PlayerChangeRoleMSG(String login, String room, UserRole role) {
        super(login, null, room, TypeMSG.PLAYER_CHANGE_ROLE);
        this.login = login;
        this.roomName = room;
        this.role = role;
    }
}
