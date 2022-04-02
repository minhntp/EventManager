package com.nqm.event_manager.activities;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

public class SplashActivity2 extends BaseActivity implements IOnDataLoadComplete {

    String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d("mytag", "Splash 2 onCreate: ");
        eventId = getIntent().getStringExtra(Constants.INTENT_EVENT_ID);
        if (DatabaseAccess.isAllDataLoaded(getApplicationContext())) {
            Intent viewEventIntent = new Intent(this, ViewEventActivity.class);
            viewEventIntent.putExtra(Constants.INTENT_EVENT_ID, eventId);
            startActivity(viewEventIntent);
            finish();
        } else {
            DatabaseAccess.setDatabaseListener(this, getApplicationContext());
        }
    }

    @Override
    public void notifyOnLoadCompleteWithContext(Context context) {
        Toast.makeText(context, "SplashActivity2: wrong notifyOnLoadComplete()",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyOnLoadComplete() {
        if (DatabaseAccess.isAllDataLoaded(getApplicationContext())) {
            Intent viewEventIntent = new Intent(this, ViewEventActivity.class);
            viewEventIntent.putExtra(Constants.INTENT_EVENT_ID, eventId);
            startActivity(viewEventIntent);
            finish();
        }
    }
}
