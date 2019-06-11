package com.nqm.event_manager.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

public class SplashActivity2 extends AppCompatActivity implements IOnDataLoadComplete {

    String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        eventId = getIntent().getStringExtra(Constants.INTENT_EVENT_ID);
        if (DatabaseAccess.isAllDataLoaded()) {
            Intent viewEventIntent = new Intent(this, ViewEventActivity.class);
            viewEventIntent.putExtra(Constants.INTENT_EVENT_ID, eventId);
            startActivity(viewEventIntent);
            finish();
        } else {
            DatabaseAccess.setDatabaseListener(this);
        }
    }

    @Override
    public void notifyOnLoadComplete() {
        if (DatabaseAccess.isAllDataLoaded()) {
            Intent viewEventIntent = new Intent(this, ViewEventActivity.class);
            viewEventIntent.putExtra(Constants.INTENT_EVENT_ID, eventId);
            startActivity(viewEventIntent);
            finish();
        }
    }
}
