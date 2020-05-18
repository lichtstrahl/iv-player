package root.iv.ivplayer.network.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FBDatabaseAdapter {
    private static final String PATH_ROOMS = "rooms";
    private static final String FIELD_STATE = "state";
    private static final String FIELD_WAIT = "wait";

    public static DatabaseReference getRooms() {
        return FirebaseDatabase.getInstance().getReference(PATH_ROOMS);
    }

    public static DatabaseReference getRoom(String roomName) {
        return getRooms().child(roomName);
    }

    public static DatabaseReference getRoomStatus(String roomName) {
        return getRooms().child(roomName).child(FIELD_STATE);
    }

    public static DatabaseReference getPlayerEmail(String roomName, String pathEmail) {
        return getRoom(roomName).child(pathEmail);
    }

    public static DatabaseReference getProgressInRoom(String roomName, String progress) {
        return getRoom(roomName).child(progress);
    }

    public static DatabaseReference getWaitField(String roomName) {
        return getRoom(roomName).child(FIELD_WAIT);
    }
}
