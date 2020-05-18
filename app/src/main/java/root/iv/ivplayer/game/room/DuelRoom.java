package root.iv.ivplayer.game.room;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.room.api.FirebaseRoom;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.BlockState;
import root.iv.ivplayer.game.tictac.TicTacEngine;
import root.iv.ivplayer.game.tictac.TicTacToeScene;
import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.dto.FBProgress;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import timber.log.Timber;

// Комната для дуэли. Является комнатой и реализует действия для слежения за количеством
public class DuelRoom extends Room implements FirebaseRoom, ValueEventListener {
    private String name;
    private TicTacEngine engine;
    @Nullable
    private Listener roomListener;
    private FBRoom fbRoom;
    private FirebaseUser fbUser;
    private boolean wait;

    public DuelRoom(TicTacTextures textures, String name, FirebaseUser user) {
        super(new TicTacToeScene(textures));
        this.name = name;
        this.fbUser = user;

        engine = new TicTacEngine();
        scene.getMainController().setTouchHandler(this::touchHandler);

        FBDatabaseAdapter.getRoom(name)
                .addValueEventListener(this);
        FBDatabaseAdapter.getRoomStatus(name)
                .addValueEventListener(new RoomStatusObserver());
        this.wait = true;
    }

    @Override
    public void addListener(RoomListener listener) {
        this.roomListener = (Listener) listener;
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    private void updateLocalRoom(RoomState newState) {
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
        if (this.wait)
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
        updateLocalRoom(RoomState.GAME);
        newRoom.setState(RoomState.GAME);
        engine.setCurrentState(newRoom.getCurrentRole(fbUser.getEmail()));
        FBDatabaseAdapter.getRoomStatus(name).setValue(RoomState.GAME);


        // Подписка на обновления и обновление
        FBDatabaseAdapter.getWaitField(name).addValueEventListener(new WaitProgressObserver());
        if (engine.getCurrentState() == BlockState.CIRCLE) {
            FBDatabaseAdapter.getWaitField(name).setValue(fbUser.getEmail());
            this.wait = true;
        } else {
            this.wait = false;
        }

        String enemyProgressPath = newRoom.getEnemyProgressPath(fbUser.getEmail());
        FBDatabaseAdapter.getProgressInRoom(name, enemyProgressPath)
                .addValueEventListener(new ProgressObserver());
    }

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
                updateLocalRoom(RoomState.CLOSE);
                newRoom.setState(RoomState.CLOSE);
                FBDatabaseAdapter.getRoom(name)
                        .setValue(newRoom);
            }

            // Обновляем локальные данные о комнате
            this.fbRoom = newRoom;
        } else { // То есть комната только только открылась. Реагируем если вошли вторым
            this.fbRoom = newRoom;
            int newCount = newRoom.countPlayer();
            Timber.i("Локальная комната создана, вошли %d-ым", newCount);
            if (newCount == 2) {
                startGame(newRoom);
            }
        }
        roomListener.updatePlayers(fbRoom.getEmailPlayer1(), fbRoom.getEmailPlayer2());
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Timber.w(databaseError.getMessage());
    }

    @Override
    public void exitFromRoom() {
        // Ищем путь до email для очистки его
        String currentEmailPath = fbRoom.getCurrentEmailPath(fbUser.getEmail());

        updateLocalRoom(RoomState.CLOSE);
        FBDatabaseAdapter.getPlayerEmail(name, currentEmailPath).removeValue();
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
            wait = waitEmail != null && waitEmail.equals(fbUser.getEmail());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.w(databaseError.getMessage());
        }
    }

    // Следим за изменением статуса комнаты
    class RoomStatusObserver implements  ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            RoomState newState = dataSnapshot.getValue(RoomState.class);
            updateLocalRoom(newState);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.w(databaseError.getMessage());
        }
    }
}
