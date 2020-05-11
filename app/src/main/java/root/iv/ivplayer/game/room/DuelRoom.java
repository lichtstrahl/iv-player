package root.iv.ivplayer.game.room;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import io.reactivex.disposables.CompositeDisposable;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.DrawableBlockState;
import root.iv.ivplayer.game.tictac.TicTacEngine;
import root.iv.ivplayer.game.tictac.TicTacJsonProcessor;
import root.iv.ivplayer.game.tictac.TicTacToeScene;
import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import timber.log.Timber;

// Комната для дуэли. Является комнатой и реализует действия для слежения за количеством
public class DuelRoom extends Room implements FirebaseRoom, ValueEventListener {
    private String name;
    private Scene scene;
    private TicTacEngine engine;
    private TicTacJsonProcessor jsonProcessor;
    @Nullable
    private Listener roomListener;
    private DrawableBlockState icons;
    private FBRoom fbRoom;
    private FirebaseUser fbUser;



    public DuelRoom(TicTacTextures textures, String name, FirebaseUser user) {
        super(2);
        this.name = name;
        this.fbUser = user;

        engine = new TicTacEngine();
        scene = new TicTacToeScene(textures, engine);
        scene.getMainController().setTouchHandler(this::touchHandler);

        jsonProcessor = new TicTacJsonProcessor();

        App.getRoom(name)
                .addValueEventListener(this);
    }


    @Override
    public void joinPlayer(String uuid) {
    }

    @Override
    public void addListener(RoomListener listener) {
        this.roomListener = (Listener) listener;
    }

    @Override
    public void removeListener() {
        this.roomListener = null;
    }

    @Override
    public void leavePlayer(String uuid) {
    }

    @Override
    public void receiveMsg(PNMessageResult msg) {
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public RoomState getRoomState() {
        return fbRoom.getState();
    }

    private void changeState(RoomState newState) {
            boolean transit = RoomStateJump.of(fbRoom.getState()).possibleTransit(newState);
            if (transit) {
                fbRoom.setState(newState);
                Timber.i("%s -> %s", fbRoom.getState().name(), newState.name());
                if (roomListener != null) {
                    roomListener.changeStatus(fbRoom.getState());
                }
            }
            else
                Timber.w("Переход %s -> %s невозможен", fbRoom.getState().name(), newState.name());
    }

    private void touchHandler(MotionEvent event) {
    }

    private void win(String uuid) {
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

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        FBRoom newRoom = dataSnapshot.getValue(FBRoom.class);

        if (fbRoom != null) {

            int oldCount = fbRoom.countPlayer();
            int newCount = newRoom.countPlayer();
            // Вход игрока
            if (newCount > oldCount) {
                // В пустую комнату
                if (oldCount == 0) {
                    Timber.i("Вход в пустую комнату");
                }

                // Вторым игроком
                if (oldCount == 1) {
                    changeState(RoomState.GAME);
                }
            }

            // Выход игрока
            if (newCount < oldCount) {
                changeState(RoomState.CLOSE);
            }
        }
        // Другие изменения
        this.fbRoom = newRoom;
        roomListener.updatePlayers(fbRoom.getEmailPlayer1(), fbRoom.getEmailPlayer2());
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Timber.w(databaseError.getMessage());
    }

    @Override
    public void exitFromRoom() {
        if (fbRoom.getEmailPlayer1().equals(fbUser.getEmail())) {
            fbRoom.setEmailPlayer1("");
        }

        if (fbRoom.getEmailPlayer2().equals(fbUser.getEmail())) {
            fbRoom.setEmailPlayer2("");
        }

        changeState(RoomState.CLOSE);

        App.getRoom(name)
                .setValue(fbRoom);
    }

    public interface Listener extends RoomListener {
        void updatePlayers(@Nullable String login1, @Nullable String login2);
        void win(String uuid);
        void end();
        void changeStatus(RoomState roomState);
        void exit();
    }
}
