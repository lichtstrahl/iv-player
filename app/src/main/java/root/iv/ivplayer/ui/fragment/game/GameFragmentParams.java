package root.iv.ivplayer.ui.fragment.game;

import android.content.pm.ActivityInfo;

public abstract class GameFragmentParams {
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
                .orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .build();
    }
}
