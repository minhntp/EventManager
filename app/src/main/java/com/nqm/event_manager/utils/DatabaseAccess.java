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
        DefaultReminderRepository.getInstance().setListener(listener);
        EmployeeRepository.getInstance().setListener(listener);
        EventRepository.getInstance().setListener(listener);
        ReminderRepository.getInstance(context).setListener(listener);
        SalaryRepository.getInstance().setListener(listener);
        ScheduleRepository.getInstance().setListener(listener);
        TaskRepository.getInstance().setListener(listener);
        DefaultEmployeeRepository.getInstance().setListener(listener);
    }

    public static boolean isAllDataLoaded(Context context) {
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
