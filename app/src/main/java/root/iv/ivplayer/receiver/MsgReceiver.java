package root.iv.ivplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MsgReceiver extends BroadcastReceiver {
    private static final String TAG = "tag:ws";
    private static final String ACTION = "action:new-msg";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "MsgReceiver");
        Toast.makeText(context, "RECEIVE MSG", Toast.LENGTH_SHORT).show();
    }

}
