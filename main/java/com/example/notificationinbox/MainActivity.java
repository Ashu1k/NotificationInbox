package com.example.notificationinbox;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NotificationAdapter.OnItemClickListener {
    private NotificationDatabase database;
    private NotificationAdapter adapter;
    private TextView emptyText, countText;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM_TOKEN", token);
                });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("FCM_TOKEN", "Token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM_TOKEN", token);
                });

        database = new NotificationDatabase(this);
        emptyText = findViewById(R.id.emptyText);
        countText = findViewById(R.id.countText);
        RecyclerView recyclerView = findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
 
        NotificationUtils.createChannel(this);
        askNotificationPermission();
        loadNotifications();
        handleNotificationTap(getIntent());
    }
    public static final String ACTION_NOTIFICATION_UPDATED =
            "com.example.notificationinbox.NOTIFICATION_UPDATED";

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void loadNotifications() {
        ArrayList<NotificationItem> items = database.getAllNotifications();

        for (NotificationItem item : items) {
            android.util.Log.d("INBOX_IMAGE",
                    "ID: " + item.getId() +
                            " | Banner: " + item.getBannerImg());
        }
        adapter.updateList(items);
        emptyText.setVisibility(items.isEmpty() ? TextView.VISIBLE : TextView.GONE);

        int unread = 0;
        for (NotificationItem item : items) if (!item.isRead()) unread++;
        countText.setText(unread + " unread");
    }

    @Override
    public void onItemClick(NotificationItem item) {
        Intent intent = new Intent(this, NotificationDetailsActivity.class);
        intent.putExtra("notification_id", item.getId());
        startActivity(intent);
    }
/*
    private void handleNotificationTap(Intent intent) {
        if (intent != null && intent.hasExtra("notification_id")) {
            String id = intent.getStringExtra("notification_id");
            if (id != null) {
                Intent detailIntent = new Intent(this, NotificationDetailsActivity.class);
                detailIntent.putExtra("notification_id", id);
                startActivity(detailIntent);
                intent.removeExtra("notification_id");
            }
        }
    }*/

//    private void handleNotificationTap(Intent intent) {
//        if (intent != null && intent.hasExtra("notification_id")) {
//            intent.removeExtra("notification_id");
//        }
//
//        loadNotifications();
//    }
private void handleNotificationTap(Intent intent) {



    if (intent != null && intent.getExtras() != null) {
        Bundle extras = intent.getExtras();

        for (String key : extras.keySet()) {
            Log.d("FCM_TAP_DATA",
                    key + " = " + extras.get(key));
        }
    }


    if (intent == null || intent.getExtras() == null) {
        loadNotifications();
        return;
    }

    Bundle extras = intent.getExtras();

    String id = extras.getString("id");

    if (id == null || id.isEmpty()) {
        id = extras.getString("google.message_id");
    }

    if (id == null || id.isEmpty()) {
        id = extras.getString("notification_id");
    }

    if (id == null || id.isEmpty()) {
        loadNotifications();
        return;
    }

    String title = extras.getString("title");
    String description = extras.getString("description");
    String bannerImg = extras.getString("bannerImg");
    String timestamp = extras.getString("timestamp");

    if (title == null || title.isEmpty()) {
        title = extras.getString("gcm.n.title", "New Notification");
    }

    if (description == null) {
        description = extras.getString("gcm.n.body", "");
    }

    if (bannerImg == null) {
        bannerImg = extras.getString("gcm.n.image", "");
    }

    if (timestamp == null || timestamp.isEmpty()) {
        timestamp = String.valueOf(System.currentTimeMillis());
    }

    // Save only if this notification is not already in SQLite
    if (database.getNotification(id) == null) {
        NotificationItem item = new NotificationItem(
                id,
                title,
                description,
                bannerImg,
                timestamp,
                false
        );

        database.saveNotification(item);
    }

    loadNotifications();


    intent.removeExtra("id");
    intent.removeExtra("notification_id");
}
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationTap(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null && adapter != null) loadNotifications();
    }

    @Override
    protected void onDestroy() {
        if (database != null) database.close();
        super.onDestroy();
    }

    private final BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadNotifications();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        ContextCompat.registerReceiver(
                this,
                notificationReceiver,
                new IntentFilter(ACTION_NOTIFICATION_UPDATED),
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        loadNotifications();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(notificationReceiver);
        super.onStop();
    }
}
