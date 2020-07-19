package root.iv.ivplayer.app;

import android.app.Application;

import timber.log.Timber;

public class App extends Application {
    private static final String TAG = "tag:ws";

    public static String getTag() {
        return TAG;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
