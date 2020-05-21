package root.iv.ivplayer.game.fanorona;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import root.iv.ivplayer.game.room.Room;
import root.iv.ivplayer.game.room.RoomListener;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.room.RoomStateJump;
import root.iv.ivplayer.game.view.GameView;
import root.iv.ivplayer.network.firebase.FBDataListener;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import timber.log.Timber;

public class FanoronaRoom extends Room {
    private FanoronaEngine engine;
    @Nullable
    private Listener roomListener;
    private FBRoom fbRoom;
    private FirebaseUser fbUser;
    private List<ValueEventListener> fbObservers;

    public FanoronaRoom(FanoronaTextures textures, String name, FirebaseUser user) {
        super(name);

        this.fbUser = user;
        fbObservers = new ArrayList<>();

        engine = new FanoronaEngine(textures, this::touchHandler);
    }

    @Override
    public void addListener(RoomListener listener) {
        this.roomListener = (Listener) listener;
    }

    @Override
    public void resize(int width, int height) {
        // resize пока не реализован у FanoronaScene
    }

    @Override
    public void connect(GameView gameView) {
        engine.connect(gameView);
    }

    @Override
    public void exit() {
        // Ищем путь до своего игрока для его очистки
        String currentPlayerPath = fbRoom.getCurrentPlayerPath(fbUser.getUid());

        // Удаляем информацию о себе из комнаты
        FBDatabaseAdapter.getPlayerEmail(name, currentPlayerPath).removeValue();

        // Отписка от всех событий FB
        for (ValueEventListener listener : fbObservers)
            FBDatabaseAdapter.getRoom(name).removeEventListener(listener);
    }

    @Override
    public void init() {
        // Здесь подписка на событие изменения комнаты
        RoomObserver roomObserver = new RoomObserver();
        FBDatabaseAdapter.getRoom(name)
                .addValueEventListener(roomObserver);
        fbObservers.add(roomObserver);
    }

    private void touchHandler(MotionEvent event) {

    }

    private void updateLocalStatus(RoomState newState) {
        boolean transit = RoomStateJump.of(fbRoom.getState()).possibleTransit(newState);
        if (transit) {
            Timber.i("%s -> %s", fbRoom.getState().name(), newState.name());
            fbRoom.setState(newState);
            if (roomListener != null) {
                roomListener.changeStatus(fbRoom.getState());
            }
        }
        else
            Timber.w("Переход %s -> %s невозможен", fbRoom.getState().name(), newState.name());
    }

    private void updateRoom(FBRoom newRoom) {
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

    public interface Listener extends RoomListener {
        void changeStatus(RoomState roomState);
        void updatePlayers(@Nullable String displayName1, @Nullable String displayName2);
    }

    // ---
    // Observers firebase
    // ---

    private class RoomObserver extends FBDataListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            FBRoom newRoom = Objects.requireNonNull(dataSnapshot.getValue(FBRoom.class));

            if (fbRoom != null) { // Уже в комнате

            } else { // Только вошли в игру
            }

            updateRoom(newRoom);
            roomListener.updatePlayers(fbRoom.name1(), fbRoom.name2());
        }
    }
}
