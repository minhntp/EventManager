package com.nqm.event_manager.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ScheduleRepository {
    static ScheduleRepository instance;
    private IOnDataLoadComplete listener;
    private HashMap<String, Schedule> allSchedules;

    //------------------------------------------------------------------------------------

    private ScheduleRepository() {
        allSchedules = new HashMap<>();
        addListener();
    }

    static public ScheduleRepository getInstance() {
        if (instance == null) {
            instance = new ScheduleRepository();
        }
        return instance;
    }

    private void addListener() {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SCHEDULE_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("debug", "Schedule collection listen failed.", e);
                            return;
                        }
                        HashMap<String, Schedule> schedules = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                Map<String, Object> data = doc.getData();
                                int order = 0;
                                if (data.get(Constants.SCHEDULE_ORDER) != null) {
                                    order = Integer.parseInt((String) data.get(Constants.SCHEDULE_ORDER));
                                }
                                Schedule tempSchedule = new Schedule(doc.getId(),
                                        (String) data.get(Constants.SCHEDULE_EVENT_ID),
                                        (String) data.get(Constants.SCHEDULE_TIME),
                                        (String) data.get(Constants.SCHEDULE_CONTENT),
                                        order);
                                schedules.put(tempSchedule.getScheduleId(), tempSchedule);
                            }
                        }
                        allSchedules = schedules;
                        listener.notifyOnLoadComplete();
                    }
                });
    }

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
    }

    //------------------------------------------------------------------------------------

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
                                int order = 0;
                                if (data.get(Constants.SCHEDULE_ORDER) != null) {
                                    order = Integer.parseInt((String) data.get(Constants.SCHEDULE_ORDER));
                                }
                                Schedule tempSchedule = new Schedule(doc.getId(),
                                        (String) data.get(Constants.SCHEDULE_EVENT_ID),
                                        (String) data.get(Constants.SCHEDULE_TIME),
                                        (String) data.get(Constants.SCHEDULE_CONTENT),
                                        order);
                                scheduleList.put(tempSchedule.getScheduleId(), tempSchedule);
                            }
                        }
                        callback.onCallback(scheduleList);
                    }
                });
    }


    //------------------------------------------------------------------------------------

    public HashMap<String, Schedule> getAllSchedules() {
        return allSchedules;
    }

    public void addScheduleToDatabase(Schedule schedule) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.SCHEDULE_EVENT_ID, schedule.getEventId());
        data.put(Constants.SCHEDULE_TIME, schedule.getTime());
        data.put(Constants.SCHEDULE_CONTENT, schedule.getContent());
        data.put(Constants.SCHEDULE_ORDER, Integer.toString(schedule.getOrder()));
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SCHEDULE_COLLECTION)
                .add(data);
    }

    public void deleteSchedulesByEventId(String eventId) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        for (Schedule schedule : allSchedules.values()) {
            if (schedule.getEventId().equals(eventId)) {
                DocumentReference scheduleDocRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.SCHEDULE_COLLECTION).document(schedule.getScheduleId());
                batch.delete(scheduleDocRef);
            }
        }

        batch.commit();
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

    private interface MyScheduleCallback {
        void onCallback(HashMap<String, Schedule> scheduleList);
    }

    //----------------------------------------------------------------------------------------------
    static public void sortSchedulesByOrder(ArrayList<Schedule> schedules) {
        Collections.sort(schedules, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule schedule1, Schedule schedule2) {
                return schedule1.getOrder() - schedule2.getOrder();
            }
        });
    }

    public static void sortSchedulesByStartTime(ArrayList<Schedule> schedules) {
        Collections.sort(schedules, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule schedule1, Schedule schedule2) {
                if (schedule1.getTime().isEmpty() && schedule2.getTime().isEmpty()) {
                    if (schedule1.getContent().isEmpty() && schedule2.getContent().isEmpty()) {
                        return 0;
                    } else if (schedule1.getContent().isEmpty() && !schedule2.getContent().isEmpty()) {
                        return 1;
                    } else if (!schedule1.getContent().isEmpty() && schedule2.getContent().isEmpty()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (schedule1.getTime().isEmpty() && !schedule2.getTime().isEmpty()) {
                    return 1;
                }
                if (!schedule1.getTime().isEmpty() && schedule2.getTime().isEmpty()) {
                    return -1;
                }
                int compareResult = 0;
                try {
                    compareResult = CalendarUtil.sdfTime.parse(schedule1.getTime()).compareTo(
                            CalendarUtil.sdfTime.parse(schedule2.getTime()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return compareResult;
            }
        });
    }

}
