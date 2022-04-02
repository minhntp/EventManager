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

    private DatabaseAccess() {
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
        System.out.println( "setDatabaseListener: ");
        DefaultReminderRepository.getInstance().addListener(listener);
        EmployeeRepository.getInstance().addListener(listener);
        EventRepository.getInstance().addListener(listener);
        ReminderRepository.getInstance(context).addListener(listener);
        SalaryRepository.getInstance().addListener(listener);
        ScheduleRepository.getInstance().addListener(listener);
        TaskRepository.getInstance().addListener(listener);
        DefaultEmployeeRepository.getInstance().addDatabaseSnapshotListener(listener);
    }

    public static boolean isAllDataLoaded(Context context) {
        System.out.println( "isAllDataLoaded: ");
        System.out.println( "DefaultReminderRepository.getInstance().getDefaultReminders() != null: " + (DefaultReminderRepository.getInstance().getDefaultReminders() != null));
        System.out.println( "EmployeeRepository.getInstance().getAllEmployees() != null: " + (EmployeeRepository.getInstance().getAllEmployees() != null));
        System.out.println( "ReminderRepository.getInstance(context).getAllReminders() != null: " + (ReminderRepository.getInstance(context).getAllReminders() != null));
        System.out.println( "SalaryRepository.getInstance().getAllSalaries() != null: " + (SalaryRepository.getInstance().getAllSalaries() != null));
        System.out.println( "ScheduleRepository.getInstance().getAllSchedules() != null: " + (ScheduleRepository.getInstance().getAllSchedules() != null));
        System.out.println( "TaskRepository.getInstance().getAllTasks() != null: " + (TaskRepository.getInstance().getAllTasks() != null));
        System.out.println( "DefaultReminderRepository.getInstance().getDefaultReminders() != null: " + (DefaultReminderRepository.getInstance().getDefaultReminders() != null));
        System.out.println( "DefaultEmployeeRepository.getInstance().getDefaultEmployeeIds() != null: " + (DefaultEmployeeRepository.getInstance().getDefaultEmployeeIds() != null));


        return ((DefaultReminderRepository.getInstance().getDefaultReminders() != null) &&
                (EmployeeRepository.getInstance().getAllEmployees() != null) &&
                (EventRepository.getInstance().getAllEvents() != null) &&
//                (EventRepository.getInstance().getNumberOfEventsMap() != null) &&
                (ReminderRepository.getInstance(context).getAllReminders() != null) &&
                (SalaryRepository.getInstance().getAllSalaries() != null) &&
                (ScheduleRepository.getInstance().getAllSchedules() != null) &&
                (TaskRepository.getInstance().getAllTasks() != null)) &&
                (DefaultEmployeeRepository.getInstance().getDefaultEmployeeIds() != null);
    }

}
