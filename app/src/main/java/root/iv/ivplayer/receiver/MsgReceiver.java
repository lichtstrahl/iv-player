package root.iv.ivplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class MsgReceiver extends BroadcastReceiver {
    private static final String TAG = "tag:ws";
    private static final String ACTION = "action:new-msg";

    private Listener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "MsgReceiver");
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
