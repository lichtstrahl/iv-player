package root.iv.ivplayer.game.fanorona.slot;

import lombok.AllArgsConstructor;
import lombok.Data;

// Соединение между двумя слотами, хранит две координаты: значения i,j для каждого слота
@Data
@AllArgsConstructor
public class SlotWay {
    private int i1;
    private int j1;
    private int i2;
    private int j2;
}
