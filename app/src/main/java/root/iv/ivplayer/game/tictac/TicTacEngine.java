package root.iv.ivplayer.game.tictac;

import android.view.MotionEvent;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;
import root.iv.ivplayer.game.tictac.scene.TicTacScene;
import root.iv.ivplayer.game.tictac.scene.TicTacSceneFactory;
import root.iv.ivplayer.game.view.GameView;

// Непосредственно движок игры
// Здесь будут храниться необходимые данные для рассчетов
public class TicTacEngine implements TicTacEngineAPI {
    private static final int COUNT_COLUMNS = 3;
    private static final int COUNT_ROWS = 3;

    private BlockState[] blocks;
    private BlockState currentState;
    private List<TicTacProgressDTO> history;

    private TicTacScene scene;

    public TicTacEngine(TicTacTextures textures, Consumer<MotionEvent> touchHandler) {
        this.currentState = BlockState.FREE;
        this.history = new ArrayList<>();

        blocks = new BlockState[9];
        Arrays.fill(blocks, BlockState.FREE);

        this.scene = TicTacSceneFactory.newFactory().defaultScene(textures);
        this.scene.getSensorController().setTouchHandler(touchHandler);
    }

    @Override
    public void markBlock(int index, BlockState state) {
        this.blocks[index] = state;
        scene.markBlock(index, state);
    }

    @Override
    public TicTacProgressDTO progress(int index, BlockState state) {
        markBlock(index, state);
        TicTacProgressDTO progress = new TicTacProgressDTO(null, currentState, index);
        history.add(progress);
        return progress;
    }

    @Override
    public BlockState getCurrentRole() {
        return currentState;
    }

    @Override
    public void setCurrentRole(BlockState state) {
        this.currentState = state;
    }

    @Override
    public void resize(int w, int h) {
        scene.resize(w, h);
    }

    @Nullable
    @Override
    public TicTacProgressDTO touchUp(float x, float y) {
        Integer index = scene.touchUpBlock(x, y, currentState);

        if (index != null) {
            return progress(index, currentState);
        }

        return null;
    }

    @Override
    public void connect(GameView gameView) {
        this.scene.connect(gameView);
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
        return Arrays.stream(blocks).anyMatch(b -> b == BlockState.FREE);
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
        BlockState state = blocks[0];
        if (state == BlockState.FREE) return false;

        for (int i = 1; i < COUNT_COLUMNS; i++) {
            if (!blocks[(COUNT_COLUMNS*i) + i].equals(state)) return false;
        }

        return true;
    }

    private boolean winSecondDiagonal() {
        BlockState state = blocks[COUNT_COLUMNS-1];
        if (state == BlockState.FREE) return false;

        for (int i = 1; i < COUNT_COLUMNS; i++) {
            if (!blocks[COUNT_COLUMNS*(i+1)-(i+1)].equals(state)) return false;
        }

        return true;
    }

    // Запоминаем состояние верхнего поля в стоолбце
    // Если оно свободно, то проверка бессмысленна
    // Проверяем, есть ли отличающиеся блоки
    private boolean winColumn(int column) {
        BlockState state = blocks[column];
        if (state == BlockState.FREE) return false;

        for (int i = column + COUNT_COLUMNS; i < 9; i += COUNT_COLUMNS) {
            if (!blocks[i].equals(state)) {
                return false;
            }
        }

        return true;
    }

    // Запоминаем состояние первого элемента строки
    // Если оно свободно, то проверка бессмысленна
    // Проверяем, есть ли отличающиеся блоки
    private boolean winRow(int row) {
        BlockState state = blocks[row*COUNT_COLUMNS];
        if (state == BlockState.FREE) return false;

        for (int j = row*COUNT_COLUMNS+1; j < (row*COUNT_COLUMNS + COUNT_COLUMNS); j++) {
            if (!blocks[j].equals(state)) return false;
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
