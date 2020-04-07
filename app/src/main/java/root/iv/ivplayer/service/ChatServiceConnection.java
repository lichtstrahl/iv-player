package root.iv.ivplayer.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import root.iv.ivplayer.network.ws.pubnub.PNPublishCallback;
import root.iv.ivplayer.network.ws.pubnub.PNSubscribeCallback;

public class ChatServiceConnection implements ServiceConnection {
    private static final String TAG = "tag:ws";
    private boolean bind = false;
    private ChatService.ChatBinder chatBinder;
    @Nullable
    private PNSubscribeCallback subscribeCallback;
    @Nullable
    private String[] channels;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "connected");
        bind = true;
        chatBinder = (ChatService.ChatBinder) service;
        chatBinder.bind();
        if (subscribeCallback != null) {
            chatBinder.addListenerForPNConnect(subscribeCallback);
            subscribeCallback = null;
        }

        if (channels != null) {
            chatBinder.subscribeToChannels(channels);
            channels = null;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "disconnected");
        bind = false;
        chatBinder.unbind();
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
        chatBinder.unbind();
    }

    public void addListener(PNSubscribeCallback callback) {
        if (chatBinder != null)
            chatBinder.addListenerForPNConnect(callback);
        else
            subscribeCallback = callback;

    }

    public void subscribeToChannel(String ... channels) {
        if (chatBinder != null)
            chatBinder.subscribeToChannels(channels);
        else
            this.channels = channels;
    }

    public void publishMessageToChannel(String msg, String channel, @Nullable PNPublishCallback callback) {
        chatBinder.sendMessageToPNChannel(msg, channel, callback);
    }
}
