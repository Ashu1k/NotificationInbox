# Notification Inbox (Java + XML)

A beginner-friendly native Android implementation of the Firebase Notification Inbox take-home assignment.

## Features
- Receives Firebase Cloud Messaging data messages.
- Displays Android system notifications using channel ID `assignment` and channel name `Assignment`.
- Stores notification data in SQLite so it remains after app restart.
- Shows notification banner, title, description, timestamp and read/unread state.
- Opens details when an inbox item is tapped and marks it as read.
- Uses notification `id` as the primary key to prevent duplicate inbox entries.
- Shows a fallback image when the banner URL is empty or cannot load.

## Requirements
- Android Studio (recent stable version)
- JDK 17
- A Firebase project and an Android app registered with package name:
  `com.example.notificationinbox`

## Open the project
1. Extract `NotificationInboxJava.zip`.
2. Open Android Studio.
3. Select **Open** and choose the extracted `NotificationInboxJava` folder (the folder containing `settings.gradle`).
4. Wait for Gradle Sync to finish.
5. Create/connect a Firebase project as described below.
6. Run the app on a device with Google Play services.

## Firebase setup
1. Open https://console.firebase.google.com/ and create a Firebase project.
2. Add an Android app with package name `com.example.notificationinbox`.
3. Download `google-services.json`.
4. Copy the file into the project's **app** folder:
   `NotificationInboxJava/app/google-services.json`
   Do not put it in the project root or `src/main`.
5. In Firebase Console, open **Cloud Messaging** and send a test message to the app's FCM registration token. For this implementation, send a **data-only** message with high priority.

### Required data payload
Send these string key/value pairs in the FCM `data` object:

```json
{
  "message": {
    "token": "DEVICE_FCM_TOKEN",
    "android": { "priority": "HIGH" },
    "data": {
      "id": "notification_101",
      "title": "New Notification",
      "description": "This is a sample notification for the assignment.",
      "bannerImg": "https://example.com/banner.jpg",
      "timestamp": "1791873000000"
    }
  }
}
```

FCM notifications can be tested using a Firebase-authorized Node.js sender. The sender requires a valid device FCM token and Firebase service-account credentials

This is the HTTP v1 message body shape for a data-only message. Send it from a trusted server using Firebase Admin SDK or authenticated HTTP v1 access. Never put service-account credentials in the Android app.

**Why data-only?** Android/Firebase automatically displays a notification-payload message when the app is in the background, without calling `onMessageReceived()` at that moment. A data-only message lets this app save the item to SQLite and then build the system notification itself. The app handles foreground and background delivery through `MyFirebaseMessagingService`.

## Permission
Android 13 and newer require notification permission. The app asks for `POST_NOTIFICATIONS` when it first opens. Allow it to see system notifications.

## Build APK
1. In Android Studio, choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. When the build finishes, select **locate** in the notification.
3. The debug APK is normally at `app/build/outputs/apk/debug/app-debug.apk`.

## Main source files
- `MainActivity.java`: inbox screen, permission request and navigation.
- `MyFirebaseMessagingService.java`: receives FCM data and stores it.
- `NotificationDatabase.java`: SQLite insert/read/update operations.
- `NotificationUtils.java`: notification channel and system notification.
- `NotificationAdapter.java`: RecyclerView list.
- `NotificationDetailsActivity.java`: details screen and mark-as-read.
- `NotificationItem.java`: notification model.

## Notes / limitations
- Firebase Console's simple notification composer may send a notification payload rather than a data-only payload. Use an authenticated server/FCM HTTP v1 request as shown above for the expected background persistence behaviour.
- The sample does not include a backend server for sending messages.
- A duplicate ID is replaced in SQLite rather than inserted as a second row.
- The app needs internet access to load remote banner images.
- Never commit `google-services.json` if it contains project configuration you do not want public; never commit service-account keys or server credentials.
