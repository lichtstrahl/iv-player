package root.iv.ivplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import root.iv.ivplayer.app.App;
import timber.log.Timber;

public class MsgReceiver extends BroadcastReceiver {
    private static final String TAG = "tag:ws";

    private Listener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.tag(App.getTag()).i("MsgReceiver");
        if (listener != null)
            listener.receive(intent);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    public interface Listener {
        void receive(Intent intent);
    }

}
