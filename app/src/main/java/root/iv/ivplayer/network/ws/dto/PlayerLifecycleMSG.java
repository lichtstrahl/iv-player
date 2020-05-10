package root.iv.ivplayer.network.ws.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerLifecycleMSG {
    private String login;
    private String roomName;
    private PlayerLifecycle lifecycle;

    public static PlayerLifecycleMSG join(String login, String room) {
        return new PlayerLifecycleMSG(login, room, PlayerLifecycle.JOIN);
    }

    public static PlayerLifecycleMSG leave(String login, String room) {
        return new PlayerLifecycleMSG(login, room, PlayerLifecycle.LEAVE);
    }
}
