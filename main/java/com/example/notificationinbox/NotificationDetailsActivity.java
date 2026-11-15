
package com.example.notificationinbox;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_details);

        ImageView banner = findViewById(R.id.detailBanner);
        TextView title = findViewById(R.id.detailTitle);
        TextView description = findViewById(R.id.detailDescription);
        TextView timestamp = findViewById(R.id.detailTimestamp);

        String id = getIntent().getStringExtra("notification_id");

        if (id == null) {
            title.setText("Notification not found");
            return;
        }

        NotificationDatabase database = new NotificationDatabase(this);

        try {
            NotificationItem item = database.getNotification(id);

            if (item == null) {
                title.setText("Notification not found");
                description.setText("This notification may have been removed.");
                timestamp.setText("");
                banner.setImageResource(R.drawable.image_placeholder);
                return;
            }

            database.markAsRead(id);

            title.setText(item.getTitle());
            description.setText(item.getDescription());
            timestamp.setText(formatDate(item.getTimestamp()));

            String imageUrl = item.getBannerImg();

            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .centerCrop()
                        .into(banner);
            } else {
                banner.setImageResource(R.drawable.image_placeholder);
            }

        } finally {
            database.close();
        }
    }

    private String formatDate(String value) {
        try {
            return new SimpleDateFormat(
                    "dd MMM yyyy, hh:mm a",
                    Locale.getDefault()
            ).format(new Date(Long.parseLong(value)));
        } catch (Exception e) {
            return value == null ? "" : value;
        }
    }
}
