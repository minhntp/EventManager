package com.nqm.event_manager.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.utils.DatabaseAccess;

public class SetRemindersOnBootReceiver extends BroadcastReceiver implements IOnDataLoadComplete {
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseAccess.setDatabaseListener(this, context);
    }


    @Override
    public void notifyOnLoadComplete() {

    }
}
