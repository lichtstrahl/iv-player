package root.iv.ivplayer.game.tictac;

import android.graphics.RectF;

// Непосредственно движок игры
// Здесь будут храниться необходимые данные для рассчетов
public class TicTacEngine {
    private Block[] blocks;
    private BlockState currentState;

    public TicTacEngine() {
        this.blocks = new Block[9];
        currentState = BlockState.FREE;
    }

    public void loadBlock(int index, Block block) {
        this.blocks[index] = block;
    }

    public void setCurrentState(BlockState state) {
        currentState = state;
    }

    // Отрыв пальца от экрана. Должен ли блок реагировать?
    public void touchUp(float x, float y) {
        for (Block b : blocks) {
            RectF bounds = b.getBounds();
            boolean click = bounds.contains(x, y);
            if (click) b.mark(currentState);
        }
    }
}
