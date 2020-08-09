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
import root.iv.ivplayer.game.fanorona.textures.FanoronaTextures;
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
        engine = new FanoronaEngine(textures, this::processTouch);
        bot = FanoronaBot.defaultSize(role == FanoronaRole.BLACK ? Role.WHITE : Role.BLACK);
        engine.setCurrentRole(role);
        state = role == FanoronaRole.BLACK ? RoomState.WAIT_PROGRESS : RoomState.GAME;
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

    private void processTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            switch (state) {
                case GAME:
                    processProgress(event.getX(), event.getY());
                    break;

                case SELECT_ATTACK_TYPE:
                    processSelectAttackType(event.getX(), event.getY());
                    break;

                default:
                    Timber.i("В текущем состоянии %s касания не обрабатываются", state.name());
            }
        }
    }

    // Обработка хода (GAME)
    private void processProgress(float x, float y) {
        FanoronaProgressDTO lastProgress = engine.touch(x, y);


        // Если ход был сделан и он оказался последним в цепочке
        if (lastProgress != null && engine.endProgressChain()) {
            messagesBot();
        } else if (engine.possibleDoubleAttack(x, y)) { // Ход не обработан, т.к. возможны два направления атаки
            updateState(RoomState.SELECT_ATTACK_TYPE);
            engine.markSlotsForAttack(x, y);
        }
    }

    // Обработка выбора направления атаки: (SELECT_ATTACK_TYPE)
    private void processSelectAttackType(float x, float y) {
        FanoronaProgressDTO progress = engine.selectAttackType(x, y);

        // Если ход был сделан, то переходим в состояние GAME
        if (progress != null) {
            updateState(RoomState.GAME);

            // Если этот ход был последним в цепочке
            if (engine.endProgressChain()) {
                messagesBot();
            }
        }
    }

    // Взаимодействие с ботом: отправка своих ходов, принятие его ходов
    private void messagesBot() {
        // Узнаём ход игрока. Говорим боту о ходе
        bot.processEnemyProgress(engine.getMove());
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
