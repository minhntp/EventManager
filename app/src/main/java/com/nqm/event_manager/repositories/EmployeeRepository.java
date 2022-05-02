package com.nqm.event_manager.repositories;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnEmployessLoadComplete;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;
import com.nqm.event_manager.utils.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeRepository {

    static EmployeeRepository instance;
    private HashMap<String, Employee> allEmployees;
    private ArrayList<String> specialities;
    private IOnEmployessLoadComplete ownListener;
    private IOnDataLoadComplete commonListener;

    //------------------------------------------------------------------------------------------

    private EmployeeRepository() {
        addDatabaseSnapshotListener();
    }

    static public EmployeeRepository getInstance() {
        if (instance == null) {
            instance = new EmployeeRepository();
        }
        return instance;
    }

    //------------------------------------------------------------------------------------------

    private void addDatabaseSnapshotListener() {
        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EMPLOYEE_COLLECTION)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("debug", "Employees listen failed.", e);
                        return;
                    }
                    HashMap<String, Employee> employees = new HashMap<>();
                    ArrayList<String> specialities = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> tempHashMap = doc.getData();
                            Employee tempEmployee = new Employee(doc.getId(),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_FULLNAME),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_SPECIALITY),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_IDENTITY),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_DAY_OF_BIRTH),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_PHONE_NUMBER),
                                    (String) tempHashMap.get(Constants.EMPLOYEE_EMAIL));
                            employees.put(tempEmployee.getId(), tempEmployee);
                            if(!specialities.contains(tempEmployee.getChuyenMon())) {
                                specialities.add(tempEmployee.getChuyenMon());
                            }
                        }
                        allEmployees = employees;
                        this.specialities = specialities;
                        if (commonListener != null) {
                            commonListener.notifyOnLoadComplete();
                        }
                        if (ownListener != null) {
                            ownListener.notifyOnEmployeesLoadComplete();
                        }
                    }
                });
    }

    public void setCommonListener(IOnDataLoadComplete commonListener) {
        this.commonListener = commonListener;
    }

    public void setOwnListener(IOnEmployessLoadComplete ownListener) {
        this.ownListener = ownListener;
    }

    public ArrayList<String> getSpecialities() {
        return specialities;
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
        employeeData.put(Constants.EMPLOYEE_FULLNAME, employee.getHoTen());
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
        employeeData.put(Constants.EMPLOYEE_FULLNAME, employee.getHoTen());
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
            if (StringUtil.normalizeString(e.getHoTen()).contains(StringUtil.normalizeString(searchString)) ||
                    StringUtil.normalizeString(e.getChuyenMon()).contains(StringUtil.normalizeString(searchString))) {
                employeesIds.add(e.getId());
            }
        }
        return employeesIds;
    }

    public List<Employee> getEmployeesBySearchString(String searchString) {
        if (searchString.isEmpty()) {
            return (new ArrayList<>(getAllEmployees().values()));
        }

        List<Employee> employees = new ArrayList<>();
        for (Employee e : allEmployees.values()) {
            if (StringUtil.normalizeString(e.getHoTen()).contains(StringUtil.normalizeString(searchString)) ||
                    StringUtil.normalizeString(e.getChuyenMon()).contains(StringUtil.normalizeString(searchString))) {
                employees.add(e);
            }
        }
        return employees;
    }

//----------------------------------------------------------------------------------------------

}
