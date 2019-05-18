package com.nqm.event_manager.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.EditSalaryAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class EditSalaryFromViewEventActivity extends AppCompatActivity {
    Toolbar toolbar;

    TextView titleEditText, timeEditText, locationEditText, noteEditText;
    CustomListView salaryListView;

    HashMap<String, Salary> salaries;
    ArrayList<String> salariesIds;
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
        event = EventRepository.getInstance(null).getAllEvents().get(eventId);
        salaries = SalaryRepository.getInstance(null).getSalariesByEventId(eventId);
        salariesIds = new ArrayList<>(salaries.keySet());

        editSalaryAdapter = new EditSalaryAdapter(this, salariesIds);
        salaryListView.setAdapter(editSalaryAdapter);

        addEvents();
        fillInformation();
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
        ArrayList<Integer> salariesAmounts = new ArrayList<>();
        ArrayList<Boolean> salariesPaidStatus = new ArrayList<>();
        for (int i = 0; i < salaryListView.getChildCount(); i++) {
            EditText salaryEditText = salaryListView.getChildAt(i).findViewById(R.id.edit_salary_salary_edit_text);
            CheckBox isPaidCheckBox = salaryListView.getChildAt(i).findViewById(R.id.edit_salary_paid_checkbox);

            if (salaryEditText.getText().toString().equals("")) {
                salariesAmounts.add(0);
            } else {
                salariesAmounts.add(Integer.parseInt(salaryEditText.getText().toString()));
            }
            salariesPaidStatus.add(isPaidCheckBox.isChecked());
        }
        SalaryRepository.getInstance(null).updateSalaries(salariesIds, salariesAmounts, salariesPaidStatus, new SalaryRepository.MyUpdateSalariesCallback() {
            @Override
            public void onCallback(boolean updateSucceed) {
                Intent intent = new Intent();
                intent.putExtra("edit salaries", true);
                intent.putExtra("edit salaries succeed", true);
                setResult(RESULT_OK, intent);
                ((Activity) context).finish();
            }
        });
    }

    private void connectViews() {
        toolbar = findViewById(R.id.edit_salary_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chỉnh sửa lương");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titleEditText = findViewById(R.id.edit_salary_from_view_event_title_text_view);
        timeEditText = findViewById(R.id.edit_salary_from_view_event_time_text_view);
        locationEditText = findViewById(R.id.edit_salary_from_view_event_location_text_view);
        noteEditText = findViewById(R.id.edit_salary_from_view_event_note_text_view);
        salaryListView = findViewById(R.id.edit_salary_from_view_event_salary_list_view);
    }

    private void addEvents() {

    }

    private void fillInformation() {
        titleEditText.setText(event.getTen());
        timeEditText.setText(event.getGioBatDau() + " - " + event.getNgayBatDau() + "\n"
                + event.getGioKetThuc() + " - " + event.getNgayKetThuc());
        locationEditText.setText(event.getDiaDiem());
        noteEditText.setText(event.getGhiChu());
    }

    @Override
    public boolean onSupportNavigateUp() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Trở về mà không lưu?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra("edit salaries", false);
                        setResult(RESULT_OK, intent);
                        context.finish();
                    }

                })
                .setNegativeButton("Hủy", null)
                .show();
        return super.onSupportNavigateUp();
    }
}
