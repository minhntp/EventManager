package com.nqm.event_manager.repositories;

import android.util.Log;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class EmployeeRepository {

    static EmployeeRepository instance;
    HashMap<String, Employee> allEmployees;
    IOnDataLoadComplete listener;

    private EmployeeRepository(final IOnDataLoadComplete listener) {
        this.listener = listener;
        allEmployees = new HashMap<>();
        addListener(new MyEmployeeCallback() {
            @Override
            public void onCallback(HashMap<String, Employee> employeeList) {
                if (employeeList != null) {
                    allEmployees = employeeList;
                    if (EmployeeRepository.this.listener != null) {
                        EmployeeRepository.this.listener.notifyOnLoadComplete();
                    }
                }
            }
        });
        if (allEmployees == null) {
            allEmployees = new HashMap<>();
        }
    }

    static public EmployeeRepository getInstance(IOnDataLoadComplete listener) {
        if (instance == null) {
            instance = new EmployeeRepository(listener);
        }
        return instance;
    }

    public HashMap<String, Employee> getAllEmployees() {
        return allEmployees;
    }

    private void addListener(final EmployeeRepository.MyEmployeeCallback callback) {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EMPLOYEE_COLLECTION)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("debug", "Employees listen failed.", e);
                            return;
                        }
                        HashMap<String, Employee> employees = new HashMap<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> tempHashMap = doc.getData();
                            Employee tempEmployee = new Employee(doc.getId(),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_NAME),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_SPECIALITY),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_IDENTITY),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_DAY_OF_BIRTH),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_PHONE_NUMBER),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_EMAIL));
                            employees.put(tempEmployee.getId(), tempEmployee);
                        }
                        callback.onCallback(employees);
                    }
                });
    }

    public ArrayList<String> getAllEmployeesIds() {
        ArrayList<String> allEmployeesIds = new ArrayList<>();
        for (Employee e : allEmployees.values()) {
            allEmployeesIds.add(e.getId());
        }
        return allEmployeesIds;
    }

    public ArrayList<String> getEmployeesIdsByEventId(String eventId) {
        ArrayList<String> employeesIds = new ArrayList<>();
        for (Salary s : SalaryRepository.getInstance(null).getSalariesByEventId(eventId).values()) {
            employeesIds.add(s.getEmployeeId());
        }
        return employeesIds;
    }

    public ArrayList<String> getEmployeesIdsFromSalariesIds(ArrayList<String> salariesIds) {
        ArrayList<String> employeesIds = new ArrayList<>();
        for (String salaryId : salariesIds) {
            String employeeId = SalaryRepository.getInstance(null).getAllSalaries().get(salaryId).getEmployeeId();
            if (!employeesIds.contains(employeeId)) {
                employeesIds.add(employeeId);
            }
        }

        return employeesIds;
    }

    public ArrayList<String> getEmployeesIdsBySearchString(String searchString) {
        if (searchString.isEmpty()) {
            return getAllEmployeesIds();
        }

        ArrayList<String> employeesIds = new ArrayList<>();
        for (Employee e : allEmployees.values()) {
            if (normalizeString(e.getHoTen()).contains(normalizeString(searchString)) ||
                    normalizeString(e.getChuyenMon()).contains(normalizeString(searchString))) {
                employeesIds.add(e.getId());
            }
        }
        return employeesIds;
    }

    public String normalizeString(String s) {
        try {
            String tempS = Normalizer.normalize(s, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(tempS).replaceAll("").toLowerCase().replaceAll("đ", "d");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private interface MyEmployeeCallback {
        void onCallback(HashMap<String, Employee> employeeList);
    }
}
