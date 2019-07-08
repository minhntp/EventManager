package com.nqm.event_manager.repositories;

import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.models.EventTask;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;
import com.nqm.event_manager.utils.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventRepository {

    private static EventRepository instance;
    private IOnDataLoadComplete listener;
    private HashMap<String, Event> allEvents;
    private HashMap<String, ArrayList<String>> numberOfEventsMap;
    private ArrayList<String> titles;
    private ArrayList<String> locations;
    private Calendar calendar = Calendar.getInstance();

    //-------------------------------------------------------------------------------------------
//    private EventRepository(final IOnDataLoadComplete listener) {
//        this.listener = listener;
//        addListener(eventList -> {
//            if (eventList != null) {
//                allEvents = eventList;
//                if (EventRepository.this.listener != null) {
//                    isLoaded = true;
//                    EventRepository.this.listener.notifyOnLoadComplete();
//                }
//            }
//        });
//    }

    private EventRepository() {
//        allEvents = new HashMap<>();
        addListener();
    }
    //-------------------------------------------------------------------------------------------

//    static public EventRepository getInstance(IOnDataLoadComplete listener) {
//        if (instance == null) {
//            instance = new EventRepository(listener);
//        } else {
//            instance.listener = listener;
//            if (instance.isLoaded && listener != null) {
//                instance.listener.notifyOnLoadComplete();
//            }
//        }
//        return instance;
//    }

    static public EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

//    private void addListener(final MyEventCallback callback) {
//        DatabaseAccess.getInstance().getDatabase()
//                .collection(Constants.EVENT_COLLECTION)
//                .addSnapshotListener((queryDocumentSnapshots, e) -> {
//                    if (e != null) {
//                        Log.w("debug", "Events listen failed.", e);
//                        return;
//                    }
//                    HashMap<String, Event> events = new HashMap<>();
//                    if (queryDocumentSnapshots != null) {
//                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                            Map<String, Object> tempHashMap = doc.getData();
//                            Event tempEvent = new Event(doc.getId(),
//                                    (String) tempHashMap.get(Constants.EVENT_NAME),
//                                    (String) tempHashMap.get(Constants.EVENT_START_DATE),
//                                    (String) tempHashMap.get(Constants.EVENT_END_DATE),
//                                    (String) tempHashMap.get(Constants.EVENT_START_TIME),
//                                    (String) tempHashMap.get(Constants.EVENT_END_TIME),
//                                    (String) tempHashMap.get(Constants.EVENT_LOCATION),
//                                    (String) tempHashMap.get(Constants.EVENT_NOTE));
//                            events.put(tempEvent.getId(), tempEvent);
//                        }
//                    }
//                    callback.onCallback(events);
//                });
//    }

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
    }

    //-------------------------------------------------------------------------------------------

    private void addListener() {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EVENT_COLLECTION)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("debug", "Events listen failed.", e);
                        return;
                    }
                    HashMap<String, Event> events = new HashMap<>();
                    HashMap<String, ArrayList<String>> numberOfEvents = new HashMap<>();
                    ArrayList<String> titles = new ArrayList<>();
                    ArrayList<String> locations = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
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
                            if (!titles.contains(tempEvent.getTen())) {
                                titles.add(tempEvent.getTen());
                            }
                            if (!locations.contains(tempEvent.getDiaDiem())) {
                                locations.add(tempEvent.getDiaDiem());
                            }
                            try {
                                Date startDate = CalendarUtil.sdfDayMonthYear.parse(tempEvent.getNgayBatDau());
                                Date endDate = CalendarUtil.sdfDayMonthYear.parse(tempEvent.getNgayKetThuc());
                                while (startDate.compareTo(endDate) <= 0) {
                                    String startString = CalendarUtil.sdfDayMonthYear.format(startDate);
                                    ArrayList<String> arr = numberOfEvents.get(startString);
                                    if (arr == null) {
                                        arr = new ArrayList<>();
                                        arr.add(tempEvent.getId());
                                        numberOfEvents.put(startString, arr);
                                    } else {
                                        arr.add(tempEvent.getId());
                                    }
                                    calendar.setTime(startDate);
                                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                                    startDate = calendar.getTime();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    allEvents = events;
                    numberOfEventsMap = numberOfEvents;
                    this.titles = titles;
                    this.locations = locations;
                    listener.notifyOnLoadComplete();
                });
    }

    public ArrayList<String> getTitles() {
        return titles;
    }

    public ArrayList<String> getLocations() {
        return locations;
    }

    public HashMap<String, ArrayList<String>> getNumberOfEventsMap() {
        return numberOfEventsMap;
    }

    public HashMap<String, Event> getAllEvents() {
        return allEvents;
    }

    public void addEventToDatabase(Event event, ArrayList<Salary> salaries, ArrayList<EventTask> eventTasks,
                                   ArrayList<Schedule> schedules, ArrayList<Reminder> reminders) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        long miliStart, miliEnd;

        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        DocumentReference eventDocRef = DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EVENT_COLLECTION).document();
        HashMap<String, String> eventData = new HashMap<>();
        eventData.put(Constants.EVENT_NAME, event.getTen());
        eventData.put(Constants.EVENT_START_DATE, event.getNgayBatDau());
        eventData.put(Constants.EVENT_END_DATE, event.getNgayKetThuc());
        eventData.put(Constants.EVENT_START_TIME, event.getGioBatDau());
        eventData.put(Constants.EVENT_END_TIME, event.getGioKetThuc());
        eventData.put(Constants.EVENT_LOCATION, event.getDiaDiem());
        eventData.put(Constants.EVENT_NOTE, event.getGhiChu());
        batch.set(eventDocRef, eventData);

        for (Salary salary : salaries) {
            DocumentReference salaryDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION).document();
            HashMap<String, Object> salaryData = new HashMap<>();
            salaryData.put(Constants.SALARY_EVENT_ID, eventDocRef.getId());
            salaryData.put(Constants.SALARY_EMPLOYEE_ID, salary.getEmployeeId());
            salaryData.put(Constants.SALARY_SALARY, "" + salary.getSalary());
            salaryData.put(Constants.SALARY_PAID, Boolean.toString(salary.isPaid()));
            try {
                c1.setTime(CalendarUtil.sdfDayMonthYear.parse(event.getNgayBatDau()));
                c2.setTime(CalendarUtil.sdfTime.parse(event.getGioBatDau()));
                c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
                c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
                miliStart = c1.getTimeInMillis();

                c1.setTime(CalendarUtil.sdfDayMonthYear.parse(event.getNgayKetThuc()));
                c2.setTime(CalendarUtil.sdfTime.parse(event.getGioKetThuc()));
                c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
                c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
                miliEnd = c1.getTimeInMillis();

                salaryData.put(Constants.SALARY_START_MILI, miliStart);
                salaryData.put(Constants.SALARY_END_MILI, miliEnd);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            batch.set(salaryDocRef, salaryData);
        }

        for (EventTask eventTask : eventTasks) {
            DocumentReference taskDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.TASK_COLLECTION).document();
            HashMap<String, String> taskData = new HashMap<>();
            taskData.put(Constants.TASK_EVENT_ID, eventDocRef.getId());
            taskData.put(Constants.TASK_DATE, eventTask.getDate());
            taskData.put(Constants.TASK_TIME, eventTask.getTime());
            taskData.put(Constants.TASK_CONTENT, eventTask.getContent());
            taskData.put(Constants.TASK_IS_DONE, "" + eventTask.isDone());
            taskData.put(Constants.TASK_ORDER, Integer.toString(eventTask.getOrder()));
            batch.set(taskDocRef, taskData);
        }

        for (Schedule schedule : schedules) {
            DocumentReference scheduleDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SCHEDULE_COLLECTION).document();
            HashMap<String, String> scheduleData = new HashMap<>();
            scheduleData.put(Constants.SCHEDULE_EVENT_ID, eventDocRef.getId());
            scheduleData.put(Constants.SCHEDULE_TIME, schedule.getTime());
            scheduleData.put(Constants.SCHEDULE_CONTENT, schedule.getContent());
            scheduleData.put(Constants.SCHEDULE_ORDER, Integer.toString(schedule.getOrder()));
            batch.set(scheduleDocRef, scheduleData);
        }

        for (Reminder reminder : reminders) {
            DocumentReference reminderDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.REMINDER_COLLECTION).document();
            HashMap<String, String> reminderData = new HashMap<>();
            reminderData.put(Constants.REMINDER_EVENT_ID, eventDocRef.getId());
            reminderData.put(Constants.REMINDER_MINUTE, "" + reminder.getMinute());
            reminderData.put(Constants.REMINDER_TIME, reminder.getTime());
            batch.set(reminderDocRef, reminderData);
        }

        batch.commit();
    }

    public void deleteEventFromDatabase(final String eventId) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        DocumentReference eventDocRef = DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EVENT_COLLECTION).document(eventId);
        batch.delete(eventDocRef);

        for (Salary salary : SalaryRepository.getInstance().getSalariesByEventId(eventId)) {
            DocumentReference salaryDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION).document(salary.getSalaryId());
            batch.delete(salaryDocRef);
        }

        for (String scheduleId : ScheduleRepository.getInstance().getSchedulesIdsByEventId(eventId)) {
            DocumentReference scheduleDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SCHEDULE_COLLECTION).document(scheduleId);
            batch.delete(scheduleDocRef);
        }

        for (String reminderId : ReminderRepository.getInstance().getRemindersIdsByEventId(eventId)) {
            DocumentReference reminderDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.REMINDER_COLLECTION).document(reminderId);
            batch.delete(reminderDocRef);
        }

        batch.commit();
    }

//    public int getNumberOfEventsThroughDate(String date, String eventId) {
//        if (eventId.isEmpty()) {
//            return getEventsThroughDate(date).size();
//        } else {
//            return getEventsThroughDateEdit(date, eventId).size();
//        }
//    }

    public void updateEventToDatabase(Event changedEvent, ArrayList<String> deleteEmployeesIds,
                                      ArrayList<String> addEmployeesIds, ArrayList<EventTask> eventTasks,
                                      ArrayList<Schedule> schedules, ArrayList<Reminder> reminders) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        long miliStart, miliEnd;
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

        for (Salary s : SalaryRepository.getInstance().getAllSalaries().values()) {
            if (s.getEventId().equals(changedEvent.getId())) {
                DocumentReference docRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.SALARY_COLLECTION).document(s.getSalaryId());
                Map<String, Object> data = new HashMap<>();
                try {
                    c1.setTime(CalendarUtil.sdfDayMonthYear.parse(changedEvent.getNgayBatDau()));
                    c2.setTime(CalendarUtil.sdfTime.parse(changedEvent.getGioBatDau()));
                    c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
                    c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
                    miliStart = c1.getTimeInMillis();

                    c1.setTime(CalendarUtil.sdfDayMonthYear.parse(changedEvent.getNgayKetThuc()));
                    c2.setTime(CalendarUtil.sdfTime.parse(changedEvent.getGioKetThuc()));
                    c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
                    c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
                    miliEnd = c1.getTimeInMillis();

                    data.put(Constants.SALARY_START_MILI, miliStart);
                    data.put(Constants.SALARY_END_MILI, miliEnd);
                    batch.update(docRef, data);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        for (String deleteEmployeeId : deleteEmployeesIds) {
            String deleteSalaryId = SalaryRepository.getInstance()
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
            try {
                c1.setTime(CalendarUtil.sdfDayMonthYear.parse(changedEvent.getNgayBatDau()));
                c2.setTime(CalendarUtil.sdfTime.parse(changedEvent.getGioBatDau()));
                c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
                c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
                miliStart = c1.getTimeInMillis();

                c1.setTime(CalendarUtil.sdfDayMonthYear.parse(changedEvent.getNgayKetThuc()));
                c2.setTime(CalendarUtil.sdfTime.parse(changedEvent.getGioKetThuc()));
                c1.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
                c1.set(Calendar.MINUTE, c2.get(Calendar.MINUTE));
                miliEnd = c1.getTimeInMillis();

                addSalaryData.put(Constants.SALARY_START_MILI, miliStart);
                addSalaryData.put(Constants.SALARY_END_MILI, miliEnd);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            batch.set(addSalaryDocRef, addSalaryData);
        }

        for (String deleteScheduleId : ScheduleRepository.getInstance()
                .getSchedulesIdsByEventId(changedEvent.getId())) {
            DocumentReference deleteScheduleDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SCHEDULE_COLLECTION).document(deleteScheduleId);
            batch.delete(deleteScheduleDocRef);
        }

        for (Schedule addSchedule : schedules) {
            DocumentReference addScheduleDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SCHEDULE_COLLECTION).document();
            Map<String, Object> addScheduleData = new HashMap<>();
            addScheduleData.put(Constants.SCHEDULE_EVENT_ID, changedEvent.getId());
            addScheduleData.put(Constants.SCHEDULE_CONTENT, addSchedule.getContent());
            addScheduleData.put(Constants.SCHEDULE_TIME, addSchedule.getTime());
            addScheduleData.put(Constants.SCHEDULE_ORDER, Integer.toString(addSchedule.getOrder()));
            batch.set(addScheduleDocRef, addScheduleData);
        }

        for (String deleteTaskId : TaskRepository.getInstance()
                .getTasksIdsByEventId(changedEvent.getId())) {
            DocumentReference deleteTaskDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.TASK_COLLECTION).document(deleteTaskId);
            batch.delete(deleteTaskDocRef);
        }

        for (EventTask t : eventTasks) {
            DocumentReference addTaskDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.TASK_COLLECTION).document();
            Map<String, Object> addTaskData = new HashMap<>();
            addTaskData.put(Constants.TASK_EVENT_ID, changedEvent.getId());
            addTaskData.put(Constants.TASK_DATE, t.getDate());
            addTaskData.put(Constants.TASK_TIME, t.getTime());
            addTaskData.put(Constants.TASK_CONTENT, t.getContent());
            addTaskData.put(Constants.TASK_IS_DONE, "" + t.isDone());
            addTaskData.put(Constants.TASK_ORDER, Integer.toString(t.getOrder()));
            batch.set(addTaskDocRef, addTaskData);
        }

        for (String reminderId : ReminderRepository.getInstance().getRemindersIdsByEventId(changedEvent.getId())) {
            DocumentReference reminderDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.REMINDER_COLLECTION).document(reminderId);
            batch.delete(reminderDocRef);
        }

        for (Reminder reminder : reminders) {
            DocumentReference reminderDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.REMINDER_COLLECTION).document();
            Map<String, Object> reminderData = new HashMap<>();
            reminderData.put(Constants.REMINDER_EVENT_ID, changedEvent.getId());
            reminderData.put(Constants.REMINDER_MINUTE, "" + reminder.getMinute());
            reminderData.put(Constants.REMINDER_TIME, reminder.getTime());

            batch.set(reminderDocRef, reminderData);
        }

        batch.commit();
    }

    public ArrayList<String> getEventsIdsBySearchString(String searchString) {
        ArrayList<String> eventsIds = new ArrayList<>();
        for (Event e : allEvents.values()) {
            if (StringUtil.normalizeString(e.getTen()).contains(StringUtil.normalizeString(searchString)) ||
                    StringUtil.normalizeString(e.getDiaDiem()).contains(StringUtil.normalizeString(searchString)) ||
                    StringUtil.normalizeString(e.getGhiChu()).contains(StringUtil.normalizeString(searchString))) {
                eventsIds.add(e.getId());
            }
        }
        sortEventsIdsByStartDateTime(eventsIds);
        return eventsIds;
    }

//    public HashMap<String, Event> getEventsByDate(String date) {
////        Log.d("debug", "EventRepository: getting event on date: " + date);
//        HashMap<String, Event> events = new HashMap<>();
//        if (getAllEvents().size() > 0) {
//            for (String eventID : allEvents.keySet()) {
//                Event e = allEvents.get(eventID);
//                if (e != null && e.getNgayBatDau().equals(date)) {
//                    events.put(eventID, allEvents.get(eventID));
//                }
//            }
//        }
//        return events;
//    }

    public Event getEventByEventId(String id) {
        return allEvents.get(id);
    }

//    public HashMap<String, Event> getEventsThroughDate(String date) {
//        Log.d("debug", "add");
//        HashMap<String, Event> events = new HashMap<>();
//        if (getAllEvents().size() > 0) {
//            for (Event tempE : allEvents.values()) {
//                try {
//                    if (CalendarUtil.sdfDayMonthYear.parse(tempE.getNgayBatDau()).compareTo(
//                            CalendarUtil.sdfDayMonthYear.parse(date)) <= 0 &&
//                            CalendarUtil.sdfDayMonthYear.parse(tempE.getNgayKetThuc()).compareTo(
//                                    CalendarUtil.sdfDayMonthYear.parse(date)) >= 0) {
//                        events.put(tempE.getId(), tempE);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return events;
//    }

//    public HashMap<String, Event> getEventsThroughDateEdit(String date, String eventId) {
//        Log.d("debug", "edit");
//        HashMap<String, Event> events = new HashMap<>();
//        if (getAllEvents().size() > 0) {
//            for (Event tempE : allEvents.values()) {
//                if (tempE.getId().equals(eventId)) {
//                    continue;
//                }
//                try {
//                    if (CalendarUtil.sdfDayMonthYear.parse(tempE.getNgayBatDau()).compareTo(
//                            CalendarUtil.sdfDayMonthYear.parse(date)) <= 0 &&
//                            CalendarUtil.sdfDayMonthYear.parse(tempE.getNgayKetThuc()).compareTo(
//                                    CalendarUtil.sdfDayMonthYear.parse(date)) >= 0) {
//                        events.put(tempE.getId(), tempE);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return events;
//    }

    public ArrayList<Event> getEventsArrayListThroughDate(String date) {
        ArrayList<Event> events = new ArrayList<>();
        if (getAllEvents().size() > 0) {
            for (Event tempE : allEvents.values()) {
                try {
                    if ((CalendarUtil.sdfDayMonthYear.parse(tempE.getNgayBatDau()).compareTo(
                            CalendarUtil.sdfDayMonthYear.parse(date)) <= 0) &&
                            (CalendarUtil.sdfDayMonthYear.parse(tempE.getNgayKetThuc()).compareTo(
                                    CalendarUtil.sdfDayMonthYear.parse(date)) >= 0)) {
                        events.add(tempE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return events;
    }

//    public Date getEventStartDateTimeByReminder(Reminder r) {
//        Calendar calendarDate = Calendar.getInstance();
//        Calendar calendarTime = Calendar.getInstance();
//        try {
//            calendarDate.setTime(CalendarUtil.sdfDayMonthYear.parse(allEvents.get(r.getEventId()).getNgayBatDau()));
//            calendarTime.setTime(CalendarUtil.sdfTime.parse(allEvents.get(r.getEventId()).getGioBatDau()));
//
//            calendarDate.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
//            calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return calendarDate.getTime();
//    }

    //----------------------------------------------------------------------------------------------
    // CONFLICT
    //----------------------------------------------------------------------------------------------

    public void getConflictEventsIdsEdit(final long startMili, final long endMili,
                                         ArrayList<String> employeesIds, final String eventId,
                                         final MyConflictEventCallback callback) {
        ArrayList<com.google.android.gms.tasks.Task<QuerySnapshot>> taskList = new ArrayList<>();
        for (String id : employeesIds) {
            taskList.add(DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION)
                    .whereLessThanOrEqualTo(Constants.SALARY_START_MILI, endMili)
                    .whereEqualTo(Constants.SALARY_EMPLOYEE_ID, id)
                    .orderBy(Constants.SALARY_START_MILI)
                    .get());
        }

//        Log.d("debug", "here2");

        com.google.android.gms.tasks.Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(taskList);
        allTasks.addOnSuccessListener(querySnapshots -> {
            HashMap<String, ArrayList<String>> conflictMap = new HashMap<>();
//            Log.d("debug", "here3");
//            Log.d("debug", "snapshots size = " + querySnapshots.size());
            for (QuerySnapshot documentSnapshots : querySnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : documentSnapshots) {
//                    Log.d("debug", "docSnapshots size = " + documentSnapshots.size());
                    long docEndMili = (long) documentSnapshot.get(Constants.SALARY_END_MILI);
                    if (docEndMili >= startMili) {
                        String docEventId = (String) documentSnapshot.get(Constants.SALARY_EVENT_ID);
                        if (docEventId != null && !docEventId.equals(eventId)) {
                            String employeeId = (String) documentSnapshot.get(Constants.SALARY_EMPLOYEE_ID);
                            if (conflictMap.get(employeeId) == null) {
                                conflictMap.put(employeeId, new ArrayList<>());
                            }
                            ArrayList<String> arr = conflictMap.get(employeeId);
                            if (arr != null) {
                                arr.add(docEventId);
                            }
                        }
                    }

                }
            }
//            Log.d("debug", "here4");

            callback.onCallback(conflictMap);
        });
    }

    public void getConflictEventsIdsAdd(final long startMili, long endMili, final ArrayList<String> employeesIds,
                                        final MyConflictEventCallback callback) {
        ArrayList<com.google.android.gms.tasks.Task<QuerySnapshot>> taskList = new ArrayList<>();
        for (String id : employeesIds) {
            taskList.add(DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION)
                    .whereLessThanOrEqualTo(Constants.SALARY_START_MILI, endMili)
                    .whereEqualTo(Constants.SALARY_EMPLOYEE_ID, id)
                    .orderBy(Constants.SALARY_START_MILI)
                    .get());
        }

        com.google.android.gms.tasks.Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(taskList);
        allTasks.addOnSuccessListener(querySnapshots -> {
            HashMap<String, ArrayList<String>> conflictMap = new HashMap<>();
            for (QuerySnapshot documentSnapshots : querySnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : documentSnapshots) {
                    long docEndMili = (long) documentSnapshot.get(Constants.SALARY_END_MILI);
                    if (docEndMili >= startMili) {
                        String employeeId = (String) documentSnapshot.get(Constants.SALARY_EMPLOYEE_ID);
                        if (conflictMap.get(employeeId) == null) {
                            conflictMap.put(employeeId, new ArrayList<>());
                        }
                        ArrayList<String> arr = conflictMap.get(employeeId);
                        if (arr != null) {
                            arr.add((String) documentSnapshot.get(Constants.SALARY_EVENT_ID));
                        }
                    }

                }
            }
            callback.onCallback(conflictMap);
        });
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public void sortEventsByStartDateTime(ArrayList<Event> events) {
        Collections.sort(events, (e1, e2) -> {
            int compareResult = 0;
            try {
                if (!e1.getNgayBatDau().equals(e2.getNgayBatDau())) {
                    compareResult = CalendarUtil.sdfDayMonthYear.parse(e1.getNgayBatDau()).compareTo(
                            CalendarUtil.sdfDayMonthYear.parse(e2.getNgayBatDau()));
                } else {
                    compareResult = CalendarUtil.sdfTime.parse(e1.getGioBatDau()).compareTo(
                            CalendarUtil.sdfTime.parse(e2.getGioBatDau()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return compareResult;
        });
    }

    public void sortEventsIdsByStartDateTime(ArrayList<String> eventsIds) {
        Collections.sort(eventsIds, (id1, id2) -> {
            int compareResult = 0;
            Event e1 = allEvents.get(id1);
            Event e2 = allEvents.get(id2);
            if (e1 != null && e2 != null) {
                try {
                    if (!e1.getNgayBatDau().equals(e2.getNgayBatDau())) {
                        compareResult = CalendarUtil.sdfDayMonthYear.parse(e1.getNgayBatDau()).compareTo(
                                CalendarUtil.sdfDayMonthYear.parse(e2.getNgayBatDau()));
                    } else {
                        compareResult = CalendarUtil.sdfTime.parse(e1.getGioBatDau()).compareTo(
                                CalendarUtil.sdfTime.parse(e2.getGioBatDau()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return compareResult;
        });
    }

//    private interface MyEventCallback {
//        void onCallback(HashMap<String, Event> eventList);
//    }

    public interface MyConflictEventCallback {
        void onCallback(HashMap<String, ArrayList<String>> conflictMap);
    }

}
