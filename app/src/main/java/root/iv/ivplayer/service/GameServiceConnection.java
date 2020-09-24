package root.iv.ivplayer.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import timber.log.Timber;

public class GameServiceConnection implements ServiceConnection {

    private GameService.GameBinder gameBinder;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Timber.i("connected");

        gameBinder = (GameService.GameBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Timber.i("disconnected");
    }
}
