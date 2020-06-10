package root.iv.ivplayer.game.fanorona;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import root.iv.ivplayer.game.fanorona.slot.SlotState;
import root.iv.ivplayer.game.room.FirebaseRoom;
import root.iv.ivplayer.game.room.RoomListener;
import root.iv.ivplayer.game.room.RoomState;
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
        engine.setCurrentState(SlotState.BLACK);
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
//        if (fbRoom.getState() != RoomState.GAME)
//            return;

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                engine.touch(event.getX(), event.getY());
                break;
        }

    }

    private void updateStatus(RoomState state) {
        boolean updated = updateLocalStatus(state);
        if (updated && roomListener != null)
            roomListener.changeStatus(state);

    }

    private void updateRoom(FBRoom newRoom) {
        updateRoomPlayersAndStatus(newRoom);

        // Если игрок ждёт хода, то обновлять статус не следует, это делается в соответствующем наблюдателе.
        if (fbRoom.getState() != RoomState.WAIT_PROGRESS) {
            updateStatus(newRoom.getState());
        }
    }

    private void startGame(FBRoom newRoom, SlotState currentRole) {
        newRoom.setState(RoomState.GAME);
        engine.setCurrentState(currentRole);
        FBDatabaseAdapter.getRoomStatus(name)
                .setValue(RoomState.GAME);


        // Подписка на обновления флага ожидания
        WaitProgressObserver waitProgressObserver = new WaitProgressObserver();
        FBDatabaseAdapter.getWaitField(name)
                .addValueEventListener(waitProgressObserver);
        addFBObserver(waitProgressObserver);

        if (currentRole == SlotState.WHITE) {
            FBDatabaseAdapter.getWaitField(name).setValue(fbUser.getUid());
            updateStatus(RoomState.WAIT_PROGRESS);
        } else {
            updateStatus(RoomState.GAME);
        }

        // Подписка на ходы соперника
//        String enemyProgressPath = newRoom.getEnemyProgressPath(fbUser.getUid());
//        TicTacRoom.ProgressObserver progressObserver = new TicTacRoom.ProgressObserver();
//        FBDatabaseAdapter.getProgressInRoom(name, enemyProgressPath)
//                .addValueEventListener(progressObserver);
//        addFBObserver(progressObserver);
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
            Timber.i("Изменения в комнате");
            if (fbRoom != null) { // Т.е. мы уже находимся в комнате
                int oldCount = fbRoom.countPlayer();
                // Вход игрока
                if (newRoom.isJoinPlayer(fbRoom)) {
                    Timber.i("В комнату кто-то вошёл");
                    // В пустую комнату
                    if (oldCount == 0) {
                        Timber.i("Вход в пустую комнату");
                    }

                    // Вторым игроком
                    if (oldCount == 1) {
                        startGame(newRoom, SlotState.BLACK);
                    }
                }

                // Выход игрока
                if (newRoom.isLeavePlayer(fbRoom)) {
                    Timber.i("Из комнаты кто-то вышел");
                    updateStatus(RoomState.CLOSE);
                    newRoom.setState(RoomState.CLOSE);
                    FBDatabaseAdapter.getRoom(name)
                            .setValue(newRoom);
                }

                // Обновляем локальные данные о комнате
                updateRoom(newRoom);
            } else { // То есть это собственный вход в комнату
                updateRoom(newRoom);
                int newCount = newRoom.countPlayer();
                Timber.i("Локальная комната создана, вошли %d-ым", newCount);
                if (newCount == 2) {
                    startGame(newRoom, SlotState.WHITE);
                }
            }

            roomListener.updatePlayers(fbRoom.name1(), fbRoom.name2());
        }
    }

    // Следим за обновлением поля WAIT (кто ждёт ход)
    class WaitProgressObserver implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String waitEmail = dataSnapshot.getValue(String.class);

            if (waitEmail != null && waitEmail.equals(fbUser.getUid()))
                updateStatus(RoomState.WAIT_PROGRESS);
            else
                updateStatus(RoomState.GAME);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.w(databaseError.getMessage());
        }
    }
}
