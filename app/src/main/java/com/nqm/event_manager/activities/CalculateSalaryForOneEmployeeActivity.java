package com.nqm.event_manager.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.CalculateSalaryAdapter;
import com.nqm.event_manager.custom_views.CustomDatePicker;
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
import com.nqm.event_manager.utils.StringUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalculateSalaryForOneEmployeeActivity extends BaseActivity
        implements IOnCalculateSalaryItemClicked, IOnDataLoadComplete, IOnCustomDatePickerItemClicked {

    Context context;

    Dialog datePickerDialog;
    TextView datePickerDialogDateTextView;
    CustomDatePicker datePicker;
    Button datePickerDialogTodayButton, datePickerDialogOkButton, datePickerDialogCancelButton;
    String selectedDateText;
    String clickedEditText;

    Button calculateButton, payAllButton, saveButton;
    RecyclerView resultRecyclerView;
    CalculateSalaryAdapter calculateSalaryAdapter;
    EditText startDateEditText, endDateEditText, sumEditText, paidEditText, unpaidEditText;
    TextView numberOfEventsTextView, sumTextView, nameTextView, specialityTextView;
    ImageView profileImageView;

    Calendar calendar = Calendar.getInstance();

    List<Salary> queryResultSalaries;
//    List<String> queryResultEventsIds;

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

        resultRecyclerView = findViewById(R.id.calculate_salary_single_result_recycler_view);

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

        queryResultSalaries = new ArrayList<>();
//        queryResultEventsIds = new ArrayList<>();

        calculateSalaryAdapter = new CalculateSalaryAdapter(queryResultSalaries);
        calculateSalaryAdapter.setListener(this);
        resultRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        resultRecyclerView.setAdapter(calculateSalaryAdapter);
    }

    private void initDatePickerDialog() {
        datePickerDialog = new Dialog(this);
        datePickerDialog.setContentView(R.layout.dialog_custom_date_picker);

        datePickerDialogDateTextView = datePickerDialog.findViewById(R.id.custom_date_picker_dialog_date_text_view);
        datePicker = datePickerDialog.findViewById(R.id.custom_date_picker_calendar_view);
        datePickerDialogTodayButton = datePickerDialog.findViewById(R.id.custom_date_picker_today_button);
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

        datePickerDialogTodayButton.setOnClickListener(v -> {
            datePicker.setViewDate(Calendar.getInstance().getTime());
        });

        datePickerDialogCancelButton.setOnClickListener(v -> datePickerDialog.dismiss());

        datePickerDialogOkButton.setOnClickListener(v -> {
            selectedDateText = datePicker.getSelectedDateString();
            try {
                Date selectedDate = CalendarUtil.sdfDayMonthYear.parse(selectedDateText);
                Date startDate = CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString());
                Date endDate = CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString());

                if (clickedEditText.equals(StringUtil.startDateEditText)) {
                    // startDate clicked
                    if (!selectedDate.before(endDate)) {
                        // Set startDate = selectedDate, endDate = last day_of_month of startDate
                        calendar.setTime(selectedDate);
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    }
                    startDateEditText.setText(selectedDateText);
                } else {
                    // endDate clicked
                    if (!selectedDate.after(startDate)) {
                        // Set endDate = selectedDate, startDate = first day_of_month of endDate
                        calendar.setTime(selectedDate);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        startDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    }
                    endDateEditText.setText(selectedDateText);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            datePickerDialog.dismiss();
        });
    }

    private void addEvents() {
        startDateEditText.setOnClickListener(v -> {
            clickedEditText = StringUtil.startDateEditText;
            selectedDateText = startDateEditText.getText().toString();
            showDatePickerDialog();
        });

        endDateEditText.setOnClickListener(v -> {
            clickedEditText = StringUtil.endDateEditText;
            selectedDateText = endDateEditText.getText().toString();
            showDatePickerDialog();
        });

        calculateButton.setOnClickListener(v -> {
            getResultSalaries();
            showResult();
        });

        saveButton.setOnClickListener(v -> confirmBeforeSaving(false));

        payAllButton.setOnClickListener(v -> confirmBeforeSaving(true));
    }

    private void showDatePickerDialog() {
        try {
            Date dateFromEditText = CalendarUtil.sdfDayMonthYear.parse(selectedDateText);
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
            System.out.println( Log.getStackTraceString(e));
        }
    }

    private void getResultSalaries() {
        queryResultSalaries.clear();
        queryResultSalaries.addAll(SalaryRepository.getInstance().getSalariesListByStartDateAndEndDateAndEmployeeId(
                startDateEditText.getText().toString(), endDateEditText.getText().toString(), selectedEmployeeId));

//        SalaryRepository.sortSalariesListByEventStartDate(resultSalaries);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void showResult() {
        int size = queryResultSalaries.size();
        if (size > 0) {
            numberOfEventsTextView.setText(String.format(getResources().getString(R.string.salary_num_of_events), size, size));
        } else {
            numberOfEventsTextView.setText(getResources().getString(R.string.salary_no_salaries));
        }

        boolean isEditedSalariesPaid = true;
        int sum, paid = 0, unpaid = 0;

        for (Salary s : queryResultSalaries) {
            if (s.isPaid()) {
                paid += s.getSalary();
            } else {
                unpaid += s.getSalary();
                isEditedSalariesPaid = false;
            }
        }

        SalaryRepository.getInstance().sortSalariesListByEventStartDate(queryResultSalaries);

        calculateSalaryAdapter.notifyDataSetChanged();

        sum = paid + unpaid;

        sumEditText.setText(String.valueOf(sum));
        paidEditText.setText(String.valueOf(paid));
        unpaidEditText.setText(String.valueOf(unpaid));

        saveButton.setEnabled(!isEditedSalariesPaid);
        payAllButton.setEnabled(!isEditedSalariesPaid);
    }

    private void confirmBeforeSaving(boolean payAll) {
        int paid = 0, unpaid = 0;
        for (Salary s : queryResultSalaries) {
            if (!s.isPaid()) {
                if (payAll || s.isChecked()) {
                    paid += s.getEditedSalary();
                } else {
                    unpaid += s.getEditedSalary();
                }
            }
        }
        String title = payAll ? "Bạn có chắc chắn muốn thanh toán tất cả?" : "Bạn có chắc chắn muốn lưu thay đổi?";
        String message = "Sẽ trả: " + paid + "\n" + getResources().getString(R.string.unpaid) + ": " + unpaid;
        String positiveButton = payAll ? "Thanh toán tất cả" : "Lưu";

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_error)
                .setPositiveButton(positiveButton, (dialog, whichButton) -> saveChanges(payAll))
                .setNegativeButton("Hủy", (dialog, which) -> {
                    sumTextView.requestFocus();
                }).show();
    }

    private void saveChanges(boolean payAll) {

        for (Salary s : queryResultSalaries) {
            s.setSalary(s.getEditedSalary());
            s.setPaid(payAll || s.isChecked());
        }

        SalaryRepository.getInstance().updateSalaries(queryResultSalaries);
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
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_save)
                .setTitle("Chuyển đến Chi tiết sự kiện")
                .setMessage("Lưu thông tin đã nhập?")
                .setPositiveButton("Lưu", (dialogInterface, i) -> {
                    saveChanges(false);
                    Intent intent = new Intent(context, ViewEventActivity.class);
                    intent.putExtra("eventId", eventId);
                    startActivity(intent);
                })
                .setNeutralButton("Không lưu", (dialogInterface, i) -> {
                    Intent intent = new Intent(context, ViewEventActivity.class);
                    intent.putExtra("eventId", eventId);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onCalculateSalaryInputLayoutLongClicked(String salaryId) {
        SalaryRepository.getInstance().revertToNotPaid(salaryId);

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
