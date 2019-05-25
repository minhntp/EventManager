package com.nqm.event_manager.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
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
    private HashMap<String, Employee> allEmployees;
    private IOnDataLoadComplete listener;

    //------------------------------------------------------------------------------------------

    private EmployeeRepository() {
        addListener();
    }

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

    static public EmployeeRepository getInstance() {
        if (instance == null) {
            instance = new EmployeeRepository();
        }
        return instance;
    }

    static public EmployeeRepository getInstance(IOnDataLoadComplete listener) {
        if (instance == null) {
            instance = new EmployeeRepository(listener);
        }
        return instance;
    }

    //------------------------------------------------------------------------------------------

    private void addListener() {
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
                        allEmployees = employees;
                        listener.notifyOnLoadComplete();
                    }
                });
    }

    public void setListener(IOnDataLoadComplete listener) {
        this.listener = listener;
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

    //------------------------------------------------------------------------------------------

    public HashMap<String, Employee> getAllEmployees() {
        return allEmployees;
    }

    public void addEmployee(Employee employee) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        DocumentReference eventDocRef = DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EMPLOYEE_COLLECTION).document();

        HashMap<String, String> employeeData = new HashMap<>();
        employeeData.put(Constants.EMPLOYEE_NAME, employee.getHoTen());
        employeeData.put(Constants.EMPLOYEE_SPECIALITY, employee.getChuyenMon());
        employeeData.put(Constants.EMPLOYEE_DAY_OF_BIRTH, employee.getNgaySinh());
        employeeData.put(Constants.EMPLOYEE_IDENTITY, employee.getCmnd());
        employeeData.put(Constants.EMPLOYEE_PHONE_NUMBER, employee.getSdt());
        employeeData.put(Constants.EMPLOYEE_EMAIL, employee.getEmail());

        batch.set(eventDocRef, employeeData);
        batch.commit();
    }

    public void deleteEmployeeByEmployeeId(String employeeId) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();

        DocumentReference employeeDocRef = DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EMPLOYEE_COLLECTION).document(employeeId);
        batch.delete(employeeDocRef);

        for (Salary salary : SalaryRepository.getInstance(null).getAllSalaries().values()) {
            if (salary.getEmployeeId().equals(employeeId)) {
                DocumentReference salaryDocRef = DatabaseAccess.getInstance().getDatabase()
                        .collection(Constants.SALARY_COLLECTION).document(salary.getSalaryId());
                batch.delete(salaryDocRef);
            }
        }

        batch.commit();
    }

    public void updateEmployee(Employee employee) {
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
        DocumentReference employeeDocRef = DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EMPLOYEE_COLLECTION).document(employee.getId());

        HashMap<String, Object> employeeData = new HashMap<>();
        employeeData.put(Constants.EMPLOYEE_NAME, employee.getHoTen());
        employeeData.put(Constants.EMPLOYEE_SPECIALITY, employee.getChuyenMon());
        employeeData.put(Constants.EMPLOYEE_DAY_OF_BIRTH, employee.getNgaySinh());
        employeeData.put(Constants.EMPLOYEE_IDENTITY, employee.getCmnd());
        employeeData.put(Constants.EMPLOYEE_PHONE_NUMBER, employee.getSdt());
        employeeData.put(Constants.EMPLOYEE_EMAIL, employee.getEmail());

        batch.update(employeeDocRef, employeeData);

        batch.commit();
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
        for (Salary s : SalaryRepository.getInstance().getSalariesByEventId(eventId)) {
            employeesIds.add(s.getEmployeeId());
        }
        return employeesIds;
    }

    public ArrayList<String> getEmployeesIdsFromSalaries(ArrayList<Salary> salaries) {
        ArrayList<String> employeesIds = new ArrayList<>();
        for (Salary s : salaries) {
            if (!employeesIds.contains(s.getEmployeeId())) {
                employeesIds.add(s.getEmployeeId());
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
