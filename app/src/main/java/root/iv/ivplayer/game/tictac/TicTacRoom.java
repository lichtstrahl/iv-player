package root.iv.ivplayer.game.tictac;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.room.Room;
import root.iv.ivplayer.game.room.RoomListener;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.room.RoomStateJump;
import root.iv.ivplayer.game.room.api.FirebaseRoom;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;
import root.iv.ivplayer.network.firebase.FBDataListener;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.dto.FBProgress;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import timber.log.Timber;

public class TicTacRoom extends Room implements FirebaseRoom {
    private String name;
    private TicTacEngine engine;
    @Nullable
    private Listener roomListener;
    private FBRoom fbRoom;
    private FirebaseUser fbUser;
    private List<ValueEventListener> fbObservers;

    public TicTacRoom(TicTacTextures textures, String name, FirebaseUser user) {
        super(new TicTacToeScene(textures));
        this.name = name;
        this.fbUser = user;
        fbObservers = new ArrayList<>();
        TicTacToeScene ticTacToeScene = (TicTacToeScene) scene;
        engine = new TicTacEngine(ticTacToeScene.getAllBlocks());
        scene.getMainController().setTouchHandler(this::touchHandler);
    }

    public void init() {
        RoomObserver roomObserver = new RoomObserver();
        FBDatabaseAdapter.getRoom(name)
                .addValueEventListener(roomObserver);
        fbObservers.add(roomObserver);
    }

    @Override
    public void addListener(RoomListener listener) {
        this.roomListener = (Listener) listener;
    }

    @Override
    public Scene getScene() {
        return scene;
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

    private void touchHandler(MotionEvent event) {
        if (fbRoom.getState() != RoomState.GAME)
            return;

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int oldHistorySize = engine.getHistorySize();
                engine.touchUp(event.getX(), event.getY());

                if (engine.getHistorySize() > oldHistorySize) {
                    TicTacProgressDTO lastProgress = engine.getLastState();
                    publishProgress(lastProgress, engine.win(), !engine.hasFreeBlocks());
                }

                break;
        }
    }

    // Публикуем в соответствующем поле (progressCROSS, progressCIRCLE)
    // А также устанавлиаем поле wait на себя
    private void publishProgress(TicTacProgressDTO lastProgress, boolean win, boolean end) {
        String progressPath = fbRoom.getCurrentProgressPath(fbUser.getEmail());
        FBProgress progress = new FBProgress(lastProgress.getBlockIndex(), win,
                end, engine.getCurrentState(), fbUser.getEmail());
        FBDatabaseAdapter.getProgressInRoom(name, progressPath)
                .setValue(progress);
        FBDatabaseAdapter.getWaitField(name)
                .setValue(fbUser.getEmail());

        if (win)
            win(fbUser.getEmail());
        else if (end)
            end();
    }

    private void win(String uuid) {
        end();
        Timber.i("Игрок %s выиграл", uuid);
        if (roomListener != null) roomListener.win(uuid);
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
    private void startGame(FBRoom newRoom) {
        newRoom.setState(RoomState.GAME);
        engine.setCurrentState(newRoom.getCurrentRole(fbUser.getEmail()));
        FBDatabaseAdapter.getRoomStatus(name).setValue(RoomState.GAME);


        // Подписка на обновления и обновление
        WaitProgressObserver waitProgressObserver = new WaitProgressObserver();
        FBDatabaseAdapter.getWaitField(name).addValueEventListener(waitProgressObserver);
        fbObservers.add(waitProgressObserver);

        if (engine.getCurrentState() == BlockState.CIRCLE) {
            FBDatabaseAdapter.getWaitField(name).setValue(fbUser.getEmail());
            updateLocalStatus(RoomState.GAME);
        } else {
            updateLocalStatus(RoomState.WAIT_PROGRESS);
        }

        String enemyProgressPath = newRoom.getEnemyProgressPath(fbUser.getEmail());
        ProgressObserver progressObserver = new ProgressObserver();
        FBDatabaseAdapter.getProgressInRoom(name, enemyProgressPath)
                .addValueEventListener(progressObserver);
        fbObservers.add(progressObserver);
    }

    // Обновление комнаты (email1, email2 и статуса)
    // Если комната не была создана, то она создаётся как WAIT_PLAYERS
    // Для второго зашедшего игрока необходимо предусмотреть:
    private void updateRoom(FBRoom newRoom) {
        if (fbRoom == null) {
            fbRoom = new FBRoom();
            fbRoom.setState(RoomState.WAIT_PLAYERS);
        }

        fbRoom.setEmailPlayer1(newRoom.getEmailPlayer1());
        fbRoom.setEmailPlayer2(newRoom.getEmailPlayer2());

        // Если игрок ждёт хода, то обновлять менять это не следует, это делается в соответствующем наблюдателе.
        if (fbRoom.getState() != RoomState.WAIT_PROGRESS) {
            updateLocalStatus(newRoom.getState());
        }
    }

    @Override
    public void exitFromRoom() {
        // Ищем путь до email для очистки его
        String currentEmailPath = fbRoom.getCurrentEmailPath(fbUser.getEmail());

        updateLocalStatus(RoomState.CLOSE);
        FBDatabaseAdapter.getPlayerEmail(name, currentEmailPath).removeValue();
        // Отписка от событий Firebase
        for (ValueEventListener listener : fbObservers) {
            FBDatabaseAdapter.getRoom(name).removeEventListener(listener);
        }
    }

    public interface Listener extends RoomListener {
        void updatePlayers(@Nullable String login1, @Nullable String login2);
        void win(String email);
        void end();
        void changeStatus(RoomState roomState);
    }

    // Следим за обновлением хода противника
    class ProgressObserver implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            FBProgress enemyProgress = dataSnapshot.getValue(FBProgress.class);
            if (enemyProgress != null) {
                engine.markBlock(enemyProgress.getIndex(), enemyProgress.getState());
                if (engine.win())
                    win(enemyProgress.getEmail());
                else if (!engine.hasFreeBlocks())
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

            if (waitEmail != null && waitEmail.equals(fbUser.getEmail()))
                updateLocalStatus(RoomState.WAIT_PROGRESS);
            else
                updateLocalStatus(RoomState.GAME);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.w(databaseError.getMessage());
        }
    }

    // Следим за изменением комнаты (вход-выход) игроков
    class RoomObserver extends FBDataListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            FBRoom newRoom = Objects.requireNonNull(dataSnapshot.getValue(FBRoom.class));
            Timber.i("Изменения в комнате");
            if (fbRoom != null) {
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
                        startGame(newRoom);
                    }
                }

                // Выход игрока
                if (newRoom.isLeavePlayer(fbRoom)) {
                    Timber.i("Из комнаты кто-то вышел");
                    updateLocalStatus(RoomState.CLOSE);
                    newRoom.setState(RoomState.CLOSE);
                    FBDatabaseAdapter.getRoom(name)
                            .setValue(newRoom);
                }

                // Обновляем локальные данные о комнате
                updateRoom(newRoom);
            } else { // То есть комната только только открылась. Реагируем если вошли вторым
                updateRoom(newRoom);
                int newCount = newRoom.countPlayer();
                Timber.i("Локальная комната создана, вошли %d-ым", newCount);
                if (newCount == 2) {
                    startGame(newRoom);
                }
            }
            roomListener.updatePlayers(fbRoom.getEmailPlayer1(), fbRoom.getEmailPlayer2());
        }
    }
}
