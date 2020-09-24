package root.iv.ivplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import root.iv.ivplayer.service.command.CommandReporter;
import root.iv.ivplayer.ui.notification.NotificationPublisher;
import timber.log.Timber;

public class GameService extends Service {

    // ACTIONS - доступные действия, на которые может реагировать сервис
    private static final String ACTION_STOP = "root.iv.ivplayer.service.STOP";
    private static final String STARTING_REPORT = "root.iv.ivplayer.service.STARTING_REPORT";

    private static final int NOTIFICATION_ID = 1;

    private NotificationPublisher notificationPublisher;
    private GameBinder gameBinder;

    // COMMANDS - доступные команды для выполнения в фоновом режиме
    private CommandReporter reporter;


    public GameService() {
        this.notificationPublisher = NotificationPublisher.defaultPublisher();
        this.gameBinder = new GameBinder();
        this.reporter = CommandReporter.create(2);
    }

    public static void reporting(Context context) {
        Intent intent = new Intent(context, GameService.class);
        intent.setAction(STARTING_REPORT);
        context.startService(intent);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GameService.class);
        context.startService(intent);
    }

    public static void bind(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, GameService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void unbind(Context context, ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    // ----
    // BIND
    // ----

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Timber.i("bind");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Timber.i("rebind");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.i("unbind");
        return false; // Чтобы вызвать rebind при повторном подключении
    }

    // При создании сервиса необходимо показать нотификашку
    @Override
    public void onCreate() {
        Timber.i("create");

        Notification notification = notificationPublisher.customForegroundChatService(
                this,
                emptyIntent(),
                closeIntent()
        );

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Timber.i("Start command, action: %s", action);

        // Обработка различных экшенов
        if (action != null) {
            switch (action) {
                case ACTION_STOP:
                    stopSelf();
                    break;
                case STARTING_REPORT:
                    if (reporter.isStarted()) {
                        Timber.i("reporting already started");
                        break;
                    }

                    Executor executor = Executors.newSingleThreadExecutor();
                    executor.execute(reporter);
                    Timber.i("started reporting");
                    break;
                default:
                    Timber.i("Unknown action");
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Timber.i("destroy");
    }

    // INTENTS

    private PendingIntent closeIntent() {
        Intent intent = new Intent(this, GameService.class);
        intent.setAction(ACTION_STOP);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent emptyIntent() {
        return PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public class GameBinder extends Binder {

    }
}
