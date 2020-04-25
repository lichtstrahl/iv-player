package root.iv.ivplayer.game.tictac.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

// Информация о текущих координатах игрока
@Data
@AllArgsConstructor
public class PlayerPositionDTO implements Serializable {
    private String uuid;
    private float x0;
    private float y0;
}
