package root.iv.ivplayer.game.tictac.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicTacEndDTO implements Serializable {
    private String uuid;
    private boolean win;
}
