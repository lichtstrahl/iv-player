package root.iv.ivplayer.network.ws;

import android.util.Log;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import root.iv.ivplayer.app.App;
import timber.log.Timber;

public class EchoWSListener extends WebSocketListener {
    private static final String WS_TAG = "tag:ws";

    private PublishSubject<String> publish = PublishSubject.create();

    public Disposable subscribe(Consumer<String> consumer) {
        return publish.subscribe(consumer);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Timber.tag(App.getTag()).i("WS: Open");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Timber.tag(App.getTag()).i("WS: Recv: %s", text);
//        publish.onNext(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Timber.tag(App.getTag()).i("WS: Recv: %s", bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Timber.tag(App.getTag()).i("WS: Close ");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Timber.tag(App.getTag()).e(t,"WS: Error");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Timber.tag(App.getTag()).i("WS: Closed");
    }
}
