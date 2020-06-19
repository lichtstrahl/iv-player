package root.iv.ivplayer.app;

import android.app.Application;

import okhttp3.OkHttpClient;
import root.iv.ivplayer.network.http.RoomAPI;
import root.iv.ivplayer.network.http.UserAPI;

public class App extends Application {
    private static final String TAG = "tag:ws";
    private static final String API = "/api";
    private static final String API_USERS = "/api/users";
    private static final String API_ROOMS = "/api/rooms";
    private static UserAPI userAPI;
    private static RoomAPI roomAPI;
    private static OkHttpClient httpClient;

    public static String getTag() {
        return TAG;
    }
}
