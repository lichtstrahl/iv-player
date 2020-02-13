package root.iv.ivplayer.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ChatServiceConnection implements ServiceConnection {
    private static final String TAG = "tag:ws";
    private boolean bind = false;
    private ChatService.ChatBinder chatBinder;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "connected");
        bind = true;
        chatBinder = (ChatService.ChatBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "disconnected");
        bind = false;
    }

    public void send(String msg) {
        chatBinder.send(msg);
    }

    public String read() {
        return chatBinder.read();
    }

    public int countMsgInQueue() {
        return chatBinder.countMsgInQueue();
    }

    public boolean isBind() {
        return bind;
    }

    public void unbound(){
        bind = false;
    }
}
