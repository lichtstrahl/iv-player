package root.iv.ivplayer.game.tictac;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import root.iv.ivplayer.game.room.FirebaseRoom;
import root.iv.ivplayer.game.room.RoomListener;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;
import root.iv.ivplayer.game.view.GameView;
import root.iv.ivplayer.network.firebase.FBDataListener;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.dto.FBTicTacProgress;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import timber.log.Timber;

public class TicTacRoom extends FirebaseRoom {
    private TicTacEngineAPI engine;
    @Nullable
    private Listener roomListener;

    public TicTacRoom(TicTacTextures textures, String name, FirebaseUser user) {
        super(name, user);
        engine = new TicTacEngine(textures, this::touchHandler);
    }

    @Override
    public void init() {
        RoomObserver roomObserver = new RoomObserver();
        registerRoomObserver(roomObserver);
    }

    @Override
    public void addListener(RoomListener listener) {
        this.roomListener = (Listener) listener;
    }

    private void touchHandler(MotionEvent event) {
        Timber.i("touch: %s, %d", fbRoom.getState().name(), event.getAction());
        if (fbRoom.getState() != RoomState.GAME)
            return;


        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                TicTacProgressDTO progress = engine.touchUp(event.getX(), event.getY());
                if (progress != null) {
                    publishProgress(progress, engine.win(), engine.end());
                }
                break;
        }
    }

    private void updateStatus(RoomState state) {
        boolean updated = updateLocalStatus(state);
        if (updated && roomListener != null)
            roomListener.changeStatus(state);

    }

    // Публикуем в соответствующем поле (progressCROSS, progressCIRCLE)
    // А также устанавлиаем поле wait на себя
    private void publishProgress(TicTacProgressDTO lastProgress, boolean win, boolean end) {
        String progressPath = fbRoom.getCurrentProgressPath(fbUser.getUid());
        FBTicTacProgress progress = new FBTicTacProgress(lastProgress.getBlockIndex(), win,
                end, engine.getCurrentRole(), fbUser.getUid());
        FBDatabaseAdapter.getProgressInRoom(name, progressPath)
                .setValue(progress);
        FBDatabaseAdapter.getWaitField(name)
                .setValue(fbUser.getUid());

        if (win)
            win(fbUser.getUid());
        else if (end)
            end();
    }

    private void win(String uid) {
        end();


        Timber.i("Игрок %s выиграл", uid);
        if (roomListener != null) roomListener.win(fbRoom.numberPlayer(uid));
    }

    private void end() {
        Timber.i("Игра окончена");
        if (roomListener != null) roomListener.end();
    }

    private void log(String prefix, TicTacProgressDTO progress) {
        Timber.i("%s Ход %s: %d %s", prefix, progress.getUuid(), progress.getBlockIndex(), progress.getState().name());
    }

    // Начало игры (передаём состояние комнаты, которое к этому привело)
    // При запуске необходимо подписаться на обновления прогресса противоположного игрока
    // Если начал игру ноликами, то установка флага ожидания на себя
    private void startGame(FBRoom newRoom, BlockState currentRole) {
        newRoom.setState(RoomState.GAME);
        engine.setCurrentRole(currentRole);
        FBDatabaseAdapter.getRoomStatus(name)
                .setValue(RoomState.GAME);


        // Подписка на обновления флага ожидания
        WaitProgressObserver waitProgressObserver = new WaitProgressObserver();
        FBDatabaseAdapter.getWaitField(name)
                .addValueEventListener(waitProgressObserver);
        addFBObserver(waitProgressObserver);

        if (currentRole == BlockState.CIRCLE) {
            FBDatabaseAdapter.getWaitField(name).setValue(fbUser.getUid());
            updateStatus(RoomState.WAIT_PROGRESS);
        } else {
            updateStatus(RoomState.GAME);
        }

        String enemyProgressPath = newRoom.getEnemyProgressPath(fbUser.getUid());
        ProgressObserver progressObserver = new ProgressObserver();
        FBDatabaseAdapter.getProgressInRoom(name, enemyProgressPath)
                .addValueEventListener(progressObserver);
        addFBObserver(progressObserver);
    }

    // Обновление комнаты (email1, email2 и статуса)
    // Если комната не была создана, то она создаётся как WAIT_PLAYERS
    // Для второго зашедшего игрока необходимо предусмотреть:
    private void updateRoom(FBRoom newRoom) {
        updateRoomPlayersAndStatus(newRoom);

        // Если игрок ждёт хода, то обновлять статус не следует, это делается в соответствующем наблюдателе.
        if (fbRoom.getState() != RoomState.WAIT_PROGRESS) {
            updateStatus(newRoom.getState());
        }
    }

    @Override
    public void exit() {
        // Ищем путь до uid для очистки его
        String currentEmailPath = fbRoom.getCurrentPlayerPath(fbUser.getUid());

        updateStatus(RoomState.CLOSE);
        FBDatabaseAdapter.getPlayerEmail(name, currentEmailPath).removeValue();
        // Отписка от событий Firebase
        unsubscribeFirebaseAll();
    }

    @Override
    public void connect(GameView gameView) {
        engine.connect(gameView);
    }

    @Override
    public void resize(int width, int height) {
        engine.resize(width, height);
    }


    public interface Listener extends RoomListener {
        void updatePlayers(@Nullable String displayName1, @Nullable String displayName2);
        void win(int numberPlayer);
        void end();
        void changeStatus(RoomState roomState);
    }

    // ---
    // Observers
    // ---

    // Следим за обновлением хода противника
    class ProgressObserver implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            FBTicTacProgress enemyProgress = dataSnapshot.getValue(FBTicTacProgress.class);
            if (enemyProgress != null) {
                engine.progress(enemyProgress.getIndex(), enemyProgress.getState());
                if (engine.win())
                    win(enemyProgress.getUid());
                else if (engine.end())
                    end();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.w(databaseError.getMessage());
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

    // Следим за изменением комнаты (вход-выход) игроков + внешнее закрытие комнаты
    class RoomObserver extends FBDataListener {
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
                        startGame(newRoom, BlockState.CROSS);
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
                    startGame(newRoom, BlockState.CIRCLE);
                }
            }
            roomListener.updatePlayers(fbRoom.name1(), fbRoom.name2());
        }
    }
}
