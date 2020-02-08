package root.iv.ivplayer.ws;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WSHolder {
    private static final int CODE_OK = 1000;

    private WebSocket webSocket;
    private OkHttpClient httpClient;
    private Request initRequest;
    private WebSocketListener listener;

    public WSHolder(String url, WebSocketListener listener) {
        this.initRequest = new Request.Builder().url(url).build();
        this.httpClient = new OkHttpClient();
        this.listener = listener;
        this.webSocket = null;
    }

    public void open() {
        webSocket = httpClient.newWebSocket(initRequest, listener);
    }

    public void send(String msg) {
        webSocket.send(msg);
    }

    public void send(ByteString msg) {
        webSocket.send(msg);
    }

    public void close() {
        webSocket.close(CODE_OK, null);
    }
}
