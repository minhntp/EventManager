package com.nqm.event_manager.repositories;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class EventRepository {

    static EventRepository instance;
    IOnDataLoadComplete listener;
    boolean isLoaded;
    private HashMap<String, Event> allEvents;

    private EventRepository(final IOnDataLoadComplete listener) {
//        allEvents = new ArrayList<>();
        this.listener = listener;
        addListener(new MyEventCallback() {
            @Override
            public void onCallback(HashMap<String, Event> eventList) {
                if (eventList != null) {
                    allEvents = eventList;
                    if (EventRepository.this.listener != null) {
                        isLoaded = true;
                        EventRepository.this.listener.notifyOnLoadComplete();
                    }
                }
            }
        });
        if (allEvents == null) {
            allEvents = new HashMap<>();
        }
    }

    static public EventRepository getInstance(IOnDataLoadComplete listener) {
        if (instance == null) {
            instance = new EventRepository(listener);
        } else {
            instance.listener = listener;
            if (instance.isLoaded && listener != null) {
                instance.listener.notifyOnLoadComplete();
            }
        }
        return instance;
    }

    private void addListener(final MyEventCallback callback) {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EVENT_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("debug", "Events listen failed.", e);
                            return;
                        }
                        HashMap<String, Event> events = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> tempHashMap = doc.getData();
                            Event tempEvent = new Event(doc.getId(),
                                    (String) tempHashMap.get(Constants.EVENT_NAME),
                                    (String) tempHashMap.get(Constants.EVENT_START_DATE),
                                    (String) tempHashMap.get(Constants.EVENT_END_DATE),
                                    (String) tempHashMap.get(Constants.EVENT_START_TIME),
                                    (String) tempHashMap.get(Constants.EVENT_END_TIME),
                                    (String) tempHashMap.get(Constants.EVENT_LOCATION),
                                    (String) tempHashMap.get(Constants.EVENT_NOTE));
                            events.put(tempEvent.getId(), tempEvent);
                        }
                        callback.onCallback(events);
                    }
                });
    }

    public HashMap<String, Event> getAllEvents() {
        return allEvents;
    }

    public void addEventToDatabase(Event event, final ArrayList<Salary> salaries, final ArrayList<Schedule> schedules,
                                   final MyAddEventCallback callback) {
        HashMap<String, String> data = new HashMap<>();
        data.put(Constants.EVENT_NAME, event.getTen());
        data.put(Constants.EVENT_START_DATE, event.getNgayBatDau());
        data.put(Constants.EVENT_END_DATE, event.getNgayKetThuc());
        data.put(Constants.EVENT_START_TIME, event.getGioBatDau());
        data.put(Constants.EVENT_END_TIME, event.getGioKetThuc());
        data.put(Constants.EVENT_LOCATION, event.getDiaDiem());
        data.put(Constants.EVENT_NOTE, event.getGhiChu());

        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EVENT_COLLECTION)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(final DocumentReference documentReference) {
                        ArrayList<Salary> tempSalaries = new ArrayList<>(salaries);
                        ArrayList<Schedule> tempSchedules = new ArrayList<>(schedules);
                        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
                        for (Salary salary : tempSalaries) {
                            DocumentReference salaryDocRef = DatabaseAccess.getInstance().getDatabase()
                                    .collection(Constants.SALARY_COLLECTION).document();
                            HashMap<String, String> salaryData = new HashMap<>();
                            salaryData.put(Constants.SALARY_EVENT_ID, documentReference.getId());
                            salaryData.put(Constants.SALARY_EMPLOYEE_ID, salary.getEmployeeId());
                            salaryData.put(Constants.SALARY_SALARY, "" + salary.getSalary());
                            salaryData.put(Constants.SALARY_PAID, Boolean.toString(salary.isPaid()));
                            batch.set(salaryDocRef, salaryData);
                        }
                        for (Schedule schedule : tempSchedules) {
                            DocumentReference scheduleDocRef = DatabaseAccess.getInstance().getDatabase()
                                    .collection(Constants.SCHEDULE_COLLECTION).document();
                            HashMap<String, String> scheduleData = new HashMap<>();
                            scheduleData.put(Constants.SCHEDULE_EVENT_ID, documentReference.getId());
                            scheduleData.put(Constants.SCHEDULE_TIME, schedule.getTime());
                            scheduleData.put(Constants.SCHEDULE_CONTENT, schedule.getContent());
                            batch.set(scheduleDocRef, scheduleData);
                        }
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                callback.onCallback(documentReference.getId());
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("debug", "Thêm sự kiện thất bại");
                    }
                });
    }

    public void deleteEventFromDatabase(final String eventId, final MyDeleteEventCallback callback) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        DocumentReference eventDocRef = DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EVENT_COLLECTION).document(eventId);
        batch.delete(eventDocRef);

        for (Salary salary : SalaryRepository.getInstance(null).getAllSalaries().values()) {
            if (salary.getEventId().equals(eventId)) {
                DocumentReference salaryDocRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.SALARY_COLLECTION).document(salary.getSalaryId());
                batch.delete(salaryDocRef);
            }
        }

        for (Schedule schedule : ScheduleRepository.getInstance(null).getAllSchedules().values()) {
            if (schedule.getEventId().equals(eventId)) {
                DocumentReference scheduleDocRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.SCHEDULE_COLLECTION).document(schedule.getScheduleId());
                batch.delete(scheduleDocRef);
            }
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callback.onCallback(true);
            }
        });

//        DatabaseAccess.getInstance().getDatabase()
//                .collection(Constants.EVENT_COLLECTION)
//                .document(eventId)
//                .delete()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("debug", "Xóa sự kiện " + eventId + " thành công");
//                        SalaryRepository.getInstance(null).deleteSalariesByEventId(eventId, new SalaryRepository.MyDeleteSalariesByEventIdCallback() {
//                            @Override
//                            public void onCallback(boolean deleteSucceed) {
//                                callback.onCallback(true, deleteSucceed);
//                            }
//                        });
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("debug", "Xóa sự kiện " + eventId + " thất bại");
//                        callback.onCallback(false, false);
//                    }
//                });
    }

    public int getNumberOfEventsThroughDate(String date) {
        return getEventsThroughDate(date).size();
    }

    public void updateEventToDatabase(Event changedEvent, ArrayList<String> deleteEmployeesIds,
                                      ArrayList<String> addEmployeesIds, ArrayList<Schedule> addSchedules,
                                      final MyUpdateEventCallback callback) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        DocumentReference eventDocRef = DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EVENT_COLLECTION).document(changedEvent.getId());
        Map<String, Object> eventData = new HashMap<>();
        eventData.put(Constants.EVENT_NAME, changedEvent.getTen());
        eventData.put(Constants.EVENT_START_DATE, changedEvent.getNgayBatDau());
        eventData.put(Constants.EVENT_END_DATE, changedEvent.getNgayKetThuc());
        eventData.put(Constants.EVENT_START_TIME, changedEvent.getGioBatDau());
        eventData.put(Constants.EVENT_END_TIME, changedEvent.getGioKetThuc());
        eventData.put(Constants.EVENT_LOCATION, changedEvent.getDiaDiem());
        eventData.put(Constants.EVENT_NOTE, changedEvent.getGhiChu());
        batch.update(eventDocRef, eventData);

        for (String deleteEmployeeId : deleteEmployeesIds) {
            String deleteSalaryId = SalaryRepository.getInstance(null)
                    .getSalaryIdByEventIdAndEmployeeId(changedEvent.getId(), deleteEmployeeId);
            if (!deleteSalaryId.equals("")) {
                DocumentReference deleteSalaryDocRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.SALARY_COLLECTION).document(deleteSalaryId);
                batch.delete(deleteSalaryDocRef);
            }
        }

        for (String addEmployeeId : addEmployeesIds) {
            DocumentReference addSalaryDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION).document();
            Map<String, Object> addSalaryData = new HashMap<>();
            addSalaryData.put(Constants.SALARY_EVENT_ID, changedEvent.getId());
            addSalaryData.put(Constants.SALARY_EMPLOYEE_ID, addEmployeeId);
            addSalaryData.put(Constants.SALARY_SALARY, "0");
            addSalaryData.put(Constants.SALARY_PAID, "false");
            batch.set(addSalaryDocRef, addSalaryData);
        }

        for (String deleteScheduleId : ScheduleRepository.getInstance(null)
                .getSchedulesIdsByEventId(changedEvent.getId())) {
            DocumentReference deleteScheduleDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SCHEDULE_COLLECTION).document(deleteScheduleId);
            batch.delete(deleteScheduleDocRef);
        }

        for (Schedule addSchedule : addSchedules) {
            DocumentReference addScheduleDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SCHEDULE_COLLECTION).document();
            Map<String, Object> addScheduleData = new HashMap<>();
            addScheduleData.put(Constants.SCHEDULE_EVENT_ID, changedEvent.getId());
            addScheduleData.put(Constants.SCHEDULE_CONTENT, addSchedule.getContent());
            addScheduleData.put(Constants.SCHEDULE_TIME, addSchedule.getTime());
            batch.set(addScheduleDocRef, addScheduleData);
        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callback.onCallback(true);
            }
        });
    }

    public HashMap<String, Event> getEventsByDate(String date) {
        Log.d("debug", "EventRepository: getting event on date: " + date);
        HashMap<String, Event> events = new HashMap<>();
        if (getAllEvents().size() > 0) {
            for (String eventID : allEvents.keySet()) {
                if (allEvents.get(eventID).getNgayBatDau().equals(date)) {
                    events.put(eventID, allEvents.get(eventID));
                }
            }
        }
        return events;
    }

    public Event getEventByEventId(String id) {
        return allEvents.get(id);
    }

    public HashMap<String, Event> getEventsThroughDate(String date) {
        HashMap<String, Event> events = new HashMap<>();
        if (getAllEvents().size() > 0) {
            for (Event tempE : allEvents.values()) {
                try {
                    if (CalendarUtil.sdfDayMonthYear.parse(tempE.getNgayBatDau()).compareTo(
                            CalendarUtil.sdfDayMonthYear.parse(date)) <= 0 &&
                            CalendarUtil.sdfDayMonthYear.parse(tempE.getNgayKetThuc()).compareTo(
                                    CalendarUtil.sdfDayMonthYear.parse(date)) >= 0) {
                        events.put(tempE.getId(), tempE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return events;
    }

    private interface MyEventCallback {
        void onCallback(HashMap<String, Event> eventList);
    }

    public interface MyAddEventCallback {
        void onCallback(String eventId);
    }

    public interface MyDeleteEventCallback {
        void onCallback(boolean deleteEventSucceed);
    }

    public interface MyUpdateEventCallback {
        void onCallback(boolean updateEventSucceed);
    }
}
