package com.nqm.event_manager.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultReminderRepository {

    private Set<IOnDataLoadComplete> listeners;
    static DefaultReminderRepository instance;
    private HashMap<String, Integer> defaultReminders;

    public void addListener(IOnDataLoadComplete listener) {
        this.listeners.add(listener);
    }

    private DefaultReminderRepository() {
        listeners = new HashSet<>();
        addListener();
    }

    static public DefaultReminderRepository getInstance() {
        if (instance == null) {
            instance = new DefaultReminderRepository();
        }
        return instance;
    }

    private void addListener() {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.DEFAULT_REMINDER_COLLECTION)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("debug", "Reminder collection listen failed.", e);
                        return;
                    }
                    HashMap<String, Integer> reminders = new HashMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() > 0) {
                            Map<String, Object> data = doc.getData();
                            int minute = Integer.parseInt((String) data.get(Constants.REMINDER_MINUTE));
                            reminders.put(doc.getId(), minute);
                        }
                    }
                    defaultReminders = reminders;
                    for (IOnDataLoadComplete listener : listeners) {
                        listener.notifyOnLoadComplete();
                    }
                });
    }

    public HashMap<String, Integer> getDefaultReminders() {
        return defaultReminders;
    }

    public void updateDefaultReminders(ArrayList<Reminder> reminders) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        for (String id : defaultReminders.keySet()) {
            DocumentReference docRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.DEFAULT_REMINDER_COLLECTION)
                    .document(id);
            batch.delete(docRef);
        }

        for (Reminder reminder : reminders) {
                DocumentReference docRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.DEFAULT_REMINDER_COLLECTION)
                        .document();
                HashMap<String, String> data = new HashMap<>();
                data.put(Constants.REMINDER_MINUTE, "" + reminder.getMinute());
                batch.set(docRef, data);
        }

        batch.commit();
    }
}
