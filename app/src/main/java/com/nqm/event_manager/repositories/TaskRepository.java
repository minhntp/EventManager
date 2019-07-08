package com.nqm.event_manager.repositories;

import androidx.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.EventTask;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TaskRepository {

    static TaskRepository instance;
    private IOnDataLoadComplete listener;
    private HashMap<String, EventTask> allTasks;

    private TaskRepository() {
//        allTasks = new HashMap<>();
        addListener();
    }

    static public TaskRepository getInstance() {
        if (instance == null) {
            instance = new TaskRepository();
        }
        return instance;
    }

    private void addListener() {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.TASK_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("debug", "Schedule collection listen failed.", e);
                            return;
                        }
                        HashMap<String, EventTask> tasks = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                Map<String, Object> data = doc.getData();
                                int order = 0;
                                if (data.get(Constants.TASK_ORDER) != null) {
                                    order = Integer.parseInt((String) data.get(Constants.TASK_ORDER));
                                }
                                EventTask tempEventTask = new EventTask(doc.getId(),
                                        (String) data.get(Constants.TASK_EVENT_ID),
                                        (String) data.get(Constants.TASK_DATE),
                                        (String) data.get(Constants.TASK_TIME),
                                        (String) data.get(Constants.TASK_CONTENT),
                                        Boolean.parseBoolean((String) data.get(Constants.TASK_IS_DONE)),
                                        order);
                                tasks.put(tempEventTask.getId(), tempEventTask);
                            }
                        }
                        allTasks = tasks;
                        listener.notifyOnLoadComplete();
                    }
                });
    }

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
    }

    public HashMap<String, EventTask> getAllTasks() {
        return allTasks;
    }

    //----------------------------------------------------------------------------------------------

    public ArrayList<EventTask> getTasksInArrayListByEventId(String eventId) {
        ArrayList<EventTask> eventTasks = new ArrayList<>();
        for (EventTask eventTask : allTasks.values()) {
            if (eventTask.getEventId().equals(eventId)) {
                eventTasks.add(eventTask);
            }
        }
        return eventTasks;
    }

    public ArrayList<String> getTasksIdsByEventId(String eventId) {
        ArrayList<String> tasksIds = new ArrayList<>();
        for (EventTask eventTask : allTasks.values()) {
            if (eventTask.getEventId().equals(eventId)) {
                tasksIds.add(eventTask.getId());
            }
        }
        return tasksIds;
    }
    //----------------------------------------------------------------------------------------------
    static public void sortTasksByOrder(ArrayList<EventTask> eventTasks) {
        Collections.sort(eventTasks, new Comparator<EventTask>() {
            @Override
            public int compare(EventTask t1, EventTask t2) {
                return t1.getOrder() - t2.getOrder();
            }
        });
    }

    public static void sortTasksByStartDateTime(ArrayList<EventTask> eventTasks) {
        Collections.sort(eventTasks, new Comparator<EventTask>() {
            @Override
            public int compare(EventTask t1, EventTask t2) {
                int compareResult = 0;
                try {
                    String t1Date = t1.getDate();
                    String t2Date = t2.getDate();
                    if (!t1Date.equals(t2Date)) {
                        compareResult = CalendarUtil.sdfDayMonthYear.parse(t1Date)
                                .compareTo(CalendarUtil.sdfDayMonthYear.parse(t2Date));
                    } else {
                        String t1Time = t1.getTime();
                        String t2Time = t2.getTime();
                        if (t1Time.isEmpty())
                            if (t2Time.isEmpty()) {
                                compareResult = 0;
                            } else {
                                compareResult = 1;
                            }
                        else {
                            if (t2Time.isEmpty()) {
                                compareResult = -1;
                            } else {
                                compareResult = CalendarUtil.sdfTime.parse(t1Time)
                                        .compareTo(CalendarUtil.sdfTime.parse(t2Time));
                            }
                        }
                    }
                } catch (Exception e) {
//                    Log.d("debug", "exception sort task by start date time");
                    e.printStackTrace();
                }
                return compareResult;
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    private interface MyTaskCallback {
        void onCallback(HashMap<String, EventTask> taskList);
    }
}
