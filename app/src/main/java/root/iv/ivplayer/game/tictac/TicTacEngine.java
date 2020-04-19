package root.iv.ivplayer.game.tictac;

// Непосредственно движок игры
// Здесь будут храниться необходимые данные для рассчетов
public class TicTacEngine {
    private Block[] blocks;

    public TicTacEngine() {
        this.blocks = new Block[9];
    }

    public void loadBlock(int index, Block block) {
        this.blocks[index] = block;
    }
}
