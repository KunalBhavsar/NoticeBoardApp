package com.jyotitech.noticeboardapp.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.jyotitech.noticeboardapp.R;
import com.jyotitech.noticeboardapp.ui.NoticeBoardListActivity;
import com.jyotitech.noticeboardapp.ui.NoticeListActivity;

/**
 * Created by Shraddha on 28/4/16.
 */
public class NotificationHandler {

    Activity context;

    public NotificationHandler(Activity context) {
        this.context = context;
    }

    public void showNotification(String notificationTitle, String notificationMessage) {

        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        //icon appears in device notification bar and right hand corner of notification
        builder.setSmallIcon(R.drawable.logo);

        builder.setAutoCancel(true);

        // This intent is fired when notification is clicked
        Intent intent = new Intent(context,NoticeBoardListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo));

        // Content title, which appears in large type at the top of the notification
        builder.setContentTitle(notificationTitle);

        // Content text, which appears in smaller text below the title
        builder.setContentText(notificationMessage);

        // The subtext, which appears under the text on newer devices.
        // This will show-up in the devices with Android 4.2 and above only
        //builder.setSubText("Tap to view documentation about notifications.");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }
}
