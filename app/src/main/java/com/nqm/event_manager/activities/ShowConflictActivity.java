package com.nqm.event_manager.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.ShowConflictEventAdapter;
import com.nqm.event_manager.interfaces.IOnConflictEventItemClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.utils.Constants;

import java.util.ArrayList;

public class ShowConflictActivity extends BaseActivity implements IOnConflictEventItemClicked {

    Toolbar toolbar;

    String startTime, endTime;
    String employeeId;
    Employee employee;
    ArrayList<String> conflictEventsIds;

    TextView startTimeTextView, endtimeTextView, nameTextView, specialityTextView;
    ImageView profileImageView;
    ShowConflictEventAdapter conflictEventsAdapter;
    RecyclerView conflictEventsRecyclerView;

    @SuppressWarnings("unchecked")
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
        toolbar = findViewById(R.id.show_conflict_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.show_conflict_activity_label);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        startTimeTextView.setText(startTime);
        endtimeTextView.setText(endTime);

        nameTextView.setText(employee.getHoTen());
        specialityTextView.setText(employee.getChuyenMon());

        conflictEventsAdapter = new ShowConflictEventAdapter(conflictEventsIds);
        conflictEventsAdapter.setListener(this);
        conflictEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conflictEventsRecyclerView.setAdapter(conflictEventsAdapter);
        conflictEventsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onResume() {
        conflictEventsAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onConflictEventItemClicked(String eventId) {
        Intent viewEventIntent = new Intent(this, ViewEventActivity.class);
        viewEventIntent.putExtra(Constants.INTENT_EVENT_ID, eventId);
        startActivity(viewEventIntent);
    }
}
