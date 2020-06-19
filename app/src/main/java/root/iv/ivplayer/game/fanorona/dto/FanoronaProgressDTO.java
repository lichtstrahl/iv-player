package root.iv.ivplayer.game.fanorona.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import root.iv.ivplayer.game.fanorona.FanoronaRole;

@Data
@AllArgsConstructor
public class FanoronaProgressDTO {
    private FanoronaRole state;
    private Integer from;
    private Integer to;
}
