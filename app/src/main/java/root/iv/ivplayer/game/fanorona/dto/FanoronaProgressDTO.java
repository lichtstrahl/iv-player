package root.iv.ivplayer.game.fanorona.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import root.iv.ivplayer.game.fanorona.slot.SlotState;

@Data
@AllArgsConstructor
public class FanoronaProgressDTO {
    private SlotState state;
    private Integer from;
    private Integer to;
}
