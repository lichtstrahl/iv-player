package root.iv.ivplayer.network.ws;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okio.ByteString;
import root.iv.ivplayer.app.App;
import timber.log.Timber;

public class WSHolder {
    private static final int CODE_OK = 1000;

    private WebSocket webSocket;
    private Request initRequest;
    private EchoWSListener listener;
    private Disposable disposable;
    private boolean opened;

    public WSHolder(String url, EchoWSListener listener) {
        this.initRequest = new Request.Builder().url(url).build();
        this.listener = listener;
        this.webSocket = null;
        this.opened = false;
    }

    public static WSHolder fromURL(String url) {
        return new WSHolder(url, new EchoWSListener());
    }

    public void open(Consumer<String> consumer) {
        webSocket = App.httpClient().newWebSocket(initRequest, listener);
        disposable = listener.subscribe(consumer);
        opened = true;
        Timber.i("WS open");
    }

    public void send(String msg) {
        webSocket.send(msg);
    }

    public void send(ByteString msg) {
        webSocket.send(msg);
    }

    public void close() {
        webSocket.close(CODE_OK, null);
        disposable.dispose();
        opened = false;
        Timber.i("WS close");
    }

    public boolean isOpened() {
        return opened;
    }
}
