package com.nqm.event_manager.application;

import android.app.Application;
import android.content.Context;

public class EventManager extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return EventManager.context;
    }
}
