package com.nqm.event_manager.repositories;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.application.EventManager;
import com.nqm.event_manager.broadcast_receivers.ReminderNotificationReceiver;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;

public class ReminderRepository implements IOnDataLoadComplete {
    static ReminderRepository instance;
    private IOnDataLoadComplete listener;
    private HashMap<String, Reminder> allReminders;
    public static AlarmManager alarmManager;
    private static int numberOfSetAlarms;

    //------------------------------------------------------------------------------------

    private ReminderRepository() {
//        allReminders = new HashMap<>();
        EventRepository.getInstance().setListener(this);
        alarmManager = (AlarmManager) EventManager.getAppContext().getSystemService(Context.ALARM_SERVICE);
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
                                String minuteString = (String) data.get(Constants.REMINDER_MINUTE);
                                int minute = 0;
                                if (minuteString != null) {
                                    if (!minuteString.isEmpty()) {
                                        minute = Integer.parseInt(minuteString);
                                    }
                                }
                                String time = (String) data.get(Constants.REMINDER_TIME);
                                Reminder tempReminder = new Reminder(doc.getId(), eventId, minute, time);
                                reminders.put(tempReminder.getId(), tempReminder);
                            }
                        }
                        allReminders = reminders;
//                        Log.d("debug", "all reminders size = " + allReminders.size());
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

    public void addAlarmForAllReminders() {
        if (EventRepository.getInstance().getAllEvents() == null || allReminders == null) {
            return;
        }
        //CANCEL OLD ALARMS
        for (int i = 0; i < numberOfSetAlarms; i++) {
            Intent intent = new Intent(EventManager.getAppContext(), ReminderNotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(EventManager.getAppContext(),
                    i, intent, PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }
        }

        //SET NEW ALARMS
        int requestCode = 0;
        for (Reminder r : allReminders.values()) {
            Event event = EventRepository.getInstance().getEventByEventId(r.getEventId());
            if (event == null) {
                continue;
            }

            Calendar reminderCalendar = Calendar.getInstance();
            try {
                reminderCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(r.getTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.set(Calendar.SECOND, 0);
            currentCalendar.set(Calendar.MILLISECOND, 0);

            if (reminderCalendar.compareTo(currentCalendar) >= 0) {
                Intent intent = new Intent(EventManager.getAppContext(), ReminderNotificationReceiver.class);
//                Log.d("debug", "eventId" + r.getEventId());
                String content = "Địa điểm: " + "\n" +
                        "\t" + event.getDiaDiem() + "\n" +
                        "Thời gian" + "\n" +
                        "\t" + event.getNgayBatDau() + " - " + event.getGioBatDau() + "\n" +
                        "\t" + event.getNgayKetThuc() + " - " + event.getGioKetThuc();
                intent.putExtra(Constants.INTENT_EVENT_ID, event.getId());
                intent.putExtra(Constants.INTENT_EVENT_TITLE, event.getTen());
                intent.putExtra(Constants.INTENT_EVENT_LOCATION, event.getDiaDiem());
                intent.putExtra(Constants.INTENT_EVENT_CONTENT, content);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(EventManager.getAppContext(),
                        requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderCalendar.getTimeInMillis(), pendingIntent);

//                Log.d("debug", "set alarm, request code = " + requestCode);
                requestCode++;
            }
            numberOfSetAlarms = requestCode;
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
//                            Log.d("debug", "Reminder collection listen failed.", e);
                            return;
                        }
                        HashMap<String, Reminder> reminderList = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                Map<String, Object> data = doc.getData();
                                String eventId = (String) data.get(Constants.REMINDER_EVENT_ID);
                                int minute = Integer.parseInt((String) data.get(Constants.REMINDER_MINUTE));
                                String time = (String) data.get(Constants.REMINDER_TIME);
                                Reminder tempReminder = new Reminder(doc.getId(), eventId, minute, time);
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
            reminderData.put(Constants.REMINDER_TIME, reminder.getTime());

            batch.set(reminderDocRef, reminderData);
        }
        batch.commit();
    }

    public void updateRemindersByEventId(ArrayList<Reminder> newReminders, String eventId) {
        deleteRemindersByEventId(eventId);
        addRemindersByEventId(newReminders, eventId);
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

    public HashMap<Integer, Reminder> getRemindersInHashMapByEventId(String eventId) {
        HashMap<Integer, Reminder> reminders = new HashMap<>();
        for (Reminder reminder : allReminders.values()) {
            if (reminder.getEventId().equals(eventId)) {
                reminders.put(reminder.getMinute(), reminder);
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

    @Override
    public void notifyOnLoadComplete() {
        addAlarmForAllReminders();
    }

    private interface MyReminderCallback {
        void onCallback(HashMap<String, Reminder> reminderList);
    }

    public static ArrayList<Reminder> defaultReminders = new ArrayList<Reminder>() {
        {
            add(new Reminder("", "", 0, ""));
            add(new Reminder("", "", 10, ""));
            add(new Reminder("", "", 20, ""));
            add(new Reminder("", "", 30, ""));
            add(new Reminder("", "", 60, ""));
            add(new Reminder("", "", 120, ""));
            add(new Reminder("", "", 240, ""));
            add(new Reminder("", "", 480, ""));
            add(new Reminder("", "", 1440, ""));
            add(new Reminder("", "", 2880, ""));
        }
    };

    public static HashMap<Integer, String> defaultRemindersMap = new HashMap<Integer, String>() {
        {
            put(0, "Thời điểm diễn ra");
            put(10, "10 phút");
            put(20, "20 phút");
            put(30, "30 phút");
            put(60, "1 giờ");
            put(120, "2 giờ");
            put(240, "3 giờ");
            put(480, "4 giờ");
            put(1440, "1 ngày");
            put(2880, "2 ngày");
        }
    };
    //----------------------------------------------------------------------------------------------


    //----------------------------------------------------------------------------------------------
    public static void sortReminder(ArrayList<Reminder> reminders) {
        Collections.sort(reminders, new Comparator<Reminder>() {
            @Override
            public int compare(Reminder r1, Reminder r2) {
                return r1.getMinute() - r2.getMinute();
            }
        });
    }

    public static void sortReminder(HashMap<Integer, Reminder> reminders) {
        TreeMap<Integer, Reminder> sortedTreeMap = new TreeMap<>(reminders);
        reminders.putAll(sortedTreeMap);
    }
}
