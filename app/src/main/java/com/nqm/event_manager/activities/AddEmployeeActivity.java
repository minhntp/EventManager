package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEmployeeActivity extends AppCompatActivity {

    Activity context;

    Employee employee;

    Toolbar toolbar;
    EditText nameEditText, specialityEditText, phoneNumberEditText, dateOfBirthEditText,
            emailEditText, cmndEditText;

    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee);

        connectViews();
        context = this;
        addEvents();
    }

    private void connectViews() {
        toolbar = findViewById(R.id.add_employee_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.add_employee_single_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nameEditText = findViewById(R.id.add_employee_activity_name_edit_text);
        specialityEditText = findViewById(R.id.add_employee_activity_speciality_edit_text);
        phoneNumberEditText = findViewById(R.id.add_employee_activity_phone_number_edit_text);
        dateOfBirthEditText = findViewById(R.id.add_employee_activity_date_of_birth_edit_text);
        emailEditText = findViewById(R.id.add_employee_activity_email_edit_text);
        cmndEditText = findViewById(R.id.add_employee_activity_cmnd_edit_text);
    }

    private void addEvents() {
        dateOfBirthEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                if (!dateOfBirthEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(dateOfBirthEditText.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int d = calendar.get(Calendar.DAY_OF_MONTH);
                final int m = calendar.get(Calendar.MONTH);
                int y = calendar.get(Calendar.YEAR);
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
                datePickerDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_employee_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_employee_activity_action_save) {
            addEmployeeToDatabase();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addEmployeeToDatabase() {
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

        Map<String, Object> newEmployeeData = new HashMap<>();
        newEmployeeData.put(Constants.EMPLOYEE_NAME, nameEditText.getText().toString());
        newEmployeeData.put(Constants.EMPLOYEE_SPECIALITY, specialityEditText.getText().toString());
        newEmployeeData.put(Constants.EMPLOYEE_IDENTITY, cmndEditText.getText().toString());
        newEmployeeData.put(Constants.EMPLOYEE_DAY_OF_BIRTH, dateOfBirthEditText.getText().toString());
        newEmployeeData.put(Constants.EMPLOYEE_PHONE_NUMBER, phoneNumberEditText.getText().toString());
        newEmployeeData.put(Constants.EMPLOYEE_EMAIL, emailEditText.getText().toString());

        DatabaseAccess.getInstance().getDatabase()
                .collection(Constants.EMPLOYEE_COLLECTION)
                .add(newEmployeeData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Intent intent = new Intent();
                        intent.putExtra("add?", true);
                        intent.putExtra("add succeed", true);
                        setResult(RESULT_OK, intent);
                        context.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Intent intent = new Intent();
                        intent.putExtra("add?", true);
                        intent.putExtra("add succeed", false);
                        setResult(RESULT_OK, intent);
                        context.finish();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Trở về mà không lưu")
                .setMessage("Bạn có chắc chắn không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra("added?", false);
                        setResult(RESULT_OK, intent);
                        context.finish();
                    }
                })
                .show();
        return super.onSupportNavigateUp();
    }

}
