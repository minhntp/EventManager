package com.nqm.event_manager.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.CalculateSalaryAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnCalculateSalaryItemClicked;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;

public class CalculateSalaryForSingleEmployeeActivity extends AppCompatActivity
        implements IOnCalculateSalaryItemClicked, IOnDataLoadComplete {

    Context context;

    Button calculateButton, payAllButton, saveButton;
    CustomListView resultListView;
    EditText startDateEditText, endDateEditText, sumEditText, paidEditText, unpaidEditText;
    TextView numberOfEventsTextView, sumTextView, nameTextView, specialityTextView;
    ImageView profileImageView;

    DatePickerDialog.OnDateSetListener dateSetListener;
    Calendar calendar = Calendar.getInstance();
    View currentView;

    ArrayList<Salary> resultSalaries;
    int resultEventsSize;

    CalculateSalaryAdapter calculateSalaryAdapter;

    String startDate, endDate;
    String selectedEmployeeId = "";

    Resources res;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_salary_for_single_employee);

        EventRepository.getInstance().setListener(this);
        EmployeeRepository.getInstance().setListener(this);
        SalaryRepository.getInstance().setListener(this);
        ScheduleRepository.getInstance().setListener(this);

        res = getResources();

        connectViews();
        init();
        addEvents();
    }

    private void connectViews() {
        calculateButton = findViewById(R.id.calculate_salary_single_calculate_button);
        payAllButton = findViewById(R.id.calculate_salary_single_pay_all_button);
        saveButton = findViewById(R.id.calculate_salary_single_save_button);

        resultListView = findViewById(R.id.calculate_salary_single_result_list_view);

        startDateEditText = findViewById(R.id.calculate_salary_single_start_date_edit_text);
        endDateEditText = findViewById(R.id.calculate_salary_single_end_date_edit_text);
        sumEditText = findViewById(R.id.calculate_salary_single_sum_edit_text);
        paidEditText = findViewById(R.id.calculate_salary_single_paid_edit_text);
        unpaidEditText = findViewById(R.id.calculate_salary_single_unpaid_edit_text);

        numberOfEventsTextView = findViewById(R.id.calculate_salary_single_number_of_events_text_view);
        sumTextView = findViewById(R.id.calculate_salary_single_sum_text_view);
        nameTextView = findViewById(R.id.calculate_salary_single_name_text_view);
        specialityTextView = findViewById(R.id.calculate_salary_single_speciality_text_view);

        profileImageView = findViewById(R.id.calculate_salary_single_profile_image_view);
    }

    private void init() {
        context = this;

        toolbar = findViewById(R.id.calculate_salary_single_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.calculate_salary_single_employee_label);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        SalaryRepository.getInstance().setListener(this);

        selectedEmployeeId = getIntent().getStringExtra(Constants.INTENT_EMPLOYEE_ID);
        Employee employee = EmployeeRepository.getInstance().getAllEmployees().get(selectedEmployeeId);
        if (employee != null) {
            nameTextView.setText(employee.getHoTen());
            specialityTextView.setText(employee.getChuyenMon());
        }

        startDate = CalendarUtil.sdfDayMonthYear.format(calendar.getTime());
        endDate = CalendarUtil.sdfDayMonthYear.format(calendar.getTime());
        startDateEditText.setText(startDate);
        endDateEditText.setText(endDate);

        resultSalaries = new ArrayList<>();
        resultEventsSize = 0;
        calculateSalaryAdapter = new CalculateSalaryAdapter(this, resultSalaries);
        calculateSalaryAdapter.setListener(this);
        resultListView.setAdapter(calculateSalaryAdapter);
    }

    private void addEvents() {
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

//                Update TextEdits & TextViews;
                if (currentView == startDateEditText) {
                    startDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    startDateEditText.setError(null);
                } else {
                    endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    endDateEditText.setError(null);
                }
                startDate = startDateEditText.getText().toString();
                endDate = endDateEditText.getText().toString();
            }
        };

        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                if (!startDateEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                currentView = startDateEditText;
                int d = calendar.get(Calendar.DAY_OF_MONTH);
                int m = calendar.get(Calendar.MONTH);
                int y = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        dateSetListener, y, m, d);
                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
                datePickerDialog.show();
            }
        });

        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                if (!endDateEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                currentView = endDateEditText;
                int d = calendar.get(Calendar.DAY_OF_MONTH);
                int m = calendar.get(Calendar.MONTH);
                int y = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        dateSetListener, y, m, d);
                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
                datePickerDialog.show();
            }
        });

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDate = startDateEditText.getText().toString();
                endDate = endDateEditText.getText().toString();
                getResultSalaries();
                showResult();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Bạn có chắn chắn muốn lưu thay đổi?")
                        .setIcon(R.drawable.ic_error)
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                saveChanges(false);
                            }
                        })
                        .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Undo changes by reloading data
                                sumTextView.requestFocus();
                                calculateSalaryAdapter.notifyDataSetChanged();
                            }
                        }).show();
            }
        });

        payAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Bạn có chắn chắn muốn thanh toán tất cả?")
                        .setIcon(R.drawable.ic_error)
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                saveChanges(true);
                            }
                        })
                        .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Undo changes by reloading data
                                sumTextView.requestFocus();
                                calculateSalaryAdapter.notifyDataSetChanged();
                            }
                        }).show();
            }
        });
    }

    private void getResultSalaries() {
        resultSalaries = SalaryRepository.getInstance()
                .getSalariesByStartDateAndEndDateAndEmployeeId(startDate, endDate, selectedEmployeeId);

        ArrayList<String> resultEventIds = new ArrayList<>();
        for (Salary s : resultSalaries) {
            if (!resultEventIds.contains(s.getEventId())) {
                resultEventIds.add(s.getEventId());
            }
        }
        resultEventsSize = resultEventIds.size();

        SalaryRepository.sortSalariesByEventStartDate(resultSalaries);
    }

    public void showResult() {
        numberOfEventsTextView.setText(String.format(getResources().getString(R.string.salary_num_of_events), resultEventsSize));

        calculateSalaryAdapter.notifyDataSetChanged(resultSalaries);

        boolean allSalariesPaid = true;
        int sum, paid = 0, unpaid = 0;

        for (Salary s : resultSalaries) {
            if (s.isPaid()) {
                paid += s.getSalary();
            } else {
                unpaid += s.getSalary();
                allSalariesPaid = false;
            }
        }
        sum = paid + unpaid;

        sumEditText.setText(String.format(res.getString(R.string.number), sum));
        paidEditText.setText(String.format(res.getString(R.string.number), paid));
        unpaidEditText.setText(String.format(res.getString(R.string.number), unpaid));

        saveButton.setEnabled(!allSalariesPaid);
        payAllButton.setEnabled(!allSalariesPaid);
    }

    private void saveChanges(boolean payAll) {
        for (int i = 0; i < resultListView.getChildCount(); i++) {
            EditText salaryEditText = resultListView.getChildAt(i)
                    .findViewById(R.id.calculate_salaries_list_item_salary_edit_text);
            CheckBox isPaidCheckBox = resultListView.getChildAt(i)
                    .findViewById(R.id.calculate_salaries_list_item_paid_checkbox);

            if (salaryEditText.getText().toString().equals("")) {
                resultSalaries.get(i).setSalary(0);
            } else {
                resultSalaries.get(i).setSalary(Integer.parseInt(salaryEditText.getText().toString()));
            }

            if (isPaidCheckBox.isChecked() || payAll) {
                resultSalaries.get(i).setPaid(true);
            } else {
                resultSalaries.get(i).setPaid(false);
            }
        }

        SalaryRepository.getInstance().updateSalaries(resultSalaries);
        showResult();
    }

    @Override
    protected void onResume() {
        EventRepository.getInstance().setListener(this);
        EmployeeRepository.getInstance().setListener(this);
        SalaryRepository.getInstance().setListener(this);
        ScheduleRepository.getInstance().setListener(this);
        getResultSalaries();
        showResult();
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onCalculateSalaryItemClicked(String eventId) {
        Intent intent = new Intent(this, ViewEventActivity.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    @Override
    public void notifyOnLoadComplete() {
        getResultSalaries();
        showResult();
    }
}
