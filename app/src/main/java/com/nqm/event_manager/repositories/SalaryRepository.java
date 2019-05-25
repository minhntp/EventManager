package com.nqm.event_manager.repositories;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
        for (HashMap.Entry<String, Salary> entry : allSalaries.entrySet()) {
            if (entry.getValue().getEventId().equals(eventId) &&
                    entry.getValue().getEmployeeId().equals(employeeId)) {
                return entry.getKey();
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

    private interface MySalaryCallback {
        void onCallback(HashMap<String, Salary> salaryList);
    }
}
