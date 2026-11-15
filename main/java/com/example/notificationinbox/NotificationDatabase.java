package com.example.notificationinbox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class NotificationDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "notifications.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "notification_inbox";

    public NotificationDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                "id TEXT PRIMARY KEY, title TEXT, description TEXT, " +
                "bannerImg TEXT, timestamp TEXT, isRead INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Add database migration code here if the table structure changes.
    }

    // INSERT OR REPLACE prevents duplicate notification IDs.
    public void saveNotification(NotificationItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", item.getId());
        values.put("title", item.getTitle());
        values.put("description", item.getDescription());
        values.put("bannerImg", item.getBannerImg());
        values.put("timestamp", item.getTimestamp());
        values.put("isRead", item.isRead() ? 1 : 0);
        db.insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public ArrayList<NotificationItem> getAllNotifications() {
        ArrayList<NotificationItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE + " ORDER BY CAST(timestamp AS INTEGER) DESC", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new NotificationItem(
                        cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getString(cursor.getColumnIndexOrThrow("bannerImg")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) == 1
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public NotificationItem getNotification(String notificationId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, "id=?", new String[]{notificationId},
                null, null, null);
        NotificationItem item = null;
        if (cursor.moveToFirst()) {
            item = new NotificationItem(
                    cursor.getString(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("bannerImg")),
                    cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) == 1
            );
        }
        cursor.close();
        db.close();
        return item;
    }

    public void markAsRead(String notificationId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isRead", 1);
        db.update(TABLE, values, "id=?", new String[]{notificationId});
        db.close();
    }
}
