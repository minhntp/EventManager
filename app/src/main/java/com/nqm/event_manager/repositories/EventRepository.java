package com.nqm.event_manager.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class EventRepository {

    static EventRepository instance;
    private IOnDataLoadComplete listener;
    private boolean isLoaded;
    private HashMap<String, Event> allEvents;

    //-------------------------------------------------------------------------------------------

    private EventRepository(final IOnDataLoadComplete listener) {
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
    }

    private EventRepository() {
        allEvents = new HashMap<>();
        addListener();
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
    //-------------------------------------------------------------------------------------------

    static public EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
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
                        Log.d("debug", "event added");
                        callback.onCallback(events);
                    }
                });
    }

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
    }

    private void addListener() {
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
                        allEvents = events;
                        listener.notifyOnLoadComplete();
                    }
                });
    }

    //-------------------------------------------------------------------------------------------

    public HashMap<String, Event> getAllEvents() {
        return allEvents;
    }

    public void addEventToDatabase(Event event, ArrayList<Salary> salaries,
                                   ArrayList<Schedule> schedules, ArrayList<Reminder> reminders) {
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
            HashMap<String, String> salaryData = new HashMap<>();
            salaryData.put(Constants.SALARY_EVENT_ID, eventDocRef.getId());
            salaryData.put(Constants.SALARY_EMPLOYEE_ID, salary.getEmployeeId());
            salaryData.put(Constants.SALARY_SALARY, "" + salary.getSalary());
            salaryData.put(Constants.SALARY_PAID, Boolean.toString(salary.isPaid()));
            batch.set(salaryDocRef, salaryData);
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
            reminderData.put(Constants.REMINDER_TEXT, reminder.getText());
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

    public int getNumberOfEventsThroughDate(String date) {
        return getEventsThroughDate(date).size();
    }

    public void updateEventToDatabase(Event changedEvent, ArrayList<String> deleteEmployeesIds,
                                      ArrayList<String> addEmployeesIds, ArrayList<Schedule> addSchedules,
                                      ArrayList<Reminder> reminders) {
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
            addScheduleData.put(Constants.SCHEDULE_ORDER, Integer.toString(addSchedule.getOrder()));
            batch.set(addScheduleDocRef, addScheduleData);
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
            reminderData.put(Constants.REMINDER_TEXT, reminder.getText());
            reminderData.put(Constants.REMINDER_TIME, reminder.getTime());

            batch.set(reminderDocRef, reminderData);
        }

        batch.commit();
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

    public Date getEventStartDateTimeByReminder(Reminder r) {
        Calendar calendarDate = Calendar.getInstance();
        Calendar calendarTime = Calendar.getInstance();
        try {
            calendarDate.setTime(CalendarUtil.sdfDayMonthYear.parse(allEvents.get(r.getEventId()).getNgayBatDau()));
            calendarTime.setTime(CalendarUtil.sdfTime.parse(allEvents.get(r.getEventId()).getGioBatDau()));

            calendarDate.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
            calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendarDate.getTime();
    }

    public ArrayList<String> getConflictEventsIds(String startTime, String endTime, String employeeId,
                                                  String eventId) {
//        ArrayList<Salary> conflictSalaries = SalaryRepository.getInstance()
//                .getSalariesByStartTimeEndTimeEmployeeId(startTime, endTime, employeeId, eventId);
////        Log.d("debug", "conflict salaries size = " + conflictSalaries.size());
//        ArrayList<String> eventsIds = new ArrayList<>();
//        for (Salary s : conflictSalaries) {
//            if (!eventsIds.contains(s.getEventId())) {
//                eventsIds.add(s.getEventId());
//            }
//        }
        ArrayList<String> conflictEventsIds = new ArrayList<>();
        try {
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(startTime));
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(endTime));

            Log.d("debug", "startTime = " + CalendarUtil.sdfDayMonthYearTime.format(startCalendar.getTime())
                    + "\n" + "endTime = " + CalendarUtil.sdfDayMonthYearTime.format(endCalendar.getTime()));
            for (Event e : getAllEvents().values()) {
                Calendar tempCalendar = Calendar.getInstance();
                Calendar eventStartCalendar = Calendar.getInstance();
                Calendar eventEndCalendar = Calendar.getInstance();

                eventStartCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(e.getNgayBatDau()));
                tempCalendar.setTime(CalendarUtil.sdfTime.parse(e.getGioBatDau()));
                eventStartCalendar.set(Calendar.HOUR_OF_DAY, tempCalendar.get(Calendar.HOUR_OF_DAY));
                eventStartCalendar.set(Calendar.MINUTE, tempCalendar.get(Calendar.MINUTE));
                eventStartCalendar.set(Calendar.SECOND, 0);
                eventStartCalendar.set(Calendar.MILLISECOND, 0);

                eventEndCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(e.getNgayKetThuc()));
                tempCalendar.setTime(CalendarUtil.sdfTime.parse(e.getGioKetThuc()));
                eventEndCalendar.set(Calendar.HOUR_OF_DAY, tempCalendar.get(Calendar.HOUR_OF_DAY));
                eventEndCalendar.set(Calendar.MINUTE, tempCalendar.get(Calendar.MINUTE));
                eventEndCalendar.set(Calendar.SECOND, 0);
                eventEndCalendar.set(Calendar.MILLISECOND, 0);

                Log.d("debug", "event startTime = " + CalendarUtil.sdfDayMonthYearTime
                        .format(eventStartCalendar.getTime()) + "\nevent endTime = " +
                        CalendarUtil.sdfDayMonthYearTime.format(eventEndCalendar.getTime()));

                if ((eventStartCalendar.compareTo(startCalendar) >= 0 &&
                        eventStartCalendar.compareTo(endCalendar) <= 0) ||
                        (eventEndCalendar.compareTo(startCalendar) >= 0 &&
                                eventEndCalendar.compareTo(endCalendar) <= 0)) {
                    conflictEventsIds.add(e.getId());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return conflictEventsIds;
    }

    private interface MyEventCallback {
        void onCallback(HashMap<String, Event> eventList);
    }

    //----------------------------------------------------------------------------------------------
    public void sortEventsByStartDateTime(ArrayList<Event> events) {
        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                int compareResult = 0;
                try {
                    if (!e1.getNgayBatDau().equals(e2.getNgayBatDau())) {
                        compareResult = CalendarUtil.sdfDayMonthYear.parse(e1.getNgayBatDau()).compareTo(
                                CalendarUtil.sdfDayMonthYear.parse(e2.getNgayBatDau()));
                    } else {
                        compareResult = CalendarUtil.sdfTime.parse(e1.getGioBatDau()).compareTo(
                                CalendarUtil.sdfTime.parse(e2.getGioKetThuc()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return compareResult;
            }
        });
    }
}
