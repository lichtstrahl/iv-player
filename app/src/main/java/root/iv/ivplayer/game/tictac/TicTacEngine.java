package root.iv.ivplayer.game.tictac;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;

// Непосредственно движок игры
// Здесь будут храниться необходимые данные для рассчетов
public class TicTacEngine implements TicTacEngineAPI {
    private static final int COUNT_COLUMNS = 3;
    private static final int COUNT_ROWS = 3;

    private Block[] blocks;
    private BlockState currentState;
    private List<TicTacProgressDTO> history;

    public TicTacEngine(List<Block> blocks) {
        this.currentState = BlockState.FREE;
        this.history = new ArrayList<>();

        loadBlocks(blocks);
    }

    public void loadBlock(int index, Block block) {
        this.blocks[index] = block;
    }

    @Override
    public void loadBlocks(List<Block> blocks) {
        this.blocks = new Block[9];

        for (int i = 0; i < 9; i++)
            loadBlock(i, blocks.get(i));
    }

    @Override
    public void markBlock(int index, BlockState state) {
        this.blocks[index].mark(state);
    }

    @Override
    public void progress(int index, BlockState state) {
        markBlock(index, state);
        history.add(new TicTacProgressDTO(null, currentState, index));
    }

    @Override
    public BlockState getCurrentRole() {
        return currentState;
    }

    @Override
    public void setCurrentRole(BlockState state) {
        this.currentState = state;
    }

    // Отрыв пальца от экрана. Должен ли блок реагировать?
    @Override
    public void touchUp(float x, float y) {
        for (int i = 0; i < blocks.length; i++) {
            Block b = blocks[i];
            RectF bounds = b.getBounds();
            boolean click = bounds.contains(x, y);
            if (click && b.getState() == BlockState.FREE) {
                b.mark(currentState);
                history.add(new TicTacProgressDTO(null, currentState, i));
            }
        }
    }

    @Override
    public boolean end() {
        return !hasFreeBlocks();
    }

    // Определяет является ли ситуация на площадке победной
    public boolean win() {
        boolean winColumns = winColumns();
        boolean winRows = winRows();
        boolean winMainDiagonal = winMainDiagonal();
        boolean winSecondDiagonal = winSecondDiagonal();

        return winColumns || winRows || winMainDiagonal || winSecondDiagonal;
    }

    public boolean hasFreeBlocks() {
        return Arrays.stream(blocks).anyMatch(b -> b.getState() == BlockState.FREE);
    }

    private boolean winColumns() {

        for (int j = 0; j < COUNT_COLUMNS; j++) {
            if (winColumn(j)) return true;
        }

        return false;
    }

    private boolean winRows() {
        for (int i = 0; i < COUNT_ROWS; i++) {
            if (winRow(i)) return true;
        }

        return false;
    }

    private boolean winMainDiagonal() {
        BlockState state = blocks[0].getState();
        if (state == BlockState.FREE) return false;

        for (int i = 1; i < COUNT_COLUMNS; i++) {
            if (!blocks[(COUNT_COLUMNS*i) + i].getState().equals(state)) return false;
        }

        return true;
    }

    private boolean winSecondDiagonal() {
        BlockState state = blocks[COUNT_COLUMNS-1].getState();
        if (state == BlockState.FREE) return false;

        for (int i = 1; i < COUNT_COLUMNS; i++) {
            if (!blocks[COUNT_COLUMNS*(i+1)-(i+1)].getState().equals(state)) return false;
        }

        return true;
    }

    // Запоминаем состояние верхнего поля в стоолбце
    // Если оно свободно, то проверка бессмысленна
    // Проверяем, есть ли отличающиеся блоки
    private boolean winColumn(int column) {
        BlockState state = blocks[column].getState();
        if (state == BlockState.FREE) return false;

        for (int i = column + COUNT_COLUMNS; i < 9; i += COUNT_COLUMNS) {
            if (!blocks[i].getState().equals(state)) {
                return false;
            }
        }

        return true;
    }

    // Запоминаем состояние первого элемента строки
    // Если оно свободно, то проверка бессмысленна
    // Проверяем, есть ли отличающиеся блоки
    private boolean winRow(int row) {
        BlockState state = blocks[row*COUNT_COLUMNS].getState();
        if (state == BlockState.FREE) return false;

        for (int j = row*COUNT_COLUMNS+1; j < (row*COUNT_COLUMNS + COUNT_COLUMNS); j++) {
            if (!blocks[j].getState().equals(state)) return false;
        }

        return true;
    }

    @Override
    public int getProgressHistorySize() {
        return history.size();
    }

    @Override
    public TicTacProgressDTO getLastProgress() {
        return history.get(history.size()-1);
    }
}
