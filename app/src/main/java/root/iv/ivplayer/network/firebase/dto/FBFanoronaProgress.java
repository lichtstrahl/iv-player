package root.iv.ivplayer.network.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import root.iv.ivplayer.game.fanorona.slot.SlotState;

@Data
@AllArgsConstructor
public class FBFanoronaProgress {
    private String uid;
    private SlotState state;
    private Integer from;
    private Integer to;
    private boolean end;
    private boolean win;
}
