package com.nqm.event_manager.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.utils.DatabaseAccess;

public class SplashActivity extends AppCompatActivity implements IOnDataLoadComplete {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (DatabaseAccess.isAllDataLoaded()) {
            startActivity(new Intent(this, RootActivity.class));
            finish();
        } else {
            DatabaseAccess.setDatabaseListener(this);
        }
    }

    @Override
    public void notifyOnLoadComplete() {
        if (DatabaseAccess.isAllDataLoaded()) {
            startActivity(new Intent(this, RootActivity.class));
            finish();
        }
    }
}
