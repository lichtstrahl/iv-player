package root.iv.ivplayer.game.room;

import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;

import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.scene.MPScene;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.DrawableBlockState;
import root.iv.ivplayer.game.tictac.TicTacEngine;
import root.iv.ivplayer.game.tictac.TicTacJsonProcessor;
import root.iv.ivplayer.game.tictac.TicTacToeScene;
import root.iv.ivplayer.game.tictac.dto.TicTacDTOType;
import root.iv.ivplayer.game.tictac.dto.TicTacEndDTO;
import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;
import root.iv.ivplayer.game.tictac.dto.TicTacRoomStatusDTO;
import root.iv.ivplayer.network.ws.WSHolder;
import root.iv.ivplayer.network.ws.WSUtil;
import timber.log.Timber;

// Комната для дуэли. Является комнатой и реализует действия для слежения за количеством
public class DuelRoom extends Room implements WSRoom {
    private Scene scene;
    private TicTacEngine engine;
    private TicTacJsonProcessor jsonProcessor;
    @Nullable
    private Listener roomListener;
    private DrawableBlockState icons;
    private WSHolder wsHolder;



    public DuelRoom(TicTacTextures textures) {
        super(2);
        wsHolder = WSHolder.fromURL(WSUtil.springWSURL("/ws/tic-tac", true));

        engine = new TicTacEngine();
        scene = new TicTacToeScene(textures, engine);
        scene.getMainController().setTouchHandler(this::touchHandler);
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

    // Первый вошедший играет крестиками
    // Если игрок вошел не первым в комнату, то отыгрываем событие "второй игрок" подключился
    // Ведь он уже здесь
    private void hereNowProcess(PNHereNowResult result, PNStatus status) {
    }

    @Override
    public void leavePlayer(String uuid) {
    }

    @Override
    public void receiveMsg(PNMessageResult msg) {
        String body = msg.getMessage().getAsString();
        TicTacDTOType dtoType = jsonProcessor.dtoType(body);

        switch (dtoType) {
            case PROGRESS:
                TicTacProgressDTO progress = jsonProcessor.receiveProgressDTO(body);
                log("receive:", progress);
                engine.markBlock(progress.getBlockIndex(), progress.getState());
                changeState(RoomState.GAME);
                break;

            case END:
                TicTacEndDTO end = jsonProcessor.receiveWinDTO(body);
                changeState(RoomState.CLOSE);
                if (end.isWin())
                    win(end.getUuid());
                else
                    end();
                break;

            case ROOM_STATE:
                TicTacRoomStatusDTO roomStatusDTO = jsonProcessor.reciveStatusRoomDTO(body);
                Timber.i("Вход в закрытую комнату");
                if (roomStatusDTO.getRoomState() == RoomState.CLOSE && roomListener != null) {
                    roomListener.exit();
                    removeListener();
                }
                break;
        }
    }

    @Override
    public Scene getScene() {
        return scene;
    }

    @Override
    public RoomState getRoomState() {
        return state;
    }

    private void changeState(RoomState newState) {
            boolean transit = RoomStateJump.of(this.state).possibleTransit(newState);
            if (transit) {
                Timber.i("%s -> %s", this.state.name(), newState.name());
                this.state = newState;
                if (roomListener != null) {
                    roomListener.changeStatus(this.state);
                }
            }
            else
                Timber.w("Переход %s -> %s невозможен", this.state.name(), newState.name());
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
    public void openWS() {
        if (!wsHolder.isOpened())
            wsHolder.open(this::receiveWSMsg);
    }

    @Override
    public void closeWS() {
        wsHolder.close();
    }

    private void receiveWSMsg(String msg) {

    }

    public interface Listener extends RoomListener {
        void updatePlayers(@Nullable String login1, @Nullable String login2);
        void win(String uuid);
        void end();
        void changeStatus(RoomState roomState);
        void exit();
    }
}
