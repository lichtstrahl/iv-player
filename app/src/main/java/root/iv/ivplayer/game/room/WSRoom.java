package root.iv.ivplayer.game.room;

/**
 * Комната, работающая с открытым WS-соединением
 * 1. Открывает WS
 * 2. Закрывает WS
  */

public interface WSRoom extends PlayerRoom {
    void openWS(); // Обработчик должен передаваться на этапе создания комнаты
    void closeWS();
}
