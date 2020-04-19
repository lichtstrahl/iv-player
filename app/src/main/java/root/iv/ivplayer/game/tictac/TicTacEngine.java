package root.iv.ivplayer.game.tictac;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

import root.iv.ivplayer.network.ws.pubnub.dto.TicTacStateDTO;

// Непосредственно движок игры
// Здесь будут храниться необходимые данные для рассчетов
public class TicTacEngine {
    private Block[] blocks;
    private BlockState currentState;
    private List<TicTacStateDTO> history;

    public TicTacEngine() {
        this.blocks = new Block[9];
        this.currentState = BlockState.FREE;
        this.history = new ArrayList<>();
    }

    public void loadBlock(int index, Block block) {
        this.blocks[index] = block;
    }

    public void setCurrentState(BlockState state) {
        currentState = state;
    }

    // Отрыв пальца от экрана. Должен ли блок реагировать?
    public void touchUp(float x, float y) {
        for (int i = 0; i < blocks.length; i++) {
            Block b = blocks[i];
            RectF bounds = b.getBounds();
            boolean click = bounds.contains(x, y);
            if (click) {
                b.mark(currentState);
                history.add(new TicTacStateDTO(null, currentState, i));
            }
        }
    }

    public int getHistorySize() {
        return history.size();
    }

    public TicTacStateDTO getLastState() {
        return history.get(history.size()-1);
    }
}
