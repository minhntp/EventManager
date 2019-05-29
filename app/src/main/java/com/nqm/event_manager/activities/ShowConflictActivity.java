package com.nqm.event_manager.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.ShowConflictEventAdapter;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.utils.Constants;

import java.util.ArrayList;

public class ShowConflictActivity extends AppCompatActivity {

    String startTime, endTime;
    String employeeId;
    Employee employee;
    ArrayList<String> conflictEventsIds;

    TextView startTimeTextView, endtimeTextView, nameTextView, specialityTextView;
    ImageView profileImageView;
    ShowConflictEventAdapter conflictEventsAdapter;
    RecyclerView conflictEventsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_conflict);

        //GET INFO FROM INTENT
        employeeId = getIntent().getStringExtra(Constants.INTENT_EMPLOYEE_ID);
        employee = EmployeeRepository.getInstance().getAllEmployees().get(employeeId);
        startTime = getIntent().getStringExtra(Constants.INTENT_START_TIME);
        endTime = getIntent().getStringExtra(Constants.INTENT_END_TIME);
        conflictEventsIds = (ArrayList<String>) getIntent().getSerializableExtra(Constants.INTENT_CONFLICT_EVENTS_IDS);

        connectViews();
        init();

    }

    private void connectViews() {
        startTimeTextView = findViewById(R.id.conflict_activity_start_time_text_view);
        endtimeTextView = findViewById(R.id.conflict_activity_end_time_text_view);

        profileImageView = findViewById(R.id.conflict_activity_profile_image_view);
        nameTextView = findViewById(R.id.conflict_activity_name_text_view);
        specialityTextView = findViewById(R.id.conflict_activity_speciality_text_view);

        conflictEventsRecyclerView = findViewById(R.id.conflict_activity_event_recycler_view);
    }

    private void init() {
        startTimeTextView.setText(startTime);
        endtimeTextView.setText(endTime);

        nameTextView.setText(employee.getHoTen());
        specialityTextView.setText(employee.getChuyenMon());

        conflictEventsAdapter = new ShowConflictEventAdapter(conflictEventsIds);
        conflictEventsRecyclerView.setAdapter(conflictEventsAdapter);
        conflictEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
