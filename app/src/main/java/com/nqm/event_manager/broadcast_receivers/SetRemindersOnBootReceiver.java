package com.nqm.event_manager.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.firestore.DocumentReference;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.repositories.ReminderRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SetRemindersOnBootReceiver extends BroadcastReceiver implements IOnDataLoadComplete {
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseAccess.setDatabaseListener(this, context);
    }

    @Override
    public void notifyOnLoadCompleteWithContext(Context context) {

    }

    @Override
    public void notifyOnLoadComplete() {

    }
}
