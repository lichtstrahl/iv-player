package root.iv.ivplayer.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import root.iv.ivplayer.activity.MainActivity;
import root.iv.ivplayer.notification.NotificationPublisher;
import root.iv.ivplayer.ws.EchoWSListener;
import root.iv.ivplayer.ws.WSHolder;
import root.iv.ivplayer.ws.WSUtil;

public class ChatService extends IntentService {
    // ACTION
    public static final String ACTION_END = "root.iv.ivplayer.service.END";
    public static final String ACTION_MSG = "root.iv.ivplayer.service.MSG";
    public static final String ACTION_CLOSE = "root.iv.ivplayer.service.CLOSE";

    private static final String INTENT_FINISH_CODE = "intent:finish-code";
    private static final String INTENT_MSG = "intent:msg";

    private static final String TAG = "tag:ws";
    private static final String NAME = "ChatService";
    private static final int STATUS_OK = 0;
    private static final int NOTIFICATION_ID = 1;

    // INTENT
    private static final Intent endIntent = new Intent(ACTION_END)
            .putExtra(INTENT_FINISH_CODE, STATUS_OK);


    private NotificationPublisher notificationPublisher;
    private WSHolder wsHolder;

    public ChatService() {
        super(NAME);
        this.notificationPublisher = new NotificationPublisher(this);
        this.wsHolder = new WSHolder(WSUtil.templateURL(), new EchoWSListener());
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ChatService.class);
        context.startService(intent);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_END);
        filter.addAction(ACTION_MSG);

        return filter;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        wsHolder.open(this::receiveMsg);

        long t0 = System.currentTimeMillis();
        long t1 = System.currentTimeMillis();
        while (wsHolder.isOpened()) {
            if (t1 - t0 > 5000) {
                Log.i(TAG, "Жду сообщений");
                t0 = t1;
            }
            t1 = System.currentTimeMillis();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Create");
        Notification notification = notificationPublisher.customForegroundChatService(this, MainActivity.class, closeIntent());
        startForeground(NOTIFICATION_ID, notification);
    }

    // Отправка команд
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i(TAG, "Start service");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wsHolder.close();
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, ChatService.class);
        context.stopService(intent);
    }

    public static String getMessage(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null)
            return bundle.getString(INTENT_MSG);
        return "";
    }

    private void receiveMsg(String msg) {
        Intent intent = new Intent(ACTION_MSG);
        intent.putExtra(INTENT_MSG, msg);
        sendBroadcast(intent);
    }

    private PendingIntent closeIntent() {
        Intent closeIntent = new Intent(ACTION_END);
        return PendingIntent.getBroadcast(this, 123, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
