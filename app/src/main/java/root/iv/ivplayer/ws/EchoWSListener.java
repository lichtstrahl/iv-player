package root.iv.ivplayer.ws;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class EchoWSListener extends WebSocketListener {
    private static final String WS_TAG = "tag:ws";

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.i(WS_TAG, "Open");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.i(WS_TAG,"Recv: " + text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        Log.i(WS_TAG, "Recv: " + bytes.hex());
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {

        Log.i(WS_TAG, "Close: ");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.i(WS_TAG, "Error", t);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.i(WS_TAG, "Closed");
    }
}
