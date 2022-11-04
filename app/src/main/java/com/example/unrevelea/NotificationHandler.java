package com.example.unrevelea;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHandler {

    String[] notificationData;
    Resources resources;
    Context context;

    public NotificationHandler(Resources appResources, Context appContext) {

        resources = appResources;
        context = appContext;

        notificationData = resources.getStringArray(R.array.notification_data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    notificationData[0],
                    notificationData[1],
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            channel.setDescription(notificationData[2]);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void createPostNotification(String secretTitle) {
        String toNotify = String.format(notificationData[3], secretTitle);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationData[0])
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationData[1])
                .setContentText(toNotify)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(toNotify));
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, builder.build());
    }

    public void createNewUserNotification(long userId) {
        String toNotify = String.format(notificationData[4], userId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationData[0])
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationData[1])
                .setContentText(toNotify);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, builder.build());
    }

    public void createNUErrorNotification() {
        String toNotify = notificationData[5];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationData[0])
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationData[1])
                .setContentText(toNotify);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, builder.build());
    }
}
