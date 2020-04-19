package root.iv.ivplayer.network.ws.pubnub.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import root.iv.ivplayer.game.tictac.BlockState;

/**
 * Ход игрока:
 * 1. uuid
 * 2. Тип хода
 * 3. Номер блока
 */
@Data
@AllArgsConstructor
public class TicTacStateDTO implements Serializable {
    private String uuid;
    private BlockState state;
    private int blockIndex;
}
