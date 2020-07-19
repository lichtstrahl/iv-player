package root.iv.ivplayer.game.fanorona.room;

import android.view.MotionEvent;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import root.iv.bot.BotAPI;
import root.iv.bot.FanoronaBot;
import root.iv.bot.Progress;
import root.iv.bot.Role;
import root.iv.ivplayer.game.fanorona.FanoronaEngine;
import root.iv.ivplayer.game.fanorona.FanoronaRole;
import root.iv.ivplayer.game.fanorona.FanoronaTextures;
import root.iv.ivplayer.game.fanorona.dto.FanoronaProgressDTO;
import root.iv.ivplayer.game.room.Room;
import root.iv.ivplayer.game.room.RoomListener;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.room.RoomStateJump;
import root.iv.ivplayer.game.view.GameView;
import timber.log.Timber;

public class FanoronaLocalRoom extends Room {
    private FanoronaEngine engine;
    @Nullable
    private FanoronaRoomListener roomListener;
    private BotAPI bot;
    private RoomState state;

    FanoronaLocalRoom(FanoronaTextures textures, FanoronaRole role) {
        super("");
        engine = new FanoronaEngine(textures, this::touchHandler);
        bot = FanoronaBot.defaultSize(role == FanoronaRole.BLACK ? Role.BLACK : Role.WHITE);
        engine.setCurrentRole(role);
        state = role == FanoronaRole.BLACK ? RoomState.GAME : RoomState.WAIT_PROGRESS;
    }

    @Override
    public void addListener(RoomListener listener) {
        this.roomListener = (FanoronaRoomListener) listener;
        roomListener.changeStatus(state);
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
        // Здесь выполнялись только действия для Firebase
    }

    @Override
    public void init() {
        // Подписка на события от Firebase
    }

    private void updateState(RoomState newState) {
        if (RoomStateJump.of(state).possibleTransit(newState)) {
            state = newState;
            roomListener.changeStatus(state);
        } else {
            Timber.w("Невозможен переход %s -> %s", state.name(), newState.name());
        }
    }

    private void touchHandler(MotionEvent event) {

        switch (state) {
            case GAME:
                processTouch(event);
                break;
        }
    }

    private void processTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            FanoronaProgressDTO lastProgress = engine.touch(event.getX(), event.getY());

            // Если ход был сделан и он оказался последним в цепочке
            if (lastProgress != null && engine.isEndSteps()) {
                // Узнаём ход игрока. Говорим боту о ходе
                List<Progress> moves = engine.getMove();
                bot.processEnemyProgress(moves);

                // Переходим в ожидание хода
                updateState(RoomState.WAIT_PROGRESS);


                // Узнаём ход бота. Моделируем ход
                List<FanoronaProgressDTO> botProgress = bot.progress()
                        .stream()
                        .map(FanoronaProgressDTO::of)
                        .collect(Collectors.toList());
//
                for (FanoronaProgressDTO p : botProgress)
                    engine.progress(p.getFrom(), p.getTo(), p.getState(), p.getAttack());

                updateState(RoomState.GAME);
            }
        }
    }
}
