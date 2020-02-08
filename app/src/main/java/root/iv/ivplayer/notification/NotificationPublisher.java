package root.iv.ivplayer.notification;

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
    private Context context;

    public NotificationPublisher(Context context) {
        this.context = context;
    }

    public void notification(String text) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent =PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notification = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);

        notification
                .setContentTitle("Чат")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(text)
                .setContentIntent(pIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "channel1", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notification.build());
    }
}
