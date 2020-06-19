package root.iv.ivplayer.ui.fragment.game;

import android.content.pm.ActivityInfo;

import root.iv.ivplayer.game.GameType;

public abstract class GameFragmentParams {

    public static ScreenParam param(GameType gameType) {
        switch (gameType) {
            case TIC_TAC:
                return ticTacParam();

            case FANORONA:
                return fanoronaParam();
        }

        throw new IllegalArgumentException("Некорректный тип игры " + gameType);
    }

    public static ScreenParam ticTacParam() {
        return ScreenParam
                .builder()
                .fullScreen(true)
                .visibleActionBar(false)
                .orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .build();
    }

    public static ScreenParam fanoronaParam() {
        return ScreenParam
                .builder()
                .fullScreen(true)
                .visibleActionBar(false)
                .orientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                .build();
    }
}
