package root.iv.ivplayer.network.ws.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayerLifecycleMSG extends BaseMessageWS {
    private String login;
    private String roomName;
    private PlayerLifecycle lifecycle;

    private PlayerLifecycleMSG(String login, String roomName, PlayerLifecycle lifecycle) {
        super(login, null, roomName, TypeMSG.LIFECYCLE);
        this.login = login;
        this.roomName = roomName;
        this.lifecycle = lifecycle;
    }

    public static PlayerLifecycleMSG join(String login, String room) {
        return new PlayerLifecycleMSG(login, room, PlayerLifecycle.JOIN);
    }

    public static PlayerLifecycleMSG leave(String login, String room) {
        return new PlayerLifecycleMSG(login, room, PlayerLifecycle.LEAVE);
    }
}
