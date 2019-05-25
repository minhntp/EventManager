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
import com.nqm.event_manager.activities.ViewEventActivity;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.repositories.EventRepository;

import java.util.Calendar;

public class ReminderNotificationReceiver extends BroadcastReceiver implements IOnDataLoadComplete {

    public NotificationCompat.Builder notificationBuilder;
    public NotificationChannel notificationChannel;
    public String CHANNEL_NAME = " Quản lí sự kiện";
    public String CHANNEL_DESCRIPTION = "Nhắc nhở khi có sự kiện";
    public String NOTIFICATION_CHANNEL_ID = "event-manager-notification";

    private String eventId;
    private Event event;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        //GET DATA
        EventRepository.getInstance().setListener(this);
        eventId = intent.getStringExtra("eventId");
        event = EventRepository.getInstance().getAllEvents().get(eventId);
        if (event != null) {
            buildAndShowNotification();
        }
    }

    private void buildAndShowNotification() {
        String content = "Địa điểm: " + "\n" +
                "\t" + event.getDiaDiem() + "\n" +
                "Thời gian" + "\n" +
                "\t" + event.getNgayBatDau() + " - " + event.getGioBatDau() + "\n" +
                "\t" + event.getNgayKetThuc() + " - " + event.getGioKetThuc();

        //PREPARE INTENT FOR NOTIFICATION
        Intent viewEventIntent = new Intent(context, ViewEventActivity.class);
        viewEventIntent.putExtra("eventId", eventId);
        viewEventIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingViewEventIntent = PendingIntent.getActivity(context,
                Calendar.getInstance().hashCode(), viewEventIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //CREATE NOTIFICATION
        notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_event_noti)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setContentTitle(event.getTen())
                .setContentText(event.getDiaDiem())
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

//    public static boolean isAppRunningInBackground(Context context) {
//        boolean isRunning = true;
//
//        try {
//            ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
//            ActivityManager.RunningTaskInfo foregroundTaskInfo = activityManager.getRunningTasks(1).get(0);
//            String forgroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
//            PackageManager packageManager = context.getPackageManager();
//            PackageInfo foregroundAppPackageInfo = packageManager.getPackageInfo(forgroundTaskPackageName, 0);
//            String forgroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(packageManager).toString();
//            if (!"Quản lí sự kiện".equalsIgnoreCase(forgroundTaskAppName)) {
//                isRunning = false;
//            }
//        } catch (Exception e) {
//            isRunning = false;
//        }
//        return isRunning;
//    }

    @Override
    public void notifyOnLoadComplete() {
        event = EventRepository.getInstance().getAllEvents().get(eventId);
        if (event != null) {
            buildAndShowNotification();
        }
    }
}