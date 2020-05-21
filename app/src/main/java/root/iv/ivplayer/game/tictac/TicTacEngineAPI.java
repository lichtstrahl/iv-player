package root.iv.ivplayer.game.tictac;

import java.util.List;

import root.iv.ivplayer.game.engine.Engine;
import root.iv.ivplayer.game.engine.SensorEngine;
import root.iv.ivplayer.game.engine.StepEngine;
import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;

/**
 * Движок для крестиков-ноликов. Является:
 * 1. Обычным движком, отслеживающим конец игры
 * 2. Сенсорным, реагирующим на касания
 * 3. Пошаговым, т.е. осуществляющим хранение истории ходов и дающий информацию о последнем ходе
 *
 * Помимо этого может:
 * 1. Помечать какую-то клетку
 * 2. Сделать ход (пометив клетку и запомнив ход в историю)
 * 3. Отдавать и получать информацию о текущей роли игрока
 * 4. Загружать новые блоки (для смены размеров)
 */
public interface TicTacEngineAPI extends Engine, StepEngine<TicTacProgressDTO>, SensorEngine {
    void markBlock(int index, BlockState state);
    void progress(int index, BlockState state);
    BlockState getCurrentRole();
    void setCurrentRole(BlockState state);
    void loadBlocks(List<Block> blocks);
}
