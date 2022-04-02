package com.nqm.event_manager.utils;

import android.util.Log;

import com.google.common.collect.Lists;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EmployeeUtil {

    public static void sortEmployeesIdsByNameNew(ArrayList<String> employeesIds) {
//        System.out.println( "sorted employees by name ");
//        System.out.println( "before:");
//        for (String id : employeesIds) {
//            System.out.println( EmployeeRepository.getInstance().getAllEmployees().get(id).getHoTen());
//        }

        employeesIds.sort((id1, id2) ->
                EmployeeRepository.getInstance().getAllEmployees().get(id1).compareTo(
                        EmployeeRepository.getInstance().getAllEmployees().get(id2)
                ));
//        System.out.println( "after:");
//        for (String id : employeesIds) {
//            System.out.println( EmployeeRepository.getInstance().getAllEmployees().get(id).getHoTen());
//        }

    }

//    public static ArrayList<String> sortEmployeesIdsByName(ArrayList<String> employeesIds) {
//        ArrayList<Employee> employees = new ArrayList<Employee>();
//        ArrayList<String> sortedIds = new ArrayList<String>();
//
//        for (String id : employeesIds) {
//            employees.add(EmployeeRepository.getInstance().getAllEmployees().get(id));
//        }
//
//        Collections.sort(employees);
//
//        for (Employee e : employees) {
//            sortedIds.add(e.getId());
//        }
//
//        return sortedIds;
//    }

    public static void sortEmployeesByName(ArrayList<Employee> employees) {
        Collections.sort(employees);
    }

    public static void sortSalariesByEmployeesNames(ArrayList<Salary> salaries) {
        salaries.sort((s1, s2) -> {
            Employee e1 = EmployeeRepository.getInstance().getAllEmployees().get(s1.getEmployeeId());
            Employee e2 = EmployeeRepository.getInstance().getAllEmployees().get(s2.getEmployeeId());
            return e1.compareTo(e2);
        });
    }
}
