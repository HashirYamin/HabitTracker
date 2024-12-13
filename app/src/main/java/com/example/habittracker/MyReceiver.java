package com.example.habittracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import androidx.core.content.res.ResourcesCompat;

public class MyReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "My Channel";

    public static final int NOTIFICATION_ID = 100;
    MediaPlayer mp;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Create a MediaPlayer instance to play the alarm sound
        mp = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI);
        mp.setLooping(false);  // Ensure the sound plays only once
        mp.start();

        // Handler to stop the alarm after 10 seconds (10000 milliseconds)
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (mp != null && mp.isPlaying()) {
                mp.stop();
                mp.release();  // Release the MediaPlayer resource
            }
        }, 10000);  // Stop after 10 seconds

        mp.setOnCompletionListener(mediaPlayer -> mediaPlayer.release());

        // Create notification logic here
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Get drawable resource as Bitmap
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.dailytasks, null);
        Bitmap largeIcon = bitmapDrawable.getBitmap();

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.dailytasks)
                    .setContentTitle("Habit Tracker Reminder")
                    .setContentText("It's time to complete your habit!")
                    .setChannelId(CHANNEL_ID)
                    .build();

            nm.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_HIGH));
        } else {
            notification = new Notification.Builder(context)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.dailytasks)
                    .setContentTitle("Habit Tracker Reminder")
                    .setContentText("It's time to complete your habit!")
                    .build();
        }

        nm.notify(NOTIFICATION_ID, notification);
    }
}
