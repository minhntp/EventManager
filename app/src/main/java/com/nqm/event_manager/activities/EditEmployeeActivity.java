package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.Calendar;

public class EditEmployeeActivity extends AppCompatActivity {
    Activity context;

    Employee employee;

    Toolbar toolbar;
    EditText nameEditText, specialityEditText, phoneNumberEditText, dateOfBirthEditText,
            emailEditText, cmndEditText;

    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_employee);

        connectViews();
        init();
        addEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_employee_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.edit_employee_activity_action_save) {
            updateEmployeeToDatabase();
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectViews() {
        toolbar = findViewById(R.id.edit_employee_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.edit_employee_single_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nameEditText = findViewById(R.id.edit_employee_activity_name_edit_text);
        specialityEditText = findViewById(R.id.edit_employee_activity_speciality_edit_text);
        phoneNumberEditText = findViewById(R.id.edit_employee_activity_phone_number_edit_text);
        dateOfBirthEditText = findViewById(R.id.edit_employee_activity_date_of_birth_edit_text);
        emailEditText = findViewById(R.id.edit_employee_activity_email_edit_text);
        cmndEditText = findViewById(R.id.edit_employee_activity_cmnd_edit_text);
    }

    private void init() {
        context = this;

        employee = EmployeeRepository.getInstance(null).getAllEmployees().get(getIntent().getStringExtra("employeeId"));

        nameEditText.setText(employee.getHoTen());
        specialityEditText.setText(employee.getChuyenMon());
        phoneNumberEditText.setText(employee.getSdt());
        dateOfBirthEditText.setText(employee.getNgaySinh());
        emailEditText.setText(employee.getEmail());
        cmndEditText.setText(employee.getCmnd());
    }

    private void addEvents() {
        dateOfBirthEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int d = 1;
                int m = 1;
                int y = 1990;
                calendar = Calendar.getInstance();
                if (!dateOfBirthEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(dateOfBirthEditText.getText().toString()));
                        d = calendar.get(Calendar.DAY_OF_MONTH);
                        m = calendar.get(Calendar.MONTH);
                        y = calendar.get(Calendar.YEAR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                dateOfBirthEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                            }
                        }, y, m, d);
                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
                datePickerDialog.show();
            }
        });
    }

    private void updateEmployeeToDatabase() {
        if (nameEditText.getText().toString().isEmpty()) {
            nameEditText.setError("Xin mời nhập");
            return;
        } else {
            nameEditText.setError(null);
        }
        if (specialityEditText.getText().toString().isEmpty()) {
            specialityEditText.setError("Xin mời nhập");
            return;
        } else {
            specialityEditText.setError(null);
        }

        Employee editedEmployee = new Employee(employee.getId(),
                nameEditText.getText().toString(),
                specialityEditText.getText().toString(),
                cmndEditText.getText().toString(),
                dateOfBirthEditText.getText().toString(),
                phoneNumberEditText.getText().toString(),
                emailEditText.getText().toString());

        EmployeeRepository.getInstance().setListener(ViewEmployeeActivity.thisListener);
        EmployeeRepository.getInstance().updateEmployee(editedEmployee);
        context.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        new android.support.v7.app.AlertDialog.Builder(this)
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
