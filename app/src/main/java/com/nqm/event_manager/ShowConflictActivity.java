package com.nqm.event_manager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nqm.event_manager.adapters.ShowConflictEventAdapter;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;

public class ShowConflictActivity extends AppCompatActivity {

    TextView employeeNameTextView, employeeSpecialityTextView;
    ImageView employeeProfileImageView;
    ListView conflictEventListView;

    ShowConflictEventAdapter eventAdapter;

    Employee employee;
    ArrayList<Salary> conflictSalaries;
    ArrayList<Event> conflictEvents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_conflict);

        employee = EmployeeRepository.getInstance().getAllEmployees().get(getIntent().getStringExtra("employeeId"));
//        conflictSalaries = SalaryRepository.getInstance().getSalariesByStartTimeAndEndTimeAndEmployeeId()
//        conflictEvents = EventRepository.getInstance().

    }
}
