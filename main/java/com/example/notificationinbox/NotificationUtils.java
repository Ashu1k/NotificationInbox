
package com.example.notificationinbox;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationUtils {

    public static final String CHANNEL_ID = "assignment";
    public static final String CHANNEL_NAME = "Assignment";

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription("Firebase notification messages");

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showNotification(Context context, NotificationItem item) {

        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("notification_id", item.getId());

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                item.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(item.getTitle())
                        .setContentText(item.getDescription())
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(item.getDescription())
                        )
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        String imageUrl = item.getBannerImg();


        NotificationManager manager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (manager == null) return;

        int notificationId = item.getId().hashCode();

        manager.notify(notificationId, builder.build());

        // Download banner image in background
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {

            executor.execute(() -> {
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(imageUrl);

                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.setInstanceFollowRedirects(true);

                    try (InputStream input =
                                 connection.getInputStream()) {

                        Bitmap bitmap = BitmapFactory.decodeStream(input);

                        if (bitmap != null) {

                            NotificationCompat.BigPictureStyle pictureStyle =
                                    new NotificationCompat.BigPictureStyle()
                                            .bigPicture(bitmap)
                                            .bigLargeIcon((Bitmap) null);

                            NotificationCompat.Builder imageBuilder =
                                    new NotificationCompat.Builder(context, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.ic_notification)
                                            .setContentTitle(item.getTitle())
                                            .setContentText(item.getDescription())
                                            .setStyle(pictureStyle)
                                            .setAutoCancel(true)
                                            .setContentIntent(pendingIntent);

                            manager.notify(notificationId, imageBuilder.build());
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            });
        }
    }
}
