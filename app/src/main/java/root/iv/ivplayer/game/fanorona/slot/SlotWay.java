package root.iv.ivplayer.game.fanorona.slot;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

// Соединение между двумя слотами, хранит две координаты: значения i,j для каждого слота
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SlotWay {
    private int countColumn;
    private int from;
    private int to;

    public static SlotWay column9(int globalIndexFrom, int globalIndexTo) {
        return new SlotWay(9, globalIndexFrom, globalIndexTo);
    }

    public static SlotWay column9(int i1, int j1, int i2, int j2) {
        return new SlotWay(9, i1*9+j1,i2*9+j2);
    }

    public boolean connect(int index) {
        return to==index || from == index;
    }

    public int friend(int origin) {
        return (from == origin)
                ? to
                : from;
    }
}
