package root.iv.ivplayer.game.room.api;

import root.iv.ivplayer.game.room.RoomListener;
import root.iv.ivplayer.game.scene.Scene;

public interface FirebaseRoom {
    void exitFromRoom();
    Scene getScene();
    void addListener(RoomListener listener);
}
