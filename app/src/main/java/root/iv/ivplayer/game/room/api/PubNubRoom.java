package root.iv.ivplayer.game.room.api;

import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import root.iv.ivplayer.game.room.listeners.RoomListener;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.scene.Scene;

/**
 * Комната создана для того, чтобы контролировать количество игроков,
 * а также обрабатывать приходящие сообщение в канале.
 * + Достаточно ли игроков?
 * + Запущена ли комната?
 * + Постановка на паузу
 */
public interface PubNubRoom {
    void joinPlayer(String uuid);
    void leavePlayer(String uuid);
    void receiveMsg(PNMessageResult msg);
    Scene getScene();
    RoomState getRoomState();
    void addListener(RoomListener listener);
    void removeListener();
}

