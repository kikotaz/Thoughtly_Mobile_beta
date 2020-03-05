package com.thoughtly.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.thoughtly.MainActivity;

/*
 * @ClassName: NotificationReceiver
 * @Description: This class will be responsible for handling the notifications
 * that should be shown to user on daily basis
 * @Developer: Karim Saleh
 * @Version: 1.0
 * @Date: 01/07/2019
 */
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        long when = System.currentTimeMillis();

        NotificationManager notification = (NotificationManager)context.
                getSystemService(Context.NOTIFICATION_SERVICE);

        //The intent that will start the app from the notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //The path to the notification ringtone
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("Reminder from Snap Thought")
                .setContentText("Hey, it is time to check if your thoughts. Hopefully you can " +
                        "make the best out of them.").setAutoCancel(true).setWhen(when)
                .setContentIntent(pendingIntent).setVibrate(new long[]{1000, 1000});

        notification.notify(0, builder.build());
    }
}
