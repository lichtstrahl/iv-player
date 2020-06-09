package root.iv.ivplayer.game.fanorona;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.Objects;

import root.iv.ivplayer.game.room.FirebaseRoom;
import root.iv.ivplayer.game.room.RoomListener;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.room.RoomStateJump;
import root.iv.ivplayer.game.view.GameView;
import root.iv.ivplayer.network.firebase.FBDataListener;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import timber.log.Timber;

public class FanoronaRoom extends FirebaseRoom {
    private FanoronaEngine engine;
    @Nullable
    private Listener roomListener;

    public FanoronaRoom(FanoronaTextures textures, String name, FirebaseUser user) {
        super(name, user);

        engine = new FanoronaEngine(textures, this::touchHandler);
    }

    @Override
    public void addListener(RoomListener listener) {
        this.roomListener = (Listener) listener;
    }

    @Override
    public void resize(int width, int height) {
        engine.resize(width, height);
    }

    @Override
    public void connect(GameView gameView) {
        engine.connect(gameView);
    }

    @Override
    public void exit() {
        // Выход вызвался до входа. Происходит при повороте экрана.
        if (fbRoom == null)
            return;

        // Ищем путь до своего игрока для его очистки
        String currentPlayerPath = fbRoom.getCurrentPlayerPath(fbUser.getUid());

        // Удаляем информацию о себе из комнаты
        FBDatabaseAdapter.getPlayerEmail(name, currentPlayerPath).removeValue();

        // Отписка от всех событий FB
        unsubscribeFirebaseAll();
    }

    @Override
    public void init() {
        // Здесь подписка на событие изменения комнаты
        RoomObserver roomObserver = new RoomObserver();
        registerRoomObserver(roomObserver);
    }

    private void touchHandler(MotionEvent event) {

    }

    private void updateStatus(RoomState newState) {
        boolean updated = updateLocalStatus(newState);
        if (updated && roomListener != null)
            roomListener.changeStatus(newState);
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
            updateStatus(newRoom.getState());
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

                updateRoom(newRoom);
            } else { // Только вошли в игру
                updateRoom(newRoom);
            }


            roomListener.updatePlayers(fbRoom.name1(), fbRoom.name2());
        }
    }
}
