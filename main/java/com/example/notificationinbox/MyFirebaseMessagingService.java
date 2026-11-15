
package com.example.notificationinbox;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();

        String id = data.get("id");

        if (id == null || id.trim().isEmpty()) {
            id = remoteMessage.getMessageId();
        }

        if (id == null || id.trim().isEmpty()) {
            id = String.valueOf(System.currentTimeMillis());
        }

        String title = data.get("title");
        String description = data.get("description");

        if (remoteMessage.getNotification() != null) {
            if (title == null || title.isEmpty()) {
                title = remoteMessage.getNotification().getTitle();
            }

            if (description == null || description.isEmpty()) {
                description = remoteMessage.getNotification().getBody();
            }
        }

        if (title == null || title.trim().isEmpty()) {
            title = "New Notification";
        }

        if (description == null) {
            description = "";
        }

        String bannerImg = data.get("bannerImg");

        if ((bannerImg == null || bannerImg.isEmpty())
                && remoteMessage.getNotification() != null
                && remoteMessage.getNotification().getImageUrl() != null) {

            bannerImg = remoteMessage.getNotification()
                    .getImageUrl().toString();
        }

        if (bannerImg == null) {
            bannerImg = "";
        }


        String timestamp = String.valueOf(System.currentTimeMillis());

        NotificationItem item = new NotificationItem(
                id,
                title,
                description,
                bannerImg,
                timestamp,
                false
        );

        NotificationDatabase database = new NotificationDatabase(this);

        try {
            database.saveNotification(item);
        } finally {
            database.close();
        }

        Intent intent = new Intent(MainActivity.ACTION_NOTIFICATION_UPDATED);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);

        NotificationUtils.createChannel(this);
        NotificationUtils.showNotification(this, item);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", token);
    }
}
