package com.nqm.event_manager.BroadcastReceiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.nqm.event_manager.activities.RootActivity;
import com.nqm.event_manager.activities.ViewEmployeeActivity;
import com.nqm.event_manager.activities.ViewEventActivity;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.repositories.EventRepository;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String eventId = intent.getStringExtra("event Id");
        Log.d("debug", "got alarm from event " + eventId);
        Event event = EventRepository.getInstance().getAllEvents().get(eventId);

        Intent viewEventIntent = new Intent(RootActivity.context, ViewEventActivity.class);
        viewEventIntent.putExtra("event Id", eventId);
        Log.d("debug", "set notification for event " + eventId);
        viewEventIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingViewEventIntent = PendingIntent.getActivity(RootActivity.context,
                0, viewEventIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String bigText = "Địa điểm: " + "\n" +
                "\t" + event.getDiaDiem() + "\n" +
                "Thời gian" + "\n" +
                "\t" + event.getNgayBatDau() + " - " + event.getGioBatDau() + "\n" +
                "\t" + event.getNgayKetThuc() + " - " + event.getGioKetThuc();

        RootActivity.notificationBuilder
                .setContentTitle(event.getTen())
                .setContentText(event.getDiaDiem())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setContentIntent(pendingViewEventIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            RootActivity.notificationBuilder
                    .setChannelId(RootActivity.NOTIFICATION_CHANNEL_ID);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(RootActivity.context);
        notificationManagerCompat.notify(RootActivity.NOTIFICATION_ID, RootActivity.notificationBuilder.build());
    }
}