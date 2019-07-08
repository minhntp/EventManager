package com.nqm.event_manager.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.EditSalaryAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;

public class EditSalaryFromViewEventActivity extends AppCompatActivity {
    Toolbar toolbar;

    CustomListView salaryListView;

    ArrayList<Salary> salaries;
    EditSalaryAdapter editSalaryAdapter;
    String eventId;
    Event event;

    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_salary_from_view_event);

        context = this;
        connectViews();

        eventId = getIntent().getStringExtra("eventId");
        event = EventRepository.getInstance().getAllEvents().get(eventId);
        salaries = SalaryRepository.getInstance().getSalariesByEventId(eventId);

        editSalaryAdapter = new EditSalaryAdapter(this, salaries);
        salaryListView.setAdapter(editSalaryAdapter);

        addEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_salary_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit_salary_action_save_salaries) {
            saveSalaries();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveSalaries() {
        for (int i = 0; i < salaryListView.getChildCount(); i++) {
            EditText salaryEditText = salaryListView.getChildAt(i).findViewById(R.id.edit_salary_salary_edit_text);
            CheckBox isPaidCheckBox = salaryListView.getChildAt(i).findViewById(R.id.edit_salary_paid_checkbox);

//            Log.d("debug", "salary = " + salaryEditText.getText().toString());
            if (salaryEditText.getText().toString().equals("")) {
                salaries.get(i).setSalary(0);
            } else {
                salaries.get(i).setSalary(Integer.parseInt(salaryEditText.getText().toString()));
//                Log.d("debug", "salary Int = " + Integer.parseInt(salaryEditText.getText().toString()));
            }
            salaries.get(i).setPaid(isPaidCheckBox.isChecked());
        }
        SalaryRepository.getInstance().updateSalaries(salaries);
        context.finish();
    }

    private void connectViews() {
        toolbar = findViewById(R.id.edit_salary_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.edit_salary_activity_label);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        salaryListView = findViewById(R.id.edit_salary_from_view_event_salary_list_view);
    }

    private void addEvents() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_error)
                .setTitle("Trở về mà không lưu?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.finish();
                    }

                })
                .setNegativeButton("Hủy", null)
                .show();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }
}
