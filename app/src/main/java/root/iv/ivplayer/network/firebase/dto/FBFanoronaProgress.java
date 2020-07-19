package root.iv.ivplayer.network.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.iv.ivplayer.game.fanorona.AttackType;
import root.iv.ivplayer.game.fanorona.FanoronaRole;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FBFanoronaProgress {
    private String uid;
    private FanoronaRole state;
    private Integer from;
    private Integer to;
    private AttackType attack;
    private boolean end;
    private boolean win;
}
