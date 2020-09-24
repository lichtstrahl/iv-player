package root.iv.ivplayer.ui.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.util.Optional;

import root.iv.ivplayer.R;

public class NotificationPublisher {
    private static final String CHANNEL_ID = "channel:1";
    private static final String CHANNEL_SERVICE = "channel:fanorona-service";
    private static final String CHANNEL_NAME = "channel-fanorona";

    public static NotificationPublisher defaultPublisher() {
        return new NotificationPublisher();
    }

    public void notification(Context context, String title, String text) {
        notification(context, title, text, CHANNEL_ID, CHANNEL_NAME);
    }

    public void notification(Context context, String title, String text, String channelID, String channelName) {
        Notification.Builder builder = builder(context, channelID);

        builder
                .setContentTitle(title)
                .setSmallIcon(R.drawable.fanorona_icon)
                .setContentText(text)
                .setContentIntent(emptyIntent(context));

        notificationManager(context, channelID, channelName)
                .notify(9, builder.build());
    }

    public Notification customForegroundChatService
            (Context context,
             @NonNull PendingIntent clickIntent,
             @NonNull PendingIntent closeIntent) {
        RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_chat);
        notificationLayout.setOnClickPendingIntent(R.id.buttonClose, closeIntent);


        NotificationCompat.Builder notificationBuilder = builderCompat(context, CHANNEL_SERVICE)
                .setSmallIcon(R.drawable.ic_chat)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setOngoing(true)
                .setContentIntent(clickIntent)
                .setCustomContentView(notificationLayout);

        createChannel(context, CHANNEL_SERVICE, CHANNEL_NAME);

        return notificationBuilder.build();
    }

    private void createChannel(Context context, String channelId, String channelName) {
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
    }

    private NotificationManager notificationManager(Context context, String channelID, String channelName) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            Optional.ofNullable(manager)
                    .ifPresent(m -> m.createNotificationChannel(channel));
        }

        return manager;
    }

    private PendingIntent emptyIntent(Context context) {
        Intent intent = new Intent();
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Notification.Builder builder(Context context, String channelID) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, channelID)
                : new Notification.Builder(context);
    }

    private NotificationCompat.Builder builderCompat(Context context, String channelID) {
        return new NotificationCompat.Builder(context, channelID);
    }
}
