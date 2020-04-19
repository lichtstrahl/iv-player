package root.iv.ivplayer.network.ws.pubnub.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicTacWinDTO implements Serializable {
    private String uuid;
    private boolean win;
}
