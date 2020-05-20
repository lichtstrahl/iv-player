package root.iv.ivplayer.game.tictac;

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
 * 2. Отдавать и получать информацию о текущей роли игрока
 */
public interface TicTacEngineAPI extends Engine, StepEngine<TicTacProgressDTO>, SensorEngine {
    void markBlock(int index, BlockState state);
    BlockState getCurrentRole();
    void setCurrentRole(BlockState state);
}
