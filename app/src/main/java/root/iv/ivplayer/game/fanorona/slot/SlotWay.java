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

    public boolean connect(int i, int j) {
        return (i1 == i && j1 == j) || (i2 == i && j2 == j);
    }

    public int iFriend(int i) {
        return (i == i1)
                ? i2
                : i1;
    }

    public int jFriend(int j) {
        return (j == j1)
                ? j2
                : j1;
    }
}
