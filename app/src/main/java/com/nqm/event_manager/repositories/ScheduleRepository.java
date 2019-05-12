package com.nqm.event_manager.repositories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ScheduleRepository {
    static ScheduleRepository instance;
    IOnDataLoadComplete listener;
    private HashMap<String, Schedule> allSchedules;

    private ScheduleRepository(final IOnDataLoadComplete listener) {
        this.listener = listener;
        addListener(new ScheduleRepository.MyScheduleCallback() {
            @Override
            public void onCallback(HashMap<String, Schedule> scheduleList) {
                if (scheduleList != null) {
                    allSchedules = scheduleList;
                    if (ScheduleRepository.this.listener != null) {
                        ScheduleRepository.this.listener.notifyOnLoadComplete();
                    }
                }
            }
        });
        if (allSchedules == null) {
            allSchedules = new HashMap<>();
        }
    }

    static public ScheduleRepository getInstance(IOnDataLoadComplete listener) {
        if (instance == null) {
            instance = new ScheduleRepository(listener);
        }
        return instance;
    }

    public HashMap<String, Schedule> getAllSchedules() {
        return allSchedules;
    }

    private void addListener(final ScheduleRepository.MyScheduleCallback callback) {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SCHEDULE_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("debug", "Schedule collection listen failed.", e);
                            return;
                        }
                        HashMap<String, Schedule> scheduleList = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                Map<String, Object> data = doc.getData();
                                Schedule tempSchedule = new Schedule(doc.getId(),
                                        (String) data.get(Constants.SCHEDULE_EVENT_ID),
                                        (String) data.get(Constants.SCHEDULE_TIME),
                                        (String) data.get(Constants.SCHEDULE_CONTENT));
                                scheduleList.put(tempSchedule.getScheduleId(), tempSchedule);
                            }
                        }
                        callback.onCallback(scheduleList);
                    }
                });
    }

    public void addScheduleToDatabase(Schedule schedule, final MyAddScheduleCallback callback) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.SCHEDULE_EVENT_ID, schedule.getEventId());
        data.put(Constants.SCHEDULE_TIME, schedule.getTime());
        data.put(Constants.SCHEDULE_CONTENT, schedule.getContent());
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SCHEDULE_COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        callback.onCallback(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    public void deleteSchedulesByEventId(String eventId, final MyDeleteSchedulesByEventIdCallback callback) {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SCHEDULE_COLLECTION)
                .whereEqualTo(Constants.SCHEDULE_EVENT_ID, eventId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                DatabaseAccess.getInstance().getDatabase()
                                        .collection(Constants.SCHEDULE_COLLECTION)
                                        .document(doc.getId())
                                        .delete();
                            }
                            callback.onCallback(true);
                        } else {
                            callback.onCallback(false);
                        }
                    }
                });
    }

    public ArrayList<Schedule> getSchedulesInArrayListByEventId(String eventId) {
        ArrayList<Schedule> schedules = new ArrayList<>();
        for (Schedule schedule : allSchedules.values()) {
            if (schedule.getEventId().equals(eventId)) {
                schedules.add(schedule);
            }
        }
        return schedules;
    }

    public ArrayList<String> getSchedulesIdsByEventId(String eventId) {
        ArrayList<String> schedulesIds = new ArrayList<>();
        for (Schedule schedule : allSchedules.values()) {
            if (schedule.getEventId().equals(eventId)) {
                schedulesIds.add(schedule.getScheduleId());
            }
        }
        return schedulesIds;
    }

    public interface MyAddScheduleCallback {
        void onCallback(String scheduleId);
    }

    private interface MyScheduleCallback {
        void onCallback(HashMap<String, Schedule> scheduleList);
    }

    public interface MyDeleteSchedulesByEventIdCallback {
        void onCallback(boolean deleteSucceed);
    }
}
