package com.nqm.event_manager.utils;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.repositories.DefaultEmployeeRepository;
import com.nqm.event_manager.repositories.DefaultReminderRepository;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ReminderRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.repositories.TaskRepository;

public class DatabaseAccess {

    private static DatabaseAccess instance;
    private FirebaseFirestore database;
    private static long createdTime = 0;
    private static long dataLoadTime = 0;

    private DatabaseAccess() {
        createdTime = System.currentTimeMillis();
        database = FirebaseFirestore.getInstance();
    }

    static public DatabaseAccess getInstance() {
        if (instance == null) {
            instance = new DatabaseAccess();
        }
        return instance;
    }

    public FirebaseFirestore getDatabase() {
        return database;
    }

    public static void setDatabaseListener(IOnDataLoadComplete listener, Context context) {
        System.out.println("DatabaseAccess: addDatabaseListener()");
        DefaultReminderRepository.getInstance().setListener(listener);
        EmployeeRepository.getInstance().setCommonListener(listener);
        EventRepository.getInstance().setListener(listener);
        ReminderRepository.getInstance(context).setListener(listener);
        SalaryRepository.getInstance().setListener(listener);
        ScheduleRepository.getInstance().setListener(listener);
        TaskRepository.getInstance().setListener(listener);
        DefaultEmployeeRepository.getInstance().setListener(listener);
    }

    public static boolean isAllDataLoaded(Context context) {

        boolean defaultRemindersLoaded = DefaultReminderRepository.getInstance().getDefaultReminders() != null;
        boolean employeesLoaded = EmployeeRepository.getInstance().getAllEmployees() != null;
        boolean eventsLoaded = EventRepository.getInstance().getAllEvents() != null;
        boolean remindersLoaded = ReminderRepository.getInstance(context).getAllReminders() != null;
        boolean salariesLoaded = SalaryRepository.getInstance().getAllSalaries() != null;
        boolean schedulesLoaded = ScheduleRepository.getInstance().getAllSchedules() != null;
        boolean tasksLoaded = TaskRepository.getInstance().getAllTasks() != null;
        boolean defaultEmployeesLoaded = DefaultEmployeeRepository.getInstance().getDefaultEmployeeIds() != null;

        System.out.println("defaultRemindersLoaded: " + (DefaultReminderRepository.getInstance().getDefaultReminders() != null) + "\n" +
                "employeesLoaded: " + (EmployeeRepository.getInstance().getAllEmployees() != null) + "\n" +
                "eventsLoaded: " + (EventRepository.getInstance().getAllEvents() != null) + "\n" +
                "remindersLoaded: " + (ReminderRepository.getInstance(context).getAllReminders() != null) + "\n" +
                "salariesLoaded: " + (SalaryRepository.getInstance().getAllSalaries() != null) + "\n" +
                "schedulesLoaded: " + (ScheduleRepository.getInstance().getAllSchedules() != null) + "\n" +
                "tasksLoaded: " + (TaskRepository.getInstance().getAllTasks() != null) + "\n" +
                "defaultEmployeesLoaded: " + (DefaultEmployeeRepository.getInstance().getDefaultEmployeeIds() != null));

        boolean isAllDataLoaded = defaultRemindersLoaded && employeesLoaded && eventsLoaded && remindersLoaded &&
                salariesLoaded && schedulesLoaded && tasksLoaded && defaultEmployeesLoaded;

        if ((dataLoadTime == 0) && isAllDataLoaded) {
            dataLoadTime = System.currentTimeMillis() - createdTime;
            System.out.println("dataLoadTime in miliseconds = " + dataLoadTime);
        }

        return isAllDataLoaded;
    }

}
