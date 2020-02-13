package root.iv.ivplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import root.iv.ivplayer.activity.MainActivity;
import root.iv.ivplayer.notification.NotificationPublisher;
import root.iv.ivplayer.ws.EchoWSListener;
import root.iv.ivplayer.ws.WSHolder;
import root.iv.ivplayer.ws.WSUtil;

public class ChatService extends Service {
    // ACTION
    public static final String ACTION_END = "root.iv.ivplayer.service.END";
    public static final String ACTION_MSG = "root.iv.ivplayer.service.MSG";
    public static final String ACTION_START = "root.iv.ivplayer.service.START";
    public static final String ACTION_CLOSE = "root.iv.ivplayer.service.CLOSE";

    private static final String INTENT_FINISH_CODE = "intent:finish-code";
    private static final String INTENT_MSG = "intent:msg";
    private static final String INTENT_FROM_NOTIFICATION = "intent:from-notification";

    private static final String TAG = "tag:ws";
    private static final String NAME = "ChatService";
    private static final int STATUS_OK = 0;
    private static final int NOTIFICATION_ID = 1;


    private NotificationPublisher notificationPublisher;
    private WSHolder wsHolder;
    private ChatBinder chatBinder;
    private ArrayDeque<String> msgQueue;        // Очередь сообщений (на приём)

    public ChatService() {
        this.notificationPublisher = new NotificationPublisher(this);
        this.wsHolder = new WSHolder(WSUtil.templateURL(), new EchoWSListener());
        chatBinder = new ChatBinder();
        msgQueue = new ArrayDeque<>();
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ChatService.class);
        context.startService(intent);
    }

    public static void bind(Context context, ServiceConnection connection) {
        Intent intent = new Intent(context, ChatService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public static void unbind(Context context, ServiceConnection connection) {
        context.unbindService(connection);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_END);
        filter.addAction(ACTION_MSG);
        filter.addAction(ACTION_START);

        return filter;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service bind");
        return chatBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Service unbind");
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Service create");
        wsHolder.open(this::receiveMsg);
        Intent startIntent = new Intent(ACTION_START);
        sendBroadcast(startIntent);

        Notification notification = notificationPublisher.customForegroundChatService(
                this,
                mainActivityIntent(),
                closeIntent());
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service start");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroy");
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

    public static boolean fromNotification(Intent intent) {
        return intent.getBooleanExtra(INTENT_FROM_NOTIFICATION, false);
    }

    private void receiveMsg(String msg) {
        Intent intent = new Intent(ACTION_MSG);
        intent.putExtra(INTENT_MSG, msg);
        msgQueue.addLast(msg);
        sendBroadcast(intent);
    }

    private PendingIntent closeIntent() {
        Intent closeIntent = new Intent(ACTION_END);
        return PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent mainActivityIntent() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.putExtra(INTENT_FROM_NOTIFICATION, true);
        return PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public class ChatBinder extends Binder {
        public void send(String msg) {
            ChatService.this.wsHolder.send(msg);
        }

        public String read() {
            return ChatService.this.msgQueue.removeFirst();
        }

        public int countMsgInQueue() {
            return ChatService.this.msgQueue.size();
        }
    }
}
