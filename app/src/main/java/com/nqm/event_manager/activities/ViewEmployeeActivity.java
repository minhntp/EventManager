package com.nqm.event_manager.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.nqm.event_manager.fragments.ManageEmployeeFragment;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;

public class ViewEmployeeActivity extends AppCompatActivity implements IOnDataLoadComplete {

    public static IOnDataLoadComplete thisListener;
    Activity context;
    Employee employee;
    String employeeId;
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
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Xóa nhân viên")
                    .setMessage("Bạn có chắc chắn không?")
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EmployeeRepository.getInstance().setListener(ManageEmployeeFragment.thisListener);
                            EmployeeRepository.getInstance().deleteEmployeeByEmployeeId(employee.getId());
                            context.finish();
                        }

                    })
                    .setNegativeButton("Không", null)
                    .show();
            return true;
        }

        if (id == R.id.view_employee_action_edit) {
            Intent intent = new Intent(this, EditEmployeeActivity.class);
            intent.putExtra("employeeId", employee.getId());
            startActivity(intent);
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
        thisListener = this;
        EmployeeRepository.getInstance().setListener(this);

        toolbar = findViewById(R.id.view_employee_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.view_employee_single_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        employeeId = getIntent().getStringExtra("employeeId");
        employee = EmployeeRepository.getInstance(null).getAllEmployees().get(employeeId);

        fillInformation();
    }

    private void fillInformation() {
        if (employee != null) {
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
                if (!employee.getSdt().isEmpty()) {
                    context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", employee.getSdt(), null)));
                } else {
                    Toast.makeText(context, "Không có số điện thoại", Toast.LENGTH_SHORT).show();
                }
            }
        });

        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!employee.getSdt().isEmpty()) {
                    context.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", employee.getSdt(), null)));
                } else {
                    Toast.makeText(context, "Không có số điện thoại", Toast.LENGTH_SHORT).show();
                }
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!employee.getEmail().isEmpty()) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    String mailto = "mailto:";
                    mailto += employee.getEmail();
                    emailIntent.setData(Uri.parse(mailto));
                    try {
                        startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng gửi Email:"));
                    } catch (Exception e) {
                        Toast.makeText(context, "Không tìm thấy ứng dụng gửi email", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Không có địa chỉ Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EmployeeRepository.getInstance().setListener(this);
        employee = EmployeeRepository.getInstance().getAllEmployees().get(employeeId);
        fillInformation();
    }

    @Override
    public void notifyOnLoadComplete() {
        employee = EmployeeRepository.getInstance().getAllEmployees().get(employeeId);
        fillInformation();
    }
}
