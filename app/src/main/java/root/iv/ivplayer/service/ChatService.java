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

import androidx.annotation.Nullable;

import java.util.ArrayDeque;

import root.iv.ivplayer.BuildConfig;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.network.ws.EchoWSListener;
import root.iv.ivplayer.network.ws.WSHolder;
import root.iv.ivplayer.network.ws.WSUtil;
import root.iv.ivplayer.network.ws.pubnub.PubNubConnector;
import root.iv.ivplayer.network.ws.pubnub.callback.PNHereNowCallback;
import root.iv.ivplayer.network.ws.pubnub.callback.PNPublishCallback;
import root.iv.ivplayer.network.ws.pubnub.callback.PNSubscribeCallback;
import root.iv.ivplayer.notification.NotificationPublisher;
import root.iv.ivplayer.ui.fragment.ChatFragment;
import timber.log.Timber;

public class ChatService extends Service {
    // ACTION
    public static final String ACTION_END = "root.iv.ivplayer.service.END";
    public static final String ACTION_MSG = "root.iv.ivplayer.service.MSG";
    public static final String ACTION_START = "root.iv.ivplayer.service.START";
    public static final String ACTION_CLOSE = "root.iv.ivplayer.service.CLOSE";

    private static final String INTENT_FINISH_CODE = "intent:finish-code";
    private static final String INTENT_MSG = "intent:msg";
    private static final String INTENT_FROM_NOTIFICATION = "intent:from-notification";
    private static final String INTENT_CREATE_LOGIN = "intent:login";

    private static final String TAG = "tag:ws";
    private static final String NAME = "ChatService";
    private static final int STATUS_OK = 0;
    private static final int NOTIFICATION_ID = 1;


    private NotificationPublisher notificationPublisher;
    private WSHolder wsHolder;
    private ChatBinder chatBinder;
    private ArrayDeque<String> msgQueue;        // Очередь сообщений (на приём)
    private int countClient;
    private PubNubConnector pnConnector;
    private String loginDevice;

    public ChatService() {
        this.notificationPublisher = new NotificationPublisher(this);
        this.wsHolder = new WSHolder(WSUtil.springWSURL(), new EchoWSListener());
        chatBinder = new ChatBinder();
        msgQueue = new ArrayDeque<>();
        countClient = 0;
    }

    public static void start(Context context, String login) {
        Intent intent = new Intent(context, ChatService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(INTENT_CREATE_LOGIN, login);
        context.startService(intent);
    }

    public static void bind(Context context, ServiceConnection connection) {
        String contextName = context.getClass().getName();
        Timber.i("bind %s", contextName);
        Intent intent = new Intent(context, ChatService.class);
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public static void unbind(Context context, ServiceConnection connection) {
        String contextName = context.getClass().getName();
        Timber.i("unbind %s", contextName);
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
        Timber.tag(App.getTag()).i("Service bind");
        return chatBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Timber.tag(App.getTag()).i("Service unbind");
        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Timber.tag(App.getTag()).i("Service rebind");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.tag(App.getTag()).i("Service create");
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
        String action = intent != null ? intent.getAction() : null;
        Timber.tag(App.getTag()).i("Service start. Action: %s", action);

        // Если пришла команда закончить работу. То отправляем сообщение Activity, если оно ещё живое
        // И убираем нотификацию
        if (action != null && action.equalsIgnoreCase(ACTION_END)) {
            sendBroadcast(new Intent(ACTION_END));
            stopForeground(true);
            stopSelf();
        }

        // Если это команда на запуск сервиса:
        if (action != null && action.equalsIgnoreCase(ACTION_START)) {
            loginDevice = intent.getStringExtra(INTENT_CREATE_LOGIN);
            // Создание подключения к PN
            pnConnector = PubNubConnector.create(BuildConfig.PUB_KEY, BuildConfig.SUB_KEY, loginDevice);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.tag(App.getTag()).i("Service destroy");
        wsHolder.close();
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, ChatService.class);
        intent.setAction(ACTION_END);
        context.startService(intent);
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
        Intent closeIntent = new Intent(this, ChatService.class);
        closeIntent.setAction(ACTION_END);
        return PendingIntent.getService(this, 0, closeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent mainActivityIntent() {
        Intent mainActivityIntent = new Intent(this, ChatFragment.class);
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

        public void bind() {
            countClient++;
        }

        public void unbind() {
            countClient--;
        }

        public void addListenerForPNConnect(PNSubscribeCallback callback) {
            pnConnector.addListener(callback);
        }

        public void subscribeToChannels(String ... channels) {
            pnConnector.subscribe(channels);
        }

        public void sendMessageToPNChannel(String msg, String channel, @Nullable PNPublishCallback publishCallback) {
            if (publishCallback != null)
                pnConnector.sendMessage(msg, channel, publishCallback);
            else
                pnConnector.sendMessage(msg, channel);
        }

        public String getPNuuid() {
            return pnConnector.getUUID();
        }

        public void hereNow(PNHereNowCallback callback, String ... channels) {
            pnConnector.hereNow(callback, channels);
        }

        public String getDeviceLogin() {
            return loginDevice;
        }

        public void stopPNConnection() {
            pnConnector.finish();
        }

        public void unsubscribe(String ... channels) {
            pnConnector.unsubscribe(channels);
        }
    }
}
