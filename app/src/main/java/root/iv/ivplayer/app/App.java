package root.iv.ivplayer.app;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import root.iv.ivplayer.BuildConfig;
import root.iv.ivplayer.R;
import root.iv.ivplayer.network.http.RoomAPI;
import root.iv.ivplayer.network.http.UserAPI;
import root.iv.ivplayer.network.ws.WSUtil;
import timber.log.Timber;

public class App extends Application {
    private static final String TAG = "tag:ws";
    private static final String API = "/api";
    private static final String API_USERS = "/api/users";
    private static final String API_ROOMS = "/api/rooms";
    private static UserAPI userAPI;
    private static RoomAPI roomAPI;
    private static OkHttpClient httpClient;
    private static FirebaseDatabase fbDatabase;

    public static OkHttpClient httpClient() {
        return httpClient;
    }

    public static UserAPI getUserAPI() {
        return userAPI;
    }

    public static RoomAPI getRoomAPI() {
        return roomAPI;
    }

    public static FirebaseDatabase getFbDatabase() {
        return fbDatabase;
    }

    public static DatabaseReference getRooms() {
        return fbDatabase.getReference("rooms");
    }

    public static DatabaseReference getRoom(String roomName) {
        return getRooms().child(roomName);
    }

    public static DatabaseReference getRoomStatus(String roomName) {
        return getRooms().child(roomName).child("state");
    }

    public static DatabaseReference getPlayerEmail(String roomName, String pathEmail) {
        return getRoom(roomName).child(pathEmail);
    }

    public static DatabaseReference getProgressInRoom(String roomName, String progress) {
        return getRoom(roomName).child(progress);
    }

    @SneakyThrows
    @Override
    public void onCreate() {
        super.onCreate();
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        InputStream file = getResources().openRawResource(R.raw.server);
        Certificate certificate = certificateFactory.generateCertificate(file);
        file.close();

        // Создание KeyStore
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", certificate);

        // Создание TrustManager для обработки нашего сертификата
        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(algorithm);
        trustManagerFactory.init(keyStore);

        // Создаём SSL контект, который будет использовать наш TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);


        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Timber.plant(new Timber.DebugTree());

        httpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier((hostname, session) -> {
                    return hostname.equals(BuildConfig.URL_SPRING);
                })
                .build();

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .client(httpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        userAPI = retrofitBuilder
                .baseUrl(String.format("%s%s/", WSUtil.baseSpringURL(true), API_USERS))
                .build()
                .create(UserAPI.class);

        roomAPI = retrofitBuilder
                .baseUrl(String.format("%s%s/", WSUtil.baseSpringURL(true), API_ROOMS))
                .build()
                .create(RoomAPI.class);

        fbDatabase = FirebaseDatabase.getInstance();
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
