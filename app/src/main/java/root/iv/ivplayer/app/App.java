package root.iv.ivplayer.app;

import android.app.Application;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import root.iv.ivplayer.BuildConfig;
import root.iv.ivplayer.R;
import root.iv.ivplayer.network.http.IvPlayerAPI;
import root.iv.ivplayer.network.ws.WSUtil;
import timber.log.Timber;

public class App extends Application {
    private static final String TAG = "tag:ws";
    private static IvPlayerAPI playerAPI;

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

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier((hostname, session) -> {
                    return hostname.equals(BuildConfig.URL_SPRING);
                })
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
