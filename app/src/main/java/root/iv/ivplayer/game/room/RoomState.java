package root.iv.ivplayer.game.room;

import lombok.AllArgsConstructor;
import lombok.Getter;

// Возможные состояния комнат и переходы между ними
@AllArgsConstructor
public enum RoomState {
    WAIT_PROGRESS("Ожидание хода игрока"),
    WAIT_PLAYERS("Ожидание игроков"),
    GAME("Игра"),
    PAUSE("Пауза"),
    CLOSE("Игровая комната закрыта"),
    NEW("Комната создана");

    @Getter
    private String description;
}
