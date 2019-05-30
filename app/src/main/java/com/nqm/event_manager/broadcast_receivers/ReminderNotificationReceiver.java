package com.nqm.event_manager.broadcast_receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.SplashActivity2;
import com.nqm.event_manager.utils.Constants;

import java.util.Calendar;

public class ReminderNotificationReceiver extends BroadcastReceiver {

    public NotificationCompat.Builder notificationBuilder;
    public NotificationChannel notificationChannel;
    public String CHANNEL_NAME = " Quản lí sự kiện";
    public String CHANNEL_DESCRIPTION = "Nhắc nhở khi có sự kiện";
    public String NOTIFICATION_CHANNEL_ID = "event-manager-notification";

    private String eventId, eventTitle, eventLocation, content;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        //GET DATA
        eventId = intent.getStringExtra(Constants.INTENT_EVENT_ID);
        eventTitle = intent.getStringExtra(Constants.INTENT_EVENT_TITLE);
        eventLocation = intent.getStringExtra(Constants.INTENT_EVENT_LOCATION);
        content = intent.getStringExtra(Constants.INTENT_EVENT_CONTENT);

        buildAndShowNotification();
    }

    private void buildAndShowNotification() {


        //PREPARE INTENT FOR NOTIFICATION
        Intent viewEventIntent = new Intent(context, SplashActivity2.class);
        viewEventIntent.putExtra(Constants.INTENT_EVENT_ID, eventId);
        viewEventIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingViewEventIntent = PendingIntent.getActivity(context,
                Calendar.getInstance().hashCode(), viewEventIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //CREATE NOTIFICATION
        notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_event_noti)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setContentTitle(eventTitle)
                .setContentText(eventLocation)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentIntent(pendingViewEventIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(eventId.hashCode(), notificationBuilder.build());
    }
}