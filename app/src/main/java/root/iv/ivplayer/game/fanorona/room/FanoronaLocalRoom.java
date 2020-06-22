package root.iv.ivplayer.game.fanorona.room;

import android.view.MotionEvent;

import androidx.annotation.Nullable;

import java.util.List;

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
import root.iv.ivplayer.game.view.GameView;

public class FanoronaLocalRoom extends Room {
    private FanoronaEngine engine;
    @Nullable
    private FanoronaRoomListener roomListener;
    private FanoronaBot bot;
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

    private void touchHandler(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            FanoronaProgressDTO lastProgress = engine.touch(event.getX(), event.getY());

            // Если ход был сделан и он оказался последним в цепочке
            if (lastProgress != null && engine.isEndSteps()) {
                // Узнаём ход игрока. Говорим боту о ходе
                List<Progress> moves = engine.getMove();
                bot.processEnemyProgress(moves);

                // Узнаём ход бота. Моделируем ход
                List<FanoronaProgressDTO> botProgress = engine.parse(bot.progress());
                for (FanoronaProgressDTO p : botProgress)
                    engine.progress(p.getFrom(), p.getTo(), p.getState());
            }
        }
    }
}
