package root.iv.ivplayer.game.fanorona.room;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.util.concurrent.Executors;

import root.iv.ivplayer.game.fanorona.FanoronaEngine;
import root.iv.ivplayer.game.fanorona.FanoronaRole;
import root.iv.ivplayer.game.fanorona.dto.FanoronaProgressDTO;
import root.iv.ivplayer.game.fanorona.textures.FanoronaTextures;
import root.iv.ivplayer.game.room.FirebaseRoom;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.room.listeners.RoomListener;
import root.iv.ivplayer.game.view.GameView;
import root.iv.ivplayer.network.firebase.FBDataListener;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.dto.FBFanoronaProgress;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import timber.log.Timber;

public class FanoronaRoom extends FirebaseRoom {
    private FanoronaEngine engine;
    private FanoronaRoomListener roomListener;

    FanoronaRoom(FanoronaTextures textures, String name, FirebaseUser user, FanoronaRole role) {
        super(name, user);
        Timber.i("Fanorona room create");
        engine = new FanoronaEngine(textures, this::touchHandler);
        engine.setCurrentRole(role);
    }

    @Override
    public void addListener(RoomListener listener) {
        this.roomListener = (FanoronaRoomListener) listener;
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
        // Проверяем, что такого слушаетеля до этого не было
        if (observerExist(RoomObserver.class))
            return;

        // Подписка на событие изменения комнаты
        RoomObserver roomObserver = new RoomObserver();
        registerRoomObserver(roomObserver);
    }

    private void touchHandler(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            switch (fbRoom.getState()) {
                case GAME:
                    processTouchGame(event.getX(), event.getY());
                    break;

                case SELECT_ATTACK_TYPE:
                    processTouchSelectAttackType(event.getX(), event.getY());
                    break;
            }
        }
    }

    // Обработка нажатия GAME
    private void processTouchGame(float x, float y) {
        FanoronaProgressDTO lastProgress = engine.touch(x,y);

        // Если ход был сделан и он оказался последним в цепочке
        if (lastProgress != null && engine.endProgressChain()) {
            boolean win = engine.win();
            // Делаем ход
            makeProgress(win);

            // Если выиграли
            if (win) {
                endGame(EndGameType.WIN);
            }
        } else if (engine.possibleDoubleAttack(x, y)) { // Ход не обработан т.к. возможны два направления атаки
            updateStatus(RoomState.SELECT_ATTACK_TYPE);
            engine.markSlotsForAttack(x, y);
        }
    }

    private void processTouchSelectAttackType(float x, float y) {
        FanoronaProgressDTO progress = engine.selectAttackType(x, y);

        // Если ход был сделан, то переходим в состояние GAME
        if (progress != null) {
            updateStatus(RoomState.GAME);


            // Если ход был последним в цепочке
            if (engine.endProgressChain()) {
                // Ход после выбора направления атаки не может быть победным
                makeProgress(false);
            }
        }
    }

    // Делаем ход:
    // Обновляем свою структуру хода. Обновляем поле ожидания, переставляя его на себя.
    // Отправляем последовательность ходов в новом потоке. Чтобы игрок не видел задержки
    private void makeProgress(boolean win) {
        String progressPath = fbRoom.getCurrentProgressPath(fbUser.getUid());

        Executors.newFixedThreadPool(1)
                .execute(() -> {
                    engine.processProgressChain(pDTO -> {
                        FBFanoronaProgress fbProgress = new FBFanoronaProgress(fbUser.getUid(), engine.getCurrentRole(),
                                pDTO.getFrom(), pDTO.getTo(), pDTO.getAttack(), win);

                        FBDatabaseAdapter.getProgressInRoom(name, progressPath)
                                .setValue(fbProgress);
                    });

                    FBDatabaseAdapter.getWaitField(name)
                            .setValue(fbUser.getUid());
                });
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

    private void startGame(FBRoom newRoom, FanoronaRole currentRole) {
        engine.setCurrentRole(currentRole);
        newRoom.setState(RoomState.GAME);
        FBDatabaseAdapter.getRoomStatus(name)
                .setValue(RoomState.GAME);


        // Подписка на обновления флага ожидания
        WaitProgressObserver waitProgressObserver = new WaitProgressObserver();
        FBDatabaseAdapter.getWaitField(name)
                .addValueEventListener(waitProgressObserver);
        addFBObserver(waitProgressObserver);

        if (currentRole == FanoronaRole.WHITE) {
            FBDatabaseAdapter.getWaitField(name).setValue(fbUser.getUid());
            updateStatus(RoomState.WAIT_PROGRESS);
        } else {
            updateStatus(RoomState.GAME);
        }

        // Подписка на ходы соперника
        String enemyProgressPath = newRoom.getEnemyProgressPath(fbUser.getUid());
        ProgressObserver progressObserver = new ProgressObserver();
        FBDatabaseAdapter.getProgressInRoom(name, enemyProgressPath)
                .addValueEventListener(progressObserver);
        addFBObserver(progressObserver);
    }

    private void endGame(EndGameType endType) {
        updateStatus(RoomState.CLOSE);

        switch (endType) {
            case WIN:
                roomListener.win();
                break;

            case LOSE:
                roomListener.lose();
                break;
        }
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
                        startGame(newRoom, FanoronaRole.WHITE);
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
                    startGame(newRoom, FanoronaRole.BLACK);
                }
            }

            roomListener.updatePlayers(fbRoom.name1(), fbRoom.name2());
        }
    }

    // Следим за обновлением поля WAIT (кто ждёт ход)
    private class WaitProgressObserver implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String waitEmail = dataSnapshot.getValue(String.class);

            if (waitEmail != null && waitEmail.equals(fbUser.getUid()))
                updateStatus(RoomState.WAIT_PROGRESS);
            else if (waitEmail != null) {
                engine.clearEnemyChain();
                engine.markPossibleProgress(engine.getCurrentRole());
                updateStatus(RoomState.GAME);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.w(databaseError.getMessage());
        }
    }

    enum EndGameType {
        WIN, LOSE
    }

    // Следим за обновлением хода противника
    private class ProgressObserver implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            FBFanoronaProgress enemyProgress = dataSnapshot.getValue(FBFanoronaProgress.class);
            if (enemyProgress != null) {
                engine.progress(
                        enemyProgress.getFrom(), enemyProgress.getTo(),
                        enemyProgress.getState(), enemyProgress.getAttack());
                engine.enemyStep();

                // Если соперник утверждает, что его ход победный.
                // И после него игра закончилась значит не было рассинхронов.
                if (enemyProgress.isWin()) {
                    if (engine.end())
                        endGame(EndGameType.LOSE);
                    else
                        Timber.e("Принят победный ход соперника. Однако это не соответствует состоянию локального engine");
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.w(databaseError.getMessage());
        }
    }
}
