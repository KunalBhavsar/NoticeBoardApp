package com.noticeboardapp.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.noticeboardapp.R;
import com.noticeboardapp.ui.NoticeBoardListActivity;

/**
 * Created by Pinky Walve on 28/4/16.
 */
public class NotificationHandler {
    private  static NotificationHandler notificationHandler;
    private Context context;
    NotificationManager notificationManager;

    private NotificationHandler(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.context = context;
    }

    public static void init(Context context) {
        notificationHandler = new NotificationHandler(context);
    }

    public static NotificationHandler getInstance() {
        return notificationHandler;
    }
    public void showNotification(String notificationTitle, String notificationMessage) {

        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        //icon appears in device notification bar and right hand corner of notification
        builder.setSmallIcon(R.drawable.ic_logo);

        builder.setAutoCancel(true);

        // This intent is fired when notification is clicked
        Intent intent = new Intent(context,NoticeBoardListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_logo));

        // Content title, which appears in large type at the top of the notification
        builder.setContentTitle(notificationTitle);

        // Content text, which appears in smaller text below the title
        builder.setContentText(notificationMessage);

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }

    public void clearNotifications() {
        notificationManager.cancelAll();
    }
}
