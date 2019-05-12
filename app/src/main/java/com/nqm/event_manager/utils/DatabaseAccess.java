package com.nqm.event_manager.utils;

import com.google.firebase.firestore.FirebaseFirestore;

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

}
