package com.nqm.event_manager.repositories;

import android.content.Context;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

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

public class SalaryRepository {
    static SalaryRepository instance;
    private IOnDataLoadComplete listener;
    private HashMap<String, Salary> allSalaries;

    //------------------------------------------------------------------------------------

    private SalaryRepository() {
        allSalaries = new HashMap<>();
        addListener();
    }

    private SalaryRepository(final IOnDataLoadComplete listener) {
        this.listener = listener;
        addListener(new SalaryRepository.MySalaryCallback() {
            @Override
            public void onCallback(HashMap<String, Salary> salaryList) {
                if (salaryList != null) {
                    allSalaries = salaryList;
                    if (SalaryRepository.this.listener != null) {
                        SalaryRepository.this.listener.notifyOnLoadComplete();
                    }
                }
            }
        });
        if (allSalaries == null) {
            allSalaries = new HashMap<>();
        }
    }

    static public SalaryRepository getInstance() {
        if (instance == null) {
            instance = new SalaryRepository();
        }
        return instance;
    }

    static public SalaryRepository getInstance(IOnDataLoadComplete listener) {
        if (instance == null) {
            instance = new SalaryRepository(listener);
        }
        return instance;
    }

    //------------------------------------------------------------------------------------

    private void addListener() {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SALARY_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("debug", "Salary collection listen failed.", e);
                            return;
                        }
                        HashMap<String, Salary> salaries = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                Map<String, Object> data = doc.getData();
                                int salary;
                                salary = Integer.parseInt((String) data.get(Constants.SALARY_SALARY));
                                Salary tempSalary = new Salary(doc.getId(),
                                        (String) data.get(Constants.SALARY_EVENT_ID),
                                        (String) data.get(Constants.SALARY_EMPLOYEE_ID),
                                        salary,
                                        Boolean.parseBoolean((String) data.get(Constants.SALARY_PAID)));
                                salaries.put(tempSalary.getSalaryId(), tempSalary);
                            }
                        }
                        allSalaries = salaries;
                        listener.notifyOnLoadComplete();
                    }
                });
    }

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
    }

    private void addListener(final SalaryRepository.MySalaryCallback callback) {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SALARY_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("debug", "Salary collection listen failed.", e);
                            return;
                        }
                        HashMap<String, Salary> salaryList = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (queryDocumentSnapshots.size() > 0) {
                                Map<String, Object> data = doc.getData();
                                int salary;
                                salary = Integer.parseInt((String) data.get(Constants.SALARY_SALARY));
                                Salary tempSalary = new Salary(doc.getId(),
                                        (String) data.get(Constants.SALARY_EVENT_ID),
                                        (String) data.get(Constants.SALARY_EMPLOYEE_ID),
                                        salary,
                                        Boolean.parseBoolean((String) data.get(Constants.SALARY_PAID)));
                                salaryList.put(tempSalary.getSalaryId(), tempSalary);
                            }
                        }
                        callback.onCallback(salaryList);
                    }
                });
    }

    //------------------------------------------------------------------------------------

    public HashMap<String, Salary> getAllSalaries() {
        return allSalaries;
    }

    public void addSalariesToDatabase(final ArrayList<Salary> salaries) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
        for (Salary salary : salaries) {
            DocumentReference salaryDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION).document();
            Map<String, Object> salaryData = new HashMap<>();
            salaryData.put(Constants.SALARY_EVENT_ID, salary.getEventId());
            salaryData.put(Constants.SALARY_EMPLOYEE_ID, salary.getEmployeeId());
            salaryData.put(Constants.SALARY_SALARY, "" + salary.getSalary());
            salaryData.put(Constants.SALARY_PAID, Boolean.toString(salary.isPaid()));

            batch.set(salaryDocRef, salaryData);
        }

        batch.commit();
    }

    public void updateSalaries(ArrayList<Salary> salaries) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
        Log.d("debug", "salaries size = " + salaries.size());
        for (int i = 0; i < salaries.size(); i++) {
            DocumentReference salaryDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION).document(salaries.get(i).getSalaryId());

            Map<String, Object> salaryData = new HashMap<>();
            salaryData.put(Constants.SALARY_SALARY, "" + salaries.get(i).getSalary());
            salaryData.put(Constants.SALARY_PAID, Boolean.toString(salaries.get(i).isPaid()));
            Log.d("debug", "amount = " + salaries.get(i).getSalary());
            batch.update(salaryDocRef, salaryData);
        }

        batch.commit();
    }

    public String getSalaryIdByEventIdAndEmployeeId(String eventId, String employeeId) {
        for (Salary s : allSalaries.values()) {
            if (s.getEventId().equals(eventId) &&
                    s.getEmployeeId().equals(employeeId)) {
                return s.getSalaryId();
            }
        }
        return "";
    }

    public ArrayList<Salary> getSalariesByEventId(String eventId) {
        ArrayList<Salary> salaries = new ArrayList<>();
        for (Salary s : allSalaries.values()) {
            if (s.getEventId().equals(eventId)) {
                salaries.add(s);
            }
        }
        return salaries;
    }

    public void deleteSalary(String salaryId, final Context context) {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SALARY_COLLECTION)
                .document(salaryId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Đã xóa bản ghi lương", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Xóa bản ghi lương thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //CALCULATE SALARIES FOR ALL EMPLOYEES
    public ArrayList<Salary> getSalariesByStartDateAndEndDate(String startDate, String endDate) {
        ArrayList<Salary> salaries = new ArrayList<>();
        for (Salary s : allSalaries.values()) {
            try {
                String thisSalaryStartDate = EventRepository.getInstance().getAllEvents().get(s.getEventId()).getNgayBatDau();
                String thisSalaryEndDate = EventRepository.getInstance().getAllEvents().get(s.getEventId()).getNgayKetThuc();

                Date start = CalendarUtil.sdfDayMonthYear.parse(startDate);
                Date end = CalendarUtil.sdfDayMonthYear.parse(endDate);
                Date thisSalaryStart = CalendarUtil.sdfDayMonthYear.parse(thisSalaryStartDate);
                Date thisSalaryEnd = CalendarUtil.sdfDayMonthYear.parse(thisSalaryEndDate);
                if ((thisSalaryStart.compareTo(start) >= 0 && thisSalaryStart.compareTo(end) <= 0) ||
                        (thisSalaryEnd.compareTo(start) >= 0 && thisSalaryEnd.compareTo(end) <= 0)) {
                    salaries.add(s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return salaries;
    }

    //CALCULATE SALARIES FOR ONE EMPLOYEE
    public ArrayList<Salary> getSalariesByStartDateAndEndDateAndEmployeeId(String startDate,
                                                                           String endDate,
                                                                           String employeeId) {
        ArrayList<Salary> salaries = new ArrayList<>();
        for (Salary s : allSalaries.values()) {
            if (s.getEmployeeId().equals(employeeId)) {
                try {
                    Date start = CalendarUtil.sdfDayMonthYear.parse(startDate);
                    Date currentStart = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getEventByEventId(s.getEventId()).getNgayBatDau());
                    Date end = CalendarUtil.sdfDayMonthYear.parse(endDate);
                    Date currentEnd = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getEventByEventId(s.getEventId()).getNgayKetThuc());
                    if ((start.compareTo(currentStart) <= 0 && currentStart.compareTo(end) <= 0) ||
                            start.compareTo(currentEnd) <= 0 && currentEnd.compareTo(end) <= 0) {
                        salaries.add(s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return salaries;
    }

    //SEARCH FOR CONFLICT: RESULT salaries.size() > 0 -> CONFLICT
    public ArrayList<Salary> getSalariesByStartTimeEndTimeEmployeeId(String startTime, String endTime,
                                                                     String employeeId, String eventId) {
        ArrayList<Salary> salaries = new ArrayList<>();
        try {
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(startTime));
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(endTime));

            Calendar tempCalendar = Calendar.getInstance();

            for (Salary s : allSalaries.values()) {
                if (s.getEmployeeId().equals(employeeId) && !s.getEventId().equals(eventId)) {
                    Log.d("debug", "condition met to compare time");

//                    Event e = EventRepository.getInstance().getEventByEventId(s.getEventId());
//                    Calendar salaryStartCalendar = Calendar.getInstance();
//
//                    salaryStartCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(e.getNgayBatDau()));
//                    tempCalendar.setTime(CalendarUtil.sdfTime.parse(e.getGioBatDau()));
//                    salaryStartCalendar.set(Calendar.HOUR_OF_DAY, tempCalendar.get(Calendar.HOUR_OF_DAY));
//                    salaryStartCalendar.set(Calendar.MINUTE, tempCalendar.get(Calendar.MINUTE));
//                    salaryStartCalendar.set(Calendar.SECOND, 0);
//                    salaryStartCalendar.set(Calendar.MILLISECOND, 0);
//
//                    Calendar salaryEndCalendar = Calendar.getInstance();
//                    salaryEndCalendar.setTime(CalendarUtil.sdfDayMonthYearTime.parse(e.getNgayKetThuc()));
//                    tempCalendar.setTime(CalendarUtil.sdfTime.parse(e.getGioKetThuc()));
//                    salaryEndCalendar.set(Calendar.HOUR_OF_DAY, tempCalendar.get(Calendar.HOUR_OF_DAY));
//                    salaryEndCalendar.set(Calendar.MINUTE, tempCalendar.get(Calendar.MINUTE));
//                    salaryEndCalendar.set(Calendar.SECOND, 0);
//                    salaryEndCalendar.set(Calendar.MILLISECOND, 0);
//
//                    if ((salaryStartCalendar.compareTo(startCalendar) >= 0 &&
//                            salaryStartCalendar.compareTo(endCalendar) <= 0) ||
//                            (salaryEndCalendar.compareTo(startCalendar) >= 0 &&
//                                    salaryEndCalendar.compareTo(endCalendar) <= 0)) {
//                        salaries.add(s);
//                    }
//                    Log.d("debug", "event startTime = " + CalendarUtil.sdfDayMonthYearTime
//                            .format(salaryStartCalendar.getTime()) + "\nevent endTime = " +
//                            CalendarUtil.sdfDayMonthYearTime.format(salaryEndCalendar.getTime()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("debug", "exception");
        }
        return salaries;
    }

    private interface MySalaryCallback {
        void onCallback(HashMap<String, Salary> salaryList);
    }

    //----------------------------------------------------------------------------------------------
    public static void sortSalariesByEventStartDate(ArrayList<Salary> salaries) {
        Collections.sort(salaries, new Comparator<Salary>() {
            @Override
            public int compare(Salary s1, Salary s2) {
                Date d1 = Calendar.getInstance().getTime();
                Date d2 = Calendar.getInstance().getTime();
                try {
                    d1 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getAllEvents().get(s1.getEventId()).getNgayBatDau());
                    d2 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getAllEvents().get(s2.getEventId()).getNgayBatDau());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return d1.compareTo(d2);
            }
        });
    }
}
