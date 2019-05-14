package com.nqm.event_manager.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

public class ViewEmployeeActivity extends AppCompatActivity {

    Activity context;

    private static final int RESULT_FROM_EDIT_EMPLOYEE_INTENT = 7;

    Employee employee;

    TextView nameTextView, specialityTextView, phoneNumberTextView, dateOfBirthTextView,
            emailTextView, cmndTextView;
    ImageButton callButton, messageButton, emailButton;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employee);

        connectViews();
        init();
        addEvents();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_employee_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.view_employee_action_delete) {
            Log.d("debug", "deleting " + employee.getId());
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Xóa nhân viên")
                    .setMessage("Bạn có chắc chắn không?")
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.putExtra("delete?", true);
                            intent.putExtra("employeeId", employee.getId());
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                    })
                    .setNegativeButton("Không", null)
                    .show();
            return true;
        }

        if (id == R.id.view_employee_action_edit) {
            Intent intent = new Intent(this, EditEmployeeActivity.class);
            intent.putExtra("employeeId", employee.getId());
            startActivityForResult(intent, RESULT_FROM_EDIT_EMPLOYEE_INTENT);
            return true;
        }

        if (id == R.id.view_employee_action_salary) {
            Intent intent = new Intent(this, CalculateSalaryForSingleEmployeeActivity.class);
            intent.putExtra("employeeId", employee.getId());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectViews() {
        nameTextView = findViewById(R.id.view_employee_activity_name_text_view);
        specialityTextView = findViewById(R.id.view_employee_activity_speciality_text_view);
        phoneNumberTextView = findViewById(R.id.view_employee_activity_phone_number_text_view);
        dateOfBirthTextView = findViewById(R.id.view_employee_activity_date_of_birth_text_view);
        emailTextView = findViewById(R.id.view_employee_activity_email_text_view);
        cmndTextView = findViewById(R.id.view_employee_activity_cmnd_text_view);

        callButton = findViewById(R.id.view_employee_activity_call_button);
        messageButton = findViewById(R.id.view_employee_activity_message_button);
        emailButton = findViewById(R.id.view_employee_activity_email_button);
    }

    private void init() {
        context = this;

        toolbar = findViewById(R.id.view_employee_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.view_employee_single_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        employee = EmployeeRepository.getInstance(null).getAllEmployees().get(getIntent()
                .getStringExtra("employeeId"));

        fillInformation();
    }

    private void fillInformation() {
        if(employee != null) {
            nameTextView.setText(employee.getHoTen());
            specialityTextView.setText(employee.getChuyenMon());
            phoneNumberTextView.setText(employee.getSdt());
            dateOfBirthTextView.setText(employee.getNgaySinh());
            emailTextView.setText(employee.getEmail());
            cmndTextView.setText(employee.getCmnd());
        }
    }

    private void addEvents() {
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!employee.getSdt().isEmpty()) {
                    context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", employee.getSdt(), null)));
                } else {
                    Toast.makeText(context, "Không có số điện thoại", Toast.LENGTH_SHORT).show();
                }
            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!employee.getSdt().isEmpty()) {
                    context.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", employee.getSdt(), null)));
                } else {
                    Toast.makeText(context, "Không có số điện thoại", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!employee.getEmail().isEmpty()) {
                    String[] TO = {employee.getEmail()};
                    String[] CC = {""};
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setDataAndType(Uri.parse("mailto"), "text/plain");

                    emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                    emailIntent.putExtra(Intent.EXTRA_CC, CC);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");

                    try {
                        context.startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng gửi email..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(context, "Không tìm thấy ứng dụng gửi email", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Không có địa chỉ Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_FROM_EDIT_EMPLOYEE_INTENT && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("edit?", false)) {
                if (data.getBooleanExtra("edit succeed", false)) {
                    employee = EmployeeRepository.getInstance(null).getAllEmployees().get(employee.getId());
                    fillInformation();
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Lưu thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
