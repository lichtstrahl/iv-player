package root.iv.ivplayer.network.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.iv.ivplayer.game.fanorona.slot.SlotState;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FBFanoronaProgress {
    private String uid;
    private SlotState state;
    private Integer from;
    private Integer to;
    private boolean end;
    private boolean win;
}
