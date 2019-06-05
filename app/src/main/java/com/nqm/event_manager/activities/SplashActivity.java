package com.nqm.event_manager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity implements IOnDataLoadComplete {

    boolean alreadyRun = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (DatabaseAccess.isAllDataLoaded()) {
            startActivity(new Intent(this, RootActivity.class));
            finish();
//            if (!alreadyRun) {
//                updateSalaryTime();
//                alreadyRun = true;
//            }
        } else {
            DatabaseAccess.setDatabaseListener(this);
        }
    }

    @Override
    public void notifyOnLoadComplete() {
        if (DatabaseAccess.isAllDataLoaded()) {
            startActivity(new Intent(this, RootActivity.class));
            finish();
//            if (!alreadyRun) {
//                updateSalaryTime();
//                alreadyRun = true;
//            }
        }
    }

    private void updateEventTime() {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        long mili;
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
        for (Event e : EventRepository.getInstance().getAllEvents().values()) {
            DocumentReference docRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.EVENT_COLLECTION)
                    .document(e.getId());
            Map<String, Object> data = new HashMap<>();
            try {
                calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(e.getNgayBatDau()));
                calendar2.setTime(CalendarUtil.sdfTime.parse(e.getGioBatDau()));
                calendar.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, calendar2.get(Calendar.MINUTE));
                mili = calendar.getTimeInMillis();
                data.put(Constants.EVENT_START_MILI, mili);
                batch.update(docRef, data);

                calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(e.getNgayKetThuc()));
                calendar2.setTime(CalendarUtil.sdfTime.parse(e.getGioKetThuc()));
                calendar.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, calendar2.get(Calendar.MINUTE));
                mili = calendar.getTimeInMillis();
                data.put(Constants.EVENT_END_MILI, mili);
                batch.update(docRef, data);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        batch.commit();
    }

    private void updateSalaryTime() {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        long mili;
        WriteBatch batch = DatabaseAccess.getInstance().getDatabase().batch();
        for (Salary s : SalaryRepository.getInstance().getAllSalaries().values()) {
            Event e = EventRepository.getInstance().getEventByEventId(s.getEventId());
            DocumentReference docRef = DatabaseAccess.getInstance().getDatabase()
                    .collection(Constants.SALARY_COLLECTION)
                    .document(s.getSalaryId());
            Map<String, Object> data = new HashMap<>();
            try {
                calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(e.getNgayBatDau()));
                calendar2.setTime(CalendarUtil.sdfTime.parse(e.getGioBatDau()));
                calendar.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, calendar2.get(Calendar.MINUTE));
                mili = calendar.getTimeInMillis();
                data.put(Constants.EVENT_START_MILI, mili);
                batch.update(docRef, data);

                calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(e.getNgayKetThuc()));
                calendar2.setTime(CalendarUtil.sdfTime.parse(e.getGioKetThuc()));
                calendar.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, calendar2.get(Calendar.MINUTE));
                mili = calendar.getTimeInMillis();
                data.put(Constants.EVENT_END_MILI, mili);
                batch.update(docRef, data);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        batch.commit();
    }
}
