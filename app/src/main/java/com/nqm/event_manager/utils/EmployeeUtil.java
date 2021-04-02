package com.nqm.event_manager.utils;

import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class EmployeeUtil {

    public static ArrayList<String> sortEmployeesIdsByName(ArrayList<String> employeesIds) {
        ArrayList<Employee> employees = new ArrayList<Employee>();
        ArrayList<String> sortedIds = new ArrayList<String>();

        for (String id : employeesIds) {
            employees.add(EmployeeRepository.getInstance().getAllEmployees().get(id));
        }

        Collections.sort(employees);

        for (Employee e : employees) {
            sortedIds.add(e.getId());
        }

        return sortedIds;
    }

    public static ArrayList<Employee> sortEmployeesByName(ArrayList<Employee> employees) {

        Collections.sort(employees);

        return employees;
    }
}
