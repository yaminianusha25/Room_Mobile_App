package com.example.dips.smartscheduler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jorge on 4/27/2016.
 */
public class AlertReceiver extends Service {

    private Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        DatabaseHelper dbHelper=new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("Data", 0x0000);
        String phoneNumber = prefs.getString("phoneNumber", "1");

        String eventName = dbHelper.getPendingJobs(phoneNumber);
        if(eventName == null){
            Log.i("AlertReceiver","No pending task for " + phoneNumber);
            this.stopSelf();
        }else{
            Log.i("AlertReceiver","pending task for " + phoneNumber + " " + eventName);
            createNotification(eventName);
        }
        return START_STICKY;
    }

    private void createNotification(String taskName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());

        builder.setSmallIcon(R.drawable.iconnotification);
        builder.setContentTitle("You have a Task due today!");
        builder.setContentText(taskName);
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setVibrate(new long[]{0, 100, 0, 0});

        builder.setAutoCancel(true);
        PendingIntent notificIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class), 0);
        builder.setContentIntent(notificIntent);

        builder.mNotification.flags|= Notification.FLAG_ONLY_ALERT_ONCE;    // Dont vibrate or make notification sound

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(0, builder.build());

    }
}
