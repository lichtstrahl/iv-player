package root.iv.ivplayer.app;

import android.app.Application;

import androidx.room.Room;

import com.facebook.stetho.Stetho;

import root.iv.ivplayer.db.IVDatabase;
import timber.log.Timber;

public class App extends Application {
    private static final String TAG = "tag:ws";

    public static String getTag() {
        return TAG;
    }
    private static IVDatabase ivDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
        ivDatabase = Room.databaseBuilder(this, IVDatabase.class, IVDatabase.NAME)
                .build();

        Stetho.InitializerBuilder stethoBuilder = Stetho.newInitializerBuilder(this);
        stethoBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this));
        stethoBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(this));

        Stetho.initialize(stethoBuilder.build());
    }

    public static IVDatabase getIvDatabase() {
        return ivDatabase;
    }
}
