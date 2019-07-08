package com.nqm.event_manager.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.CalculateSalaryAdapter;
import com.nqm.event_manager.custom_views.CustomDatePicker;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnCalculateSalaryItemClicked;
import com.nqm.event_manager.interfaces.IOnCustomDatePickerItemClicked;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalculateSalaryForSingleEmployeeActivity extends AppCompatActivity
        implements IOnCalculateSalaryItemClicked, IOnDataLoadComplete, IOnCustomDatePickerItemClicked {

    Context context;

    Dialog datePickerDialog;
    TextView datePickerDialogDateTextView;
    CustomDatePicker datePicker;
    Button datePickerDialogOkButton, datePickerDialogCancelButton;
    EditText selectedDateEditText;

    Button calculateButton, payAllButton, saveButton;
    CustomListView resultListView;
    EditText startDateEditText, endDateEditText, sumEditText, paidEditText, unpaidEditText;
    TextView numberOfEventsTextView, sumTextView, nameTextView, specialityTextView;
    ImageView profileImageView;

    Calendar calendar = Calendar.getInstance();

    ArrayList<Salary> resultSalaries;
    int resultEventsSize;

    CalculateSalaryAdapter calculateSalaryAdapter;

    //    String startDate, endDate;
    String selectedEmployeeId = "";

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_salary_for_single_employee);

        EventRepository.getInstance().setListener(this);
        EmployeeRepository.getInstance().setListener(this);
        SalaryRepository.getInstance().setListener(this);
        ScheduleRepository.getInstance().setListener(this);

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

        initDatePickerDialog();

        selectedEmployeeId = getIntent().getStringExtra(Constants.INTENT_EMPLOYEE_ID);
        Employee employee = EmployeeRepository.getInstance().getAllEmployees().get(selectedEmployeeId);
        if (employee != null) {
            nameTextView.setText(employee.getHoTen());
            specialityTextView.setText(employee.getChuyenMon());
        }

        startDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
        endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));

        resultSalaries = new ArrayList<>();
        resultEventsSize = 0;
        calculateSalaryAdapter = new CalculateSalaryAdapter(this, resultSalaries);
        calculateSalaryAdapter.setListener(this);
        resultListView.setAdapter(calculateSalaryAdapter);
    }

    private void initDatePickerDialog() {
        datePickerDialog = new Dialog(this);
        datePickerDialog.setContentView(R.layout.dialog_custom_date_picker);

        datePickerDialogDateTextView = datePickerDialog.findViewById(R.id.custom_date_picker_dialog_date_text_view);
        datePicker = datePickerDialog.findViewById(R.id.custom_date_picker_calendar_view);
        datePickerDialogCancelButton = datePickerDialog.findViewById(R.id.custom_date_picker_cancel_button);
        datePickerDialogOkButton = datePickerDialog.findViewById(R.id.custom_date_picker_ok_button);

        datePickerDialogDateTextView.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder(datePickerDialogDateTextView.getText().toString());
            sb.delete(0, sb.lastIndexOf("-") + 1);
            try {
                datePicker.setViewDate(CalendarUtil.sdfDayMonthYear.parse(sb.toString()));
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        });

        datePicker.setListener(this);

        datePickerDialogCancelButton.setOnClickListener(v -> datePickerDialog.dismiss());

        datePickerDialogOkButton.setOnClickListener(v -> {
            String selectedDate = datePicker.getSelectedDate();
            selectedDateEditText.setText(selectedDate);
            try {
                Date startDate = CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString());
                Date endDate = CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString());

                if (startDate.compareTo(endDate) > 0) {
                    if (selectedDateEditText == startDateEditText) {
                        endDateEditText.setText(selectedDate);
                    } else {
                        startDateEditText.setText(selectedDate);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            datePickerDialog.dismiss();
        });
    }

    private void addEvents() {
        startDateEditText.setOnClickListener(v -> {
            selectedDateEditText = startDateEditText;
            showDatePickerDialog();
        });

        endDateEditText.setOnClickListener(v -> {
            selectedDateEditText = endDateEditText;
            showDatePickerDialog();
        });

        calculateButton.setOnClickListener(v -> {
            getResultSalaries();
            showResult();
        });

        saveButton.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle("Bạn có chắn chắn muốn lưu thay đổi?")
                .setIcon(R.drawable.ic_error)
                .setPositiveButton("Có", (dialog, whichButton) -> saveChanges(false))
                .setNegativeButton("Không", (dialog, which) -> {
                    //Undo changes by reloading data
                    sumTextView.requestFocus();
                    calculateSalaryAdapter.notifyDataSetChanged();
                }).show());

        payAllButton.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle("Bạn có chắn chắn muốn thanh toán tất cả?")
                .setIcon(R.drawable.ic_error)
                .setPositiveButton("Có", (dialog, whichButton) -> saveChanges(true))
                .setNegativeButton("Không", (dialog, which) -> {
                    //Undo changes by reloading data
                    sumTextView.requestFocus();
                    calculateSalaryAdapter.notifyDataSetChanged();
                }).show());
    }

    private void showDatePickerDialog() {
        try {
            Date dateFromEditText = CalendarUtil.sdfDayMonthYear.parse(selectedDateEditText.getText().toString());
            datePicker.setViewDate(dateFromEditText);
            datePicker.setSelectedDate(dateFromEditText);
            String txt = CalendarUtil.sdfDayOfWeek.format(dateFromEditText) +
                    " - " + CalendarUtil.sdfDayMonthYear.format(dateFromEditText);
            datePickerDialogDateTextView.setText(txt);
            datePickerDialog.show();
            if (datePickerDialog.getWindow() != null) {
                datePickerDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getResultSalaries() {
        resultSalaries = SalaryRepository.getInstance()
                .getSalariesByStartDateAndEndDateAndEmployeeId(startDateEditText.getText().toString(),
                        endDateEditText.getText().toString(), selectedEmployeeId);

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
        if (resultSalaries.size() > 0) {
            numberOfEventsTextView.setText(String.format(getResources().getString(R.string.salary_num_of_events),
                    resultSalaries.size(), resultEventsSize));
        } else {
            numberOfEventsTextView.setText(getResources().getString(R.string.salary_no_salaries));
        }

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

        sumEditText.setText(String.valueOf(sum));
        paidEditText.setText(String.valueOf(paid));
        unpaidEditText.setText(String.valueOf(unpaid));

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

    @Override
    public void onCustomDatePickerItemClicked(String selectedDate, String dayOfWeek) {
        datePickerDialogDateTextView.setText(String.format(Locale.US, "%s - %s", dayOfWeek, selectedDate));

    }
}
