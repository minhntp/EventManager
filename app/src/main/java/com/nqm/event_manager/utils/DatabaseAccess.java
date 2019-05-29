package com.nqm.event_manager.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.repositories.DefaultReminderRepository;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ReminderRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.repositories.TaskRepository;

public class DatabaseAccess {

    static DatabaseAccess instance;
    FirebaseFirestore database;

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

    public static void setDatabaseListener(IOnDataLoadComplete listener) {
        DefaultReminderRepository.getInstance().setListener(listener);
        EmployeeRepository.getInstance().setListener(listener);
        EventRepository.getInstance().setListener(listener);
        ReminderRepository.getInstance().setListener(listener);
        SalaryRepository.getInstance().setListener(listener);
        ScheduleRepository.getInstance().setListener(listener);
        TaskRepository.getInstance().setListener(listener);
    }

}
