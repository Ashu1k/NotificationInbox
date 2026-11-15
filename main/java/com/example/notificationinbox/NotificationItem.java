package com.example.notificationinbox;

public class NotificationItem {
    private String id, title, description, bannerImg, timestamp;
    private boolean read;

    public NotificationItem(String id, String title, String description,
                            String bannerImg, String timestamp, boolean read) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.bannerImg = bannerImg;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getBannerImg() { return bannerImg; }
    public String getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
