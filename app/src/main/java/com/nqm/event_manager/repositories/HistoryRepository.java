package com.nqm.event_manager.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.History;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryRepository {

    private static HistoryRepository instance;
    private IOnDataLoadComplete listener;
    private final Map<String, History> allHistories = new HashMap<>();

    //------------------------------------------------------------------------------------

    private HistoryRepository() {
        addDatabaseSnapshotListener();
    }

    static public HistoryRepository getInstance() {
        if (instance == null) {
            instance = new HistoryRepository();
        }
        return instance;
    }

    static public HistoryRepository getInstance(IOnDataLoadComplete listener) {
        if (instance == null) {
            instance = new HistoryRepository();
        }
        instance.setListener(listener);
        return instance;
    }

    //------------------------------------------------------------------------------------

    private void addDatabaseSnapshotListener() {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.HISTORY_COLLECTION)
                .addSnapshotListener((queryDocumentSnapshots, exception) -> {
                    if (exception != null) {
                        Log.w("debug", "History collection listen failed.", exception);
                        return;
                    }

                    Map<String, History> histories = new HashMap<>();
                    if (queryDocumentSnapshots != null) {
                        History tempHistory;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Map<String, Object> data = doc.getData();
                                tempHistory = new History(doc.getId(),
                                        Long.parseLong(String.valueOf(data.get(Constants.HISTORY_EDITED_TIME))),
                                        Long.parseLong(String.valueOf(data.get(Constants.HISTORY_DATETIME))),
                                        (String) data.get(Constants.HISTORY_EVENT_NAME),
                                        (String) data.get(Constants.HISTORY_EVENT_LOCATION),
                                        (String) data.get(Constants.HISTORY_EMPLOYEE_NAME),
                                        (String) data.get(Constants.HISTORY_EMPLOYEE_SPECIALITY),
                                        (String) data.get(Constants.HISTORY_OLD_SALARY),
                                        (String) data.get(Constants.HISTORY_NEW_SALARY)
                                );
                                histories.put(tempHistory.getHistoryId(), tempHistory);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    allHistories.clear();
                    allHistories.putAll(histories);
                    if (listener != null) {
                        listener.notifyOnLoadComplete();
                    }
                });
    }

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
    }

    //-------------------------------------- WRITE ----------------------------------------------

    public void addHistoriesToDatabase(List<History> histories) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        for (History history : histories) {
            if (!history.getOldSalary().toString().equals(history.getNewSalary().toString())) {
                DocumentReference historyDocRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.HISTORY_COLLECTION).document();

                Map<String, String> historyData = new HashMap<>();
                historyData.put(Constants.HISTORY_EDITED_TIME, String.valueOf(history.getEditedDateTimeInMillis()));
                historyData.put(Constants.HISTORY_DATETIME, String.valueOf(history.getDateTimeInMillis()));
                historyData.put(Constants.HISTORY_EVENT_NAME, history.getEventName());
                historyData.put(Constants.HISTORY_EVENT_LOCATION, history.getEventLocation());
                historyData.put(Constants.HISTORY_EMPLOYEE_NAME, history.getEmployeeName());
                historyData.put(Constants.HISTORY_EMPLOYEE_SPECIALITY, history.getEmployeeSpeciality());
                historyData.put(Constants.HISTORY_OLD_SALARY, history.getOldSalary().toString());
                historyData.put(Constants.HISTORY_NEW_SALARY, history.getNewSalary().toString());

                batch.set(historyDocRef, historyData);
            }
        }

        batch.commit();
    }

    //------------------------------------------ READ ----------------------------------------------

    public Map<String, History> getAllHistories() {
        return allHistories;
    }


    public List<History> getAllHistoriesSortedByDateTime() {
        // Compare by minute
        List<History> sortedHistories = new ArrayList<>(allHistories.values());
        sortedHistories.sort(
                (h1, h2) ->
                        (Instant.ofEpochMilli(h2.getEditedDateTimeInMillis()).compareTo(
                                Instant.ofEpochMilli(h1.getEditedDateTimeInMillis()))));
        return sortedHistories;
    }

}
