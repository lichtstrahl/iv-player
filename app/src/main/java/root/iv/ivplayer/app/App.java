package root.iv.ivplayer.app;

import android.app.Application;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import root.iv.ivplayer.network.http.IvPlayerAPI;
import root.iv.ivplayer.network.ws.WSUtil;
import timber.log.Timber;

public class App extends Application {
    private static final String TAG = "tag:ws";
    private static IvPlayerAPI playerAPI;

    @Override
    public void onCreate() {
        super.onCreate();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Timber.plant(new Timber.DebugTree());

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(WSUtil.baseSpringURL(true))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        playerAPI = retrofit.create(IvPlayerAPI.class);
    }

    public static IvPlayerAPI getPlayerAPI() {
        return playerAPI;
    }

    public static void logE(Throwable t) {
        Timber.tag(TAG).e(t);
    }

    public static void logE(String msg) {
        Timber.tag(TAG).e(msg);
    }

    public static String getTag() {
        return TAG;
    }
}
