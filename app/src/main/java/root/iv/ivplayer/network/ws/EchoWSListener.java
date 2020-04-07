package root.iv.ivplayer.network.ws;

import android.util.Log;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class EchoWSListener extends WebSocketListener {
    private static final String WS_TAG = "tag:ws";

    private PublishSubject<String> publish = PublishSubject.create();

    public Disposable subscribe(Consumer<String> consumer) {
        return publish.subscribe(consumer);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.i(WS_TAG, "WS: Open");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.i(WS_TAG,"WS: Recv: " + text);
        publish.onNext(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Log.i(WS_TAG, "WS: Recv: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {

        Log.i(WS_TAG, "WS: Close ");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(WS_TAG, "WS: Error", t);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.i(WS_TAG, "WS: Closed");
    }
}
