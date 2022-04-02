package com.nqm.event_manager.repositories;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;

public class SalaryRepository {
    private static SalaryRepository instance;
    private IOnDataLoadComplete listener;
    private HashMap<String, Salary> allSalaries;

    //------------------------------------------------------------------------------------

    private SalaryRepository() {
//        allSalaries = new HashMap<>();
        addListener();
    }

    private SalaryRepository(final IOnDataLoadComplete listener) {
        this.listener = listener;
        addListener(salaryList -> {
            if (salaryList != null) {
                allSalaries = salaryList;
                if (SalaryRepository.this.listener != null) {
                    SalaryRepository.this.listener.notifyOnLoadComplete();
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
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
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
                                    Boolean.parseBoolean((String) data.get(Constants.SALARY_PAID)),
                                    (long) data.get(Constants.SALARY_START_MILI),
                                    (long) data.get(Constants.SALARY_END_MILI));
                            salaries.put(tempSalary.getSalaryId(), tempSalary);
                        }
                    }
                    allSalaries = salaries;
                    listener.notifyOnLoadComplete();
                });
    }

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
    }

    private void addListener(final SalaryRepository.MySalaryCallback callback) {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SALARY_COLLECTION)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
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
                                    Boolean.parseBoolean((String) data.get(Constants.SALARY_PAID)),
                                    (long) data.get(Constants.SALARY_START_MILI),
                                    (long) data.get(Constants.SALARY_END_MILI));
                            salaryList.put(tempSalary.getSalaryId(), tempSalary);
                        }
                    }
                    callback.onCallback(salaryList);
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

    public void updateSalaries(List<Salary> salaries) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
//        Log.d("debug", "salaries size = " + salaries.size());
        for (int i = 0; i < salaries.size(); i++) {
            DocumentReference salaryDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION).document(salaries.get(i).getSalaryId());

            Map<String, Object> salaryData = new HashMap<>();
            salaryData.put(Constants.SALARY_SALARY, "" + salaries.get(i).getSalary());
            salaryData.put(Constants.SALARY_PAID, Boolean.toString(salaries.get(i).isPaid()));
//            Log.d("debug", "amount = " + salaries.get(i).getSalary());
            batch.update(salaryDocRef, salaryData);
        }

        batch.commit();
    }

    public void revertToNotPaid(String salaryId) {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.SALARY_COLLECTION)
                .document(salaryId)
                .update(Constants.SALARY_PAID, "false");
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
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã xóa bản ghi lương", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Xóa bản ghi lương thất bại", Toast.LENGTH_SHORT).show());
    }

    public boolean isSalaryPaid(String employeeId, String eventId) {
        boolean isPaid = false;
        for (Salary s : allSalaries.values()) {
//            Log.d("debug", "looping to find out if salary is paid");
            if (s.getEmployeeId().equals(employeeId) && s.getEventId().equals(eventId)) {
                isPaid = s.isPaid();
                break;
            }
        }
        return isPaid;
    }

//    public void isSalaryPaid(String employeeId, String eventId, final MyIsPaidSalaryCallback callback) {
//        DatabaseAccess.getInstance().getDatabase()
//                .collection(Constants.SALARY_COLLECTION)
//                .whereEqualTo(Constants.SALARY_EMPLOYEE_ID, employeeId)
//                .whereEqualTo(Constants.SALARY_EVENT_ID, eventId)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        if (task.getResult().size() == 0) {
//                            callback.onCallback(false);
//                            return;
//                        } else {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Map<String, Object> data = document.getData();
//                                boolean isPaid = Boolean.parseBoolean((String) data.get(Constants.SALARY_PAID));
//                                callback.onCallback(isPaid);
//                            }
//                        }
//                    } else {
//                        Log.d("debug", "is paid salary query task failed");
//                        callback.onCallback(false);
//                    }
//                });
//    }

//    long startMili = 0, endMili = 0;

    //CALCULATE SALARIES FOR ALL EMPLOYEES
//    public void getSalariesByStartDateEndDate(String startDate, String endDate,
//                                              final MySalaryQueryCallback callback) {
//        try {
//            final Calendar calendar = Calendar.getInstance();
//            calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(startDate));
//            startMili = calendar.getTimeInMillis();
//
//            calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(endDate));
//            endMili = calendar.getTimeInMillis();
//
//            DatabaseAccess.getInstance().getDatabase()
//                    .collection(Constants.SALARY_COLLECTION)
//                    .whereLessThanOrEqualTo(Constants.SALARY_START_MILI, endMili)
//                    .orderBy(Constants.SALARY_START_MILI)
//                    .get()
//                    .addOnSuccessListener(queryDocumentSnapshots -> {
//                        ArrayList<String> salariesIds = new ArrayList<>();
//                        ArrayList<String> eventsIds = new ArrayList<>();
//                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                            long docEndMili = (long) documentSnapshot.get(Constants.SALARY_END_MILI);
//                            if (docEndMili >= startMili) {
//                                salariesIds.add(documentSnapshot.getId());
//                                String eventId = (String) documentSnapshot.get(Constants.SALARY_EVENT_ID);
//                                if(!eventsIds.contains(eventId)) {
//                                    eventsIds.add(eventId);
//                                }
//                            }
//
//                        }
//                        callback.onCallback(salariesIds, eventsIds);
//                    });
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    public Map<String, Salary> getSalariesMapByStartDateEndDate(String startDateString, String endDateString) {

        Map<String, Salary> salaries = new HashMap<>();

        for (Salary s : allSalaries.values()) {
            if (isBetween(s, startDateString, endDateString)) {
                salaries.put(s.getSalaryId(), s);
            }
        }
        return salaries;
    }

    public List<Salary> getSalariesListByStartDateEndDate(String startDateString, String endDateString) {

        List<Salary> salaries = new ArrayList<>();

        for (Salary s : allSalaries.values()) {
            if (isBetween(s, startDateString, endDateString)) {
                salaries.add(s);
            }
        }
        return salaries;
    }

    private boolean isBetween(Salary s, String startDateString, String endDateString) {
        try {
            String salaryStartDate = EventRepository.getInstance().getAllEvents().get(s.getEventId()).getNgayBatDau();
            String salaryEndDate = EventRepository.getInstance().getAllEvents().get(s.getEventId()).getNgayKetThuc();

            Date startDate = CalendarUtil.sdfDayMonthYear.parse(startDateString);
            Date endDate = CalendarUtil.sdfDayMonthYear.parse(endDateString);
            Date thisSalaryStart = CalendarUtil.sdfDayMonthYear.parse(salaryStartDate);
            Date thisSalaryEnd = CalendarUtil.sdfDayMonthYear.parse(salaryEndDate);
            if ((thisSalaryStart.compareTo(endDate) <= 0 && thisSalaryEnd.compareTo(startDate) >= 0)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //CALCULATE SALARIES FOR ONE EMPLOYEE
    public List<Salary> getSalariesListByStartDateAndEndDateAndEmployeeId(String startDateString,
                                                                          String endDateString,
                                                                          String employeeId) {
        List<Salary> salaries = new ArrayList<>();
        for (Salary s : allSalaries.values()) {
            if (s.getEmployeeId().equals(employeeId) && isBetween(s, startDateString, endDateString)) {
                salaries.add(s);
            }
        }
        return salaries;
    }

    public Map<String, Salary> getSalariesMapByStartDateAndEndDateAndEmployeeId(String startDate,
                                                                                String endDate,
                                                                                String employeeId) {
        Map<String, Salary> salaries = new HashMap<>();
        for (Salary s : allSalaries.values()) {
            if (s.getEmployeeId().equals(employeeId)) {
                try {
                    Date start = CalendarUtil.sdfDayMonthYear.parse(startDate);
                    Date currentStart = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance()
                            .getEventByEventId(s.getEventId()).getNgayBatDau());
                    Date end = CalendarUtil.sdfDayMonthYear.parse(endDate);
                    Date currentEnd = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance()
                            .getEventByEventId(s.getEventId()).getNgayKetThuc());
                    if ((start.compareTo(currentStart) <= 0 && currentStart.compareTo(end) <= 0) ||
                            start.compareTo(currentEnd) <= 0 && currentEnd.compareTo(end) <= 0) {
                        salaries.put(s.getSalaryId(), s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return salaries;
    }

    private interface MySalaryCallback {
        void onCallback(HashMap<String, Salary> salaryList);
    }

    public interface MySalaryQueryCallback {
        void onCallback(ArrayList<String> salariesIds, ArrayList<String> eventIds);
    }

    public interface MyIsPaidSalaryCallback {
        void onCallback(boolean isPaid);
    }

    //----------------------------------------------------------------------------------------------
    public void sortSalariesListByEventStartDate(List<Salary> salaries) {
        Collections.sort(salaries, (s1, s2) -> {
            Date d1 = Calendar.getInstance().getTime();
            Date d2 = Calendar.getInstance().getTime();
            try {
                d1 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance().getAllEvents()
                        .get(s1.getEventId()).getNgayBatDau());
                d2 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance().getAllEvents()
                        .get(s2.getEventId()).getNgayBatDau());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return d1.compareTo(d2);
        });
    }

}
