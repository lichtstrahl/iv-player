package root.iv.ivplayer.game.fanorona.slot;

public enum SlotState {
    DEFAULT,        // Обычное состояние
    SELECTED,       // Ячейка выбрана
    PROGRESS,       // Ячейка помечена как готовая к ходу
    HAS_PROGRESS,   // Ячейка имеет ходы
    MARK_FOR_ATTACK,// Помечена для выбора направления атаки
}
