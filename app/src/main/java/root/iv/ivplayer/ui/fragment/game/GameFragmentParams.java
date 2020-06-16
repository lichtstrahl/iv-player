package root.iv.ivplayer.ui.fragment.game;

import android.content.pm.ActivityInfo;

public abstract class GameFragmentParams {
    private static final int GAME_TIC_TAC = 1;
    private static final int GAME_FANORONA = 2;

    public static ScreenParam param(int gameType) {
        switch (gameType) {
            case GAME_TIC_TAC:
                return ticTacParam();

            case GAME_FANORONA:
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
