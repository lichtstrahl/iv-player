package root.iv.ivplayer.network.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Ход игрока. Может быть победным или просто последним возможным.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FBProgress {
    private int index;
    private boolean win;
    private boolean end;
}
