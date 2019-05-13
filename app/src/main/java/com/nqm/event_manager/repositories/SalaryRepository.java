package com.nqm.event_manager.repositories;

import android.content.Context;
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
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class SalaryRepository {
    static SalaryRepository instance;
    IOnDataLoadComplete listener;
    private HashMap<String, Salary> allSalaries;

    private SalaryRepository(final IOnDataLoadComplete listener) {
//        allEvents = new ArrayList<>();
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

    static public SalaryRepository getInstance(IOnDataLoadComplete listener) {
        if (instance == null) {
            instance = new SalaryRepository(listener);
        }
        return instance;
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

    public HashMap<String, Salary> getAllSalaries() {
        return allSalaries;
    }

    public void addSalariesToDatabase(final ArrayList<Salary> salaries, final MyAddSalariesCallback callback) {
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
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callback.onCallback(true);
            }
        });
    }

    public void updateSalaries(ArrayList<String> salariesIds, ArrayList<Integer> salariesAmounts,
                               ArrayList<Boolean> salariesPaidStatus,
                               final MyUpdateSalariesCallback callback) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
        for (int i = 0; i < salariesIds.size(); i++) {
            DocumentReference salaryDocRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION).document(salariesIds.get(i));

            Map<String, Object> salaryData = new HashMap<>();
            salaryData.put(Constants.SALARY_SALARY, "" + salariesAmounts.get(i));
            salaryData.put(Constants.SALARY_PAID, Boolean.toString(salariesPaidStatus.get(i)));

            batch.update(salaryDocRef, salaryData);
        }
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callback.onCallback(true);
            }
        });
    }

    public HashMap<String, Salary> getSalariesByEventId(String eventId) {
        HashMap<String, Salary> salaries = new HashMap<>();
        for (HashMap.Entry<String, Salary> entry : allSalaries.entrySet()) {
            if (entry.getValue().getEventId().equals(eventId)) {
                salaries.put(entry.getKey(), entry.getValue());
            }
        }
        return salaries;
    }

    public String getSalaryIdByEventIdAndEmployeeId(String eventId, String employeeId) {
        for (HashMap.Entry<String, Salary> entry : allSalaries.entrySet()) {
            if (entry.getValue().getEventId().equals(eventId) &&
                    entry.getValue().getEmployeeId().equals(employeeId)) {
                return entry.getKey();
            }
        }
        return "";
    }

    public ArrayList<String> getSalariesIdsByEventId(String eventId) {
        ArrayList<String> salariesIds = new ArrayList<>();
        for (HashMap.Entry<String, Salary> entry : allSalaries.entrySet()) {
            if (entry.getValue().getEventId().equals(eventId)) {
                salariesIds.add(entry.getKey());
            }
        }
        return salariesIds;
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

    public ArrayList<String> getSalariesIdsByStartDateAndEndDate(String startDate, String endDate) {
        ArrayList<String> salariesIds = new ArrayList<>();
        for (Salary s : allSalaries.values()) {
            try {
                Date start = CalendarUtil.sdfDayMonthYear.parse(startDate);
                Date currentStart = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getEventByEventId(s.getEventId()).getNgayBatDau());
                Date end = CalendarUtil.sdfDayMonthYear.parse(endDate);
                Date currentEnd = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getEventByEventId(s.getEventId()).getNgayKetThuc());
                if ((start.compareTo(currentStart) <= 0 && currentStart.compareTo(end) <= 0) ||
                        start.compareTo(currentEnd) <= 0 && currentEnd.compareTo(end) <= 0) {
                    salariesIds.add(s.getSalaryId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return salariesIds;
    }

    public ArrayList<String> getSalariesIdsByStartDateAndEndDateAndEmployeeId(String startDate,
                                                                              String endDate,
                                                                              String employeeId) {
        ArrayList<String> salariesIds = new ArrayList<>();
        for (Salary s : allSalaries.values()) {
            if(s.getEmployeeId().equals(employeeId)) {
                try {
                    Date start = CalendarUtil.sdfDayMonthYear.parse(startDate);
                    Date currentStart = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getEventByEventId(s.getEventId()).getNgayBatDau());
                    Date end = CalendarUtil.sdfDayMonthYear.parse(endDate);
                    Date currentEnd = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getEventByEventId(s.getEventId()).getNgayKetThuc());
                    if ((start.compareTo(currentStart) <= 0 && currentStart.compareTo(end) <= 0) ||
                            start.compareTo(currentEnd) <= 0 && currentEnd.compareTo(end) <= 0) {
                        salariesIds.add(s.getSalaryId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return salariesIds;
    }

    private interface MySalaryCallback {
        void onCallback(HashMap<String, Salary> salaryList);
    }

    public interface MyAddSalariesCallback {
        void onCallback(Boolean addSalariesSucceed);
    }

    public interface MyUpdateSalariesCallback {
        void onCallback(boolean updateSucceed);
    }
}
