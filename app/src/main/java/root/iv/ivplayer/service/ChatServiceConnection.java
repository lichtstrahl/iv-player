package root.iv.ivplayer.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.Nullable;

import root.iv.ivplayer.app.App;
import root.iv.ivplayer.network.ws.pubnub.callback.PNHereNowCallback;
import root.iv.ivplayer.network.ws.pubnub.callback.PNPublishCallback;
import root.iv.ivplayer.network.ws.pubnub.callback.PNSubscribeCallback;
import timber.log.Timber;

public class ChatServiceConnection implements ServiceConnection {
    private static final String TAG = "tag:ws";
    private boolean bind = false;
    private ChatService.ChatBinder chatBinder;
    @Nullable
    private PNSubscribeCallback subscribeCallback;
    @Nullable
    private String[] channels;
    @Nullable
    private PNHereNowCallback hereNowCallback;
    @Nullable
    private String[] channelsForHereNow;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Timber.tag(App.getTag()).i("connected");
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

        if (hereNowCallback != null && channelsForHereNow != null) {
            chatBinder.hereNow(hereNowCallback, channelsForHereNow);
            channelsForHereNow = null;
            hereNowCallback = null;
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Timber.tag(App.getTag()).i("disconnected");
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

    public String getSelfUUID() {
        return chatBinder.getPNuuid();
    }

    public void hereNow(PNHereNowCallback callback, String ... channels) {
        if (chatBinder != null)
            chatBinder.hereNow(callback, channels);
        else {
            this.hereNowCallback = callback;
            this.channelsForHereNow = channels;
        }
    }

    public void stopPNConnection() {
        chatBinder.stopPNConnection();
    }

    public void unsubscribe(String ... channels) {
        chatBinder.unsubscribe(channels);
    }
}
