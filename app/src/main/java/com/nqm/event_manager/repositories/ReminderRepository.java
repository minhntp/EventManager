package com.nqm.event_manager.repositories;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.BroadcastReceiver.MyBroadcastReceiver;
import com.nqm.event_manager.activities.RootActivity;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ReminderRepository {
    static ReminderRepository instance;
    private IOnDataLoadComplete listener;
    private HashMap<String, Reminder> allReminders;
    public static AlarmManager alarmManager;

    //------------------------------------------------------------------------------------

    private ReminderRepository() {
        allReminders = new HashMap<>();
        addListener();
    }

    static public ReminderRepository getInstance() {
        if (instance == null) {
            instance = new ReminderRepository();
        }
        return instance;
    }

    private void addListener() {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.REMINDER_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("debug", "Reminder collection listen failed.", e);
                            return;
                        }
                        HashMap<String, Reminder> reminders = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                Map<String, Object> data = doc.getData();
                                String eventId = (String) data.get(Constants.REMINDER_EVENT_ID);
                                int minute = Integer.parseInt((String) data.get(Constants.REMINDER_MINUTE));
                                String text = (String) data.get(Constants.REMINDER_TEXT);
                                String time = (String) data.get(Constants.REMINDER_TIME);
                                Reminder tempReminder = new Reminder(doc.getId(), eventId, minute, text, time);
                                reminders.put(tempReminder.getId(), tempReminder);
                            }
                        }
                        allReminders = reminders;
                        Log.d("debug", "all reminders size = " + allReminders.size());
                        if (reminders.size() > 0) {
                            addAlarmForAllReminders();
                        }
                        listener.notifyOnLoadComplete();
                    }
                });
    }

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
    }

    private void addAlarmForAllReminders() {
        int requestCode = 100;
        Log.d("debug", "add alarms");
        for (Reminder r : allReminders.values()) {
//            Date dateTime = EventRepository.getInstance().getEventStartDateTimeByReminder(r);
//
//            Calendar calendarOfReminder = Calendar.getInstance();
//            calendarOfReminder.setTime(dateTime);
//            calendarOfReminder.add(Calendar.MINUTE, r.getMinute() * (-1));
//            calendarOfReminder.set(Calendar.SECOND, 0);
//            calendarOfReminder.set(Calendar.MILLISECOND, 0);
//
//            Calendar calendarOfCurrentTime = Calendar.getInstance();
//            calendarOfCurrentTime.set(Calendar.SECOND, 0);
//            calendarOfCurrentTime.set(Calendar.MILLISECOND, 0);
//
//            Log.d("debug", CalendarUtil.sdfDayMonthYearTime.format(calendarOfReminder.getTime()) +
//                    " - " + CalendarUtil.sdfDayMonthYearTime.format(calendarOfCurrentTime.getTime()));
//
//            if (calendarOfReminder.compareTo(calendarOfCurrentTime) >= 0) {
//                Intent intent = new Intent(RootActivity.context, MyBroadcastReceiver.class);
//                intent.putExtra("eventId", r.getEventId());
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(RootActivity.context,
//                        requestCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendarOfReminder.getTimeInMillis(), pendingIntent);
//            }
            Calendar reminderCalendar = Calendar.getInstance();
            try {
                reminderCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(r.getTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.set(Calendar.SECOND, 0);
            currentCalendar.set(Calendar.MILLISECOND, 0);
            Log.d("debug", CalendarUtil.sdfDayMonthYearTime.format(reminderCalendar.getTime()) +
                    " - " + CalendarUtil.sdfDayMonthYearTime.format(currentCalendar.getTime()));
            if(reminderCalendar.compareTo(currentCalendar) >= 0) {
                Intent intent = new Intent(RootActivity.context, MyBroadcastReceiver.class);
                intent.putExtra("eventId", r.getEventId());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(RootActivity.context,
                        requestCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    //------------------------------------------------------------------------------------

    private ReminderRepository(final IOnDataLoadComplete listener) {
        this.listener = listener;
        addListener(new ReminderRepository.MyReminderCallback() {
            @Override
            public void onCallback(HashMap<String, Reminder> reminderList) {
                if (reminderList != null) {
                    allReminders = reminderList;
                    if (ReminderRepository.this.listener != null) {
                        ReminderRepository.this.listener.notifyOnLoadComplete();
                    }
                }
            }
        });

        if (allReminders == null) {
            allReminders = new HashMap<>();
        }
    }

    static public ReminderRepository getInstance(IOnDataLoadComplete listener) {
        if (instance == null) {
            instance = new ReminderRepository(listener);
        }
        return instance;
    }

    private void addListener(final ReminderRepository.MyReminderCallback callback) {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.REMINDER_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("debug", "Reminder collection listen failed.", e);
                            return;
                        }
                        HashMap<String, Reminder> reminderList = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                Map<String, Object> data = doc.getData();
                                String eventId = (String) data.get(Constants.REMINDER_EVENT_ID);
                                int minute = Integer.parseInt((String) data.get(Constants.REMINDER_MINUTE));
                                String text = (String) data.get(Constants.REMINDER_TEXT);
                                String time = (String) data.get(Constants.REMINDER_TIME);
                                Reminder tempReminder = new Reminder(doc.getId(), eventId, minute, text, time);
                                reminderList.put(tempReminder.getId(), tempReminder);
                            }
                        }
                        callback.onCallback(reminderList);
                    }
                });
    }

    //------------------------------------------------------------------------------------

    public HashMap<String, Reminder> getAllReminders() {
        return allReminders;
    }

    public void addReminderToDatabase(Reminder reminder) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.REMINDER_EVENT_ID, reminder.getEventId());
        data.put(Constants.REMINDER_MINUTE, "" + reminder.getMinute());
        data.put(Constants.REMINDER_TEXT, reminder.getText());
        data.put(Constants.REMINDER_TIME, reminder.getTime());

        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.REMINDER_COLLECTION)
                .add(data);
    }

    public void addRemindersByEventId(final ArrayList<Reminder> reminders, String eventId) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
        for (Reminder reminder : reminders) {
            DocumentReference reminderDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.REMINDER_COLLECTION).document();
            Map<String, Object> reminderData = new HashMap<>();
            reminderData.put(Constants.REMINDER_EVENT_ID, eventId);
            reminderData.put(Constants.REMINDER_MINUTE, "" + reminder.getMinute());
            reminderData.put(Constants.REMINDER_TEXT, reminder.getText());
            reminderData.put(Constants.REMINDER_TIME, reminder.getTime());

            batch.set(reminderDocRef, reminderData);
        }
        batch.commit();
    }

    public void updateRemindersByEventId(ArrayList<Reminder> reminders, String eventId) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        ArrayList<Integer> oldRemindersMinutes = getRemindersMinutesByEventId(eventId);
        ArrayList<Integer> newRemindersMinutes = new ArrayList<>();
        for (Reminder r : reminders) {
            newRemindersMinutes.add(r.getMinute());
            if (!oldRemindersMinutes.contains(r.getMinute())) {
                DocumentReference reminderDocRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.REMINDER_COLLECTION).document();
                Map<String, Object> reminderData = new HashMap<>();
                reminderData.put(Constants.REMINDER_EVENT_ID, eventId);
                reminderData.put(Constants.REMINDER_MINUTE, "" + r.getMinute());
                reminderData.put(Constants.REMINDER_TEXT, r.getText());
                reminderData.put(Constants.REMINDER_TIME, r.getTime());
                batch.set(reminderDocRef, reminderData);
            }
        }
        ArrayList<Reminder> oldReminders = getRemindersInArrayListByEventId(eventId);
        for (Reminder r : oldReminders) {
            if (!newRemindersMinutes.contains(r.getMinute())) {
                DocumentReference reminderDocRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.REMINDER_COLLECTION).document(r.getId());
                batch.delete(reminderDocRef);
            }
        }

        batch.commit();
    }

    public void deleteRemindersByEventId(String eventId) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
        for (Reminder reminder : allReminders.values()) {
            if (reminder.getEventId().equals(eventId)) {
                DocumentReference reminderDocRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.REMINDER_COLLECTION).document(reminder.getId());
                batch.delete(reminderDocRef);
            }
        }
        batch.commit();
    }

    public ArrayList<Reminder> getRemindersInArrayListByEventId(String eventId) {
        ArrayList<Reminder> reminders = new ArrayList<>();
        for (Reminder reminder : allReminders.values()) {
            if (reminder.getEventId().equals(eventId)) {
                reminders.add(reminder);
            }
        }
        return reminders;
    }

    public ArrayList<Integer> getRemindersMinutesByEventId(String eventId) {
        ArrayList<Integer> remindersMinutes = new ArrayList<>();
        for (Reminder reminder : allReminders.values()) {
            if (reminder.getEventId().equals(eventId)) {
                remindersMinutes.add(reminder.getMinute());
            }
        }
        return remindersMinutes;
    }

    public ArrayList<String> getRemindersIdsByEventId(String eventId) {
        ArrayList<String> remindersIds = new ArrayList<>();
        for (Reminder r : allReminders.values()) {
            if (r.getEventId().equals(eventId)) {
                remindersIds.add(r.getId());
            }
        }
        return remindersIds;
    }

    private interface MyReminderCallback {
        void onCallback(HashMap<String, Reminder> reminderList);
    }

    public static ArrayList<Reminder> defaultReminders = new ArrayList<Reminder>() {
        {
            add(new Reminder("", "", 0, "Thời điểm diễn ra",""));
            add(new Reminder("", "", 10, "10 phút", ""));
            add(new Reminder("", "", 20, "20 phút",""));
            add(new Reminder("", "", 30, "30 phút", ""));
            add(new Reminder("", "", 60, "1 giờ",""));
            add(new Reminder("", "", 120, "2 giờ", ""));
            add(new Reminder("", "", 240, "4 giờ",""));
            add(new Reminder("", "", 480, "8 giờ", ""));
            add(new Reminder("", "", 1440, "1 ngày",""));
            add(new Reminder("", "", 2880, "2 ngày",""));
        }
    };
}
