package root.iv.ivplayer.game.room;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import timber.log.Timber;

public abstract class FirebaseRoom extends Room {
    protected FBRoom fbRoom;
    protected FirebaseUser fbUser;
    private List<ValueEventListener> fbObservers;

    public FirebaseRoom(String name, FirebaseUser user) {
        super(name);

        this.fbUser = user;
        fbObservers = new ArrayList<>();
    }

    protected void updateRoomPlayersAndStatus(FBRoom newRoom) {
        if (fbRoom == null) {
            fbRoom = new FBRoom();
            fbRoom.setState(RoomState.WAIT_PLAYERS);
        }

        fbRoom.setPlayer1(newRoom.getPlayer1());
        fbRoom.setPlayer2(newRoom.getPlayer2());

        // Если игрок ждёт хода, то обновлять статус не следует, это делается в соответствующем наблюдателе.
        if (fbRoom.getState() != RoomState.WAIT_PROGRESS) {
            updateLocalStatus(newRoom.getState());
        }
    }

    protected boolean updateLocalStatus(RoomState newState) {
        boolean transit = RoomStateJump.of(fbRoom.getState()).possibleTransit(newState);
        if (transit) {
            Timber.i("%s -> %s", fbRoom.getState().name(), newState.name());
            fbRoom.setState(newState);
            return true;
        }

        Timber.w("Переход %s -> %s невозможен", fbRoom.getState().name(), newState.name());
        return false;
    }

    protected void addFBObserver(ValueEventListener listener) {
        fbObservers.add(listener);
    }

    protected <T> boolean observerExist(Class<T> clazz) {
        return fbObservers
                .stream()
                .map(ValueEventListener::getClass)
                .anyMatch(cls -> cls.equals(clazz));
    }


    // Регистрация главного наблюдателя за FB-комнатой
    protected void registerRoomObserver(ValueEventListener roomObserver) {
        FBDatabaseAdapter.getRoom(name)
                .addValueEventListener(roomObserver);
        addFBObserver(roomObserver);
    }

    // Полная отписка от всего Firebase
    protected void unsubscribeFirebaseAll() {
        for (ValueEventListener listener : fbObservers) {
            FBDatabaseAdapter.getRoom(name).removeEventListener(listener);
        }
    }
}
