package com.nqm.event_manager.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    CustomListView salaryListView;

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
        salariesIds = SalaryRepository.getInstance(null).getSalariesIdsByEventId(eventId);

        editSalaryAdapter = new EditSalaryAdapter(this, salariesIds);
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
                if (updateSucceed) {
                    Toast.makeText(context, "Cập nhật lương thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Cập nhật lương thất bại", Toast.LENGTH_SHORT).show();
                }
                context.finish();
            }
        });
    }

    private void connectViews() {
        toolbar = findViewById(R.id.edit_salary_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.edit_salary_activity_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        salaryListView = findViewById(R.id.edit_salary_from_view_event_salary_list_view);
    }

    private void addEvents() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
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
