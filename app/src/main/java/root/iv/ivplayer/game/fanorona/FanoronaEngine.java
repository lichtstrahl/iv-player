package root.iv.ivplayer.game.fanorona;

import java.util.Arrays;

import root.iv.ivplayer.game.fanorona.slot.SlotState;
import root.iv.ivplayer.game.fanorona.slot.SlotWay;

/**
 * Игровой движок фанороны
 */
public class FanoronaEngine {
    private static final int COUNT_ROW = 5;
    private static final int COUNT_COLUMN = 9;
    private SlotState[][] slots;
    private SlotWay[] slotWays;

    public FanoronaEngine() {
        slots = new SlotState[COUNT_ROW][COUNT_COLUMN];

        // Заполняем все слоты пустыми
        for (int i = 0; i < COUNT_ROW; i++) {
            Arrays.fill(slots[i], SlotState.FREE);
        }

        // Пустой массив связей
        slotWays = new SlotWay[0];
    }
}
