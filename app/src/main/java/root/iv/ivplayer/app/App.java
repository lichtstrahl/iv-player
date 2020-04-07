package root.iv.ivplayer.app;

import android.app.Application;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import root.iv.ivplayer.network.http.IvPlayerAPI;
import root.iv.ivplayer.network.ws.WSUtil;

public class App extends Application {
    private static IvPlayerAPI playerAPI;

    @Override
    public void onCreate() {
        super.onCreate();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WSUtil.baseSpringURL())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        playerAPI = retrofit.create(IvPlayerAPI.class);
    }

    public static IvPlayerAPI getPlayerAPI() {
        return playerAPI;
    }
}
