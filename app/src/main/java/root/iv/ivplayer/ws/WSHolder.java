package root.iv.ivplayer.ws;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okio.ByteString;

public class WSHolder {
    private static final int CODE_OK = 1000;

    private WebSocket webSocket;
    private OkHttpClient httpClient;
    private Request initRequest;
    private EchoWSListener listener;
    private Disposable disposable;
    private boolean opened;

    public WSHolder(String url, EchoWSListener listener) {
        this.initRequest = new Request.Builder().url(url).build();
        this.httpClient = new OkHttpClient();
        this.listener = listener;
        this.webSocket = null;
        this.opened = false;
    }

    public void open(Consumer<String> consumer) {
        webSocket = httpClient.newWebSocket(initRequest, listener);
        disposable = listener.subscribe(consumer);
        opened = true;
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
    }

    public boolean isOpened() {
        return opened;
    }
}
