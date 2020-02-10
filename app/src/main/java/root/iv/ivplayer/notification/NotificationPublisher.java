package root.iv.ivplayer.notification;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import root.iv.ivplayer.R;
import root.iv.ivplayer.activity.MainActivity;

public class NotificationPublisher {
    private static final String CHANNEL_ID = "channel:1";
    private static final String CHANNEL_SERVICE = "channel:chat-service";
    private static final String CHANNEL_NAME = "name";
    private Context context;

    public NotificationPublisher(Context context) {
        this.context = context;
    }

    public void notification(String text) {
        PendingIntent pIntent = activityIntent(MainActivity.class);

        Notification.Builder notification = builder(CHANNEL_ID);

        notification
                .setContentTitle("Чат")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(text)
                .setContentIntent(pIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(notificationManager, CHANNEL_ID, CHANNEL_NAME);

        notificationManager.notify(0, notification.build());
    }

    public Notification foregroundChatService() {
        PendingIntent mainActivityIntent = activityIntent(MainActivity.class);
        Notification.Builder notificationBuilder = builder(CHANNEL_SERVICE)
                .setContentTitle("Чат")
                .setSmallIcon(R.drawable.ic_chat)
                .setContentText("Сервис приёма сообщений")
                .setContentIntent(mainActivityIntent);

        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(manager, CHANNEL_SERVICE, CHANNEL_NAME);

        return notificationBuilder.build();
    }

    private void createChannel(NotificationManager notificationManager, String channelId, String channelName) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private PendingIntent activityIntent(Class cls) {
        Intent intent = new Intent(context, cls);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification.Builder builder(String channelID) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, channelID)
                : new Notification.Builder(context);
    }
}
