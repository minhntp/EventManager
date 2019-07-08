package com.nqm.event_manager.repositories;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DefaultEmployeeRepository {
    private IOnDataLoadComplete listener;
    static DefaultEmployeeRepository instance;
    private HashMap<String, String> defaultEmployeeIds;

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
    }

    private DefaultEmployeeRepository() {
//        defaultEmployeesIds = new ArrayList<>();
        addListener();
    }

    static public DefaultEmployeeRepository getInstance() {
        if (instance == null) {
            instance = new DefaultEmployeeRepository();
        }
        return instance;
    }

    private void addListener() {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.DEFAULT_EMPLOYEE_COLLECTION)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("debug", "Reminder collection listen failed.", e);
                        return;
                    }
                    HashMap<String, String> ids = new HashMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() > 0) {
                            Map<String, Object> data = doc.getData();
                            String employeeId = (String) data.get(Constants.EMPLOYEE_ID);
                            ids.put(doc.getId(), employeeId);
                        }
                    }
                    defaultEmployeeIds = ids;
                    listener.notifyOnLoadComplete();
                });
    }

    public HashMap<String, String> getDefaultEmployeeIds() {
        return defaultEmployeeIds;
    }

    public void updateDefaultEmployees(ArrayList<String> employeeIds) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        for (String id : defaultEmployeeIds.keySet()) {
            DocumentReference docRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.DEFAULT_EMPLOYEE_COLLECTION)
                    .document(id);
            batch.delete(docRef);
        }

        for (String id : employeeIds) {
            DocumentReference docRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.DEFAULT_EMPLOYEE_COLLECTION)
                    .document();
            HashMap<String, String> data = new HashMap<>();
            data.put(Constants.EMPLOYEE_ID, id);
            batch.set(docRef, data);
        }

        batch.commit();
    }
}
