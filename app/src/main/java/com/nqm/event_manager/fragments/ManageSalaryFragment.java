package com.nqm.event_manager.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.ViewEventActivity;
import com.nqm.event_manager.adapters.CalculateSalaryAdapter;
import com.nqm.event_manager.adapters.ViewSalaryHistoryAdapter;
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
import java.util.Objects;

@SuppressLint("NotifyDataSetChanged")
public class ManageSalaryFragment extends Fragment implements IOnCalculateSalaryItemClicked,
        IOnDataLoadComplete, IOnCustomDatePickerItemClicked {

    Button calculateButton, payAllButton, saveButton;
    RecyclerView resultRecyclerView;
    Spinner selectEmployeeSpinner;
    CheckBox paidOnlyCheckBox, zeroOnlyCheckBox;
    TextView startDateTextView, endDateTextView, sumTextView, paidTextView, unpaidTextView,
            numberOfEventsTextView, employeeNumOfEventsTextView;

    Calendar calendar = Calendar.getInstance();

    Dialog datePickerDialog;
    TextView datePickerDialogDateTextView;
    CustomDatePicker datePicker;
    Button datePickerTodayButton, datePickerDialogOkButton, datePickerDialogCancelButton;
    String clickedEditText;
    String selectedDateText;

    Dialog salaryHistoryDialog;
    RecyclerView salaryHistoryRecyclerView;
    TextView salaryHistoryEmptyTextView;
    ViewSalaryHistoryAdapter salaryHistoryAdapter;

    List<String> employeesIds;
    List<String> employeesInfo;
    List<Salary> queryResultSalaries;
    List<String> queryResultEventsIds;
    List<Salary> selectedEmployeeSalaries;
    List<Salary> showedEmployeeSalaries;
    List<Salary> unPaidSalaries;
    List<Salary> zeroSalaries;

    ArrayAdapter<String> employeesSpinnerAdapter;
    CalculateSalaryAdapter calculateSalaryAdapter;

    String selectedEmployeeId = "";

    Activity context;

    public ManageSalaryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.manage_salaries_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_salary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getActivity();
        connectViews(view);
        addEvents();

        EventRepository.getInstance().setListener(this);
        EmployeeRepository.getInstance().setCommonListener(this);
        SalaryRepository.getInstance().setListener(this);
        ScheduleRepository.getInstance().setListener(this);

        employeesIds = new ArrayList<>();
        employeesInfo = new ArrayList<>();
        queryResultSalaries = new ArrayList<>();
        queryResultEventsIds = new ArrayList<>();
        selectedEmployeeSalaries = new ArrayList<>();
        showedEmployeeSalaries = new ArrayList<>();
        unPaidSalaries = new ArrayList<>();
        zeroSalaries = new ArrayList<>();

        employeesSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, employeesInfo);
        employeesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        selectEmployeeSpinner.setAdapter(employeesSpinnerAdapter);

        calculateSalaryAdapter = new CalculateSalaryAdapter(showedEmployeeSalaries);
        calculateSalaryAdapter.setListener(this);
        resultRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        resultRecyclerView.setAdapter(calculateSalaryAdapter);

        startDateTextView.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
        endDateTextView.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));

        initDatePickerDialog();
        initSalaryHistoryDialog();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.manage_salaries_history_action) {
            showSalaryHistory();
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectViews(View view) {
        calculateButton = view.findViewById(R.id.fragment_manage_salary_calculate_button);
        payAllButton = view.findViewById(R.id.fragment_manage_salary_pay_all_button);
        saveButton = view.findViewById(R.id.fragment_manage_salary_save_button);

        resultRecyclerView = view.findViewById(R.id.fragment_manage_salary_result_list_view);

        selectEmployeeSpinner = view.findViewById(R.id.fragment_manage_salary_employee_spinner);

        sumTextView = view.findViewById(R.id.fragment_manage_salary_sum_edit_text);
        paidTextView = view.findViewById(R.id.fragment_manage_salary_paid_edit_text);
        unpaidTextView = view.findViewById(R.id.fragment_manage_salary_unpaid_edit_text);
        startDateTextView = view.findViewById(R.id.fragment_manage_salary_start_date_edit_text);
        endDateTextView = view.findViewById(R.id.fragment_manage_salary_end_date_edit_text);

        paidOnlyCheckBox = view.findViewById(R.id.fragment_manage_salary_paid_checkbox);
        zeroOnlyCheckBox = view.findViewById(R.id.fragment_manage_salary_zero_checkbox);

        numberOfEventsTextView = view.findViewById(R.id.fragment_manage_salary_number_of_events_text_view);
        employeeNumOfEventsTextView = view.findViewById((R.id.fragment_manage_salary_employee_number_of_events_text_view));
    }

    private void initDatePickerDialog() {
        datePickerDialog = new Dialog(context);
        datePickerDialog.setContentView(R.layout.dialog_custom_date_picker);

        datePickerDialogDateTextView = datePickerDialog.findViewById(R.id.custom_date_picker_dialog_date_text_view);
        datePicker = datePickerDialog.findViewById(R.id.custom_date_picker_calendar_view);
        datePickerTodayButton = datePickerDialog.findViewById(R.id.custom_date_picker_today_button);
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

        datePickerTodayButton.setOnClickListener(v -> datePicker.setViewDate(Calendar.getInstance().getTime()));

        datePickerDialogCancelButton.setOnClickListener(v -> datePickerDialog.dismiss());

        datePickerDialogOkButton.setOnClickListener(v -> {
            selectedDateText = datePicker.getSelectedDateString();
            try {
                Date selectedDate = CalendarUtil.sdfDayMonthYear.parse(selectedDateText);
                Date startDate = CalendarUtil.sdfDayMonthYear.parse(startDateTextView.getText().toString());
                Date endDate = CalendarUtil.sdfDayMonthYear.parse(endDateTextView.getText().toString());

                if (clickedEditText.equals(StringUtil.startDateEditText)) {
                    // startDate clicked
                    if (!Objects.requireNonNull(selectedDate).before(endDate)) {
                        // Set startDate = selectedDate, endDate = last day_of_month of startDate
                        calendar.setTime(selectedDate);
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        endDateTextView.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    }
                    startDateTextView.setText(selectedDateText);
                } else {
                    // endDate clicked
                    if (!Objects.requireNonNull(selectedDate).after(startDate)) {
                        // Set endDate = selectedDate, startDate = first day_of_month of endDate
                        calendar.setTime(selectedDate);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        startDateTextView.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    }
                    endDateTextView.setText(selectedDateText);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            datePickerDialog.dismiss();
        });
    }

    private void initSalaryHistoryDialog() {
        salaryHistoryDialog = new Dialog(context);
        salaryHistoryDialog.setContentView(R.layout.dialog_salary_history);
        salaryHistoryRecyclerView = salaryHistoryDialog.findViewById(R.id.salary_history_recycler_view);
        salaryHistoryEmptyTextView = salaryHistoryDialog.findViewById(R.id.salary_history_empty_text_view);
        salaryHistoryAdapter = new ViewSalaryHistoryAdapter();
        salaryHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        salaryHistoryRecyclerView.setAdapter(salaryHistoryAdapter);
        salaryHistoryRecyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
    }

    private void addEvents() {
        startDateTextView.setOnClickListener(v -> {
            clickedEditText = StringUtil.startDateEditText;
            selectedDateText = startDateTextView.getText().toString();
            showDatePickerDialog();
        });

        endDateTextView.setOnClickListener(v -> {
            clickedEditText = StringUtil.endDateEditText;
            selectedDateText = endDateTextView.getText().toString();
            showDatePickerDialog();
        });

        calculateButton.setOnClickListener(v -> {
            getResultSalaries();
            updateEmployeesSpinner();
            showResult();
        });

        paidOnlyCheckBox.setOnClickListener(v -> updateSalaryList());
        zeroOnlyCheckBox.setOnClickListener(v -> updateSalaryList());

        selectEmployeeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEmployeeId = employeesIds.get(selectEmployeeSpinner.getSelectedItemPosition());
                showResult();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        saveButton.setOnClickListener(v -> confirmBeforeSaving(false));

        payAllButton.setOnClickListener(v -> confirmBeforeSaving(true));
    }

    private void showDatePickerDialog() {
        try {
            Date dateFromEditText = CalendarUtil.sdfDayMonthYear.parse(selectedDateText);
            datePicker.setSelectedDate(dateFromEditText);
            datePicker.setViewDate(dateFromEditText);
            String txt = CalendarUtil.sdfDayOfWeek.format(Objects.requireNonNull(dateFromEditText)) +
                    " - " + CalendarUtil.sdfDayMonthYear.format(dateFromEditText);
            datePickerDialogDateTextView.setText(txt);
            datePickerDialog.show();
            if (datePickerDialog.getWindow() != null) {
                datePickerDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
            }
        } catch (Exception e) {
            System.out.println(Log.getStackTraceString(e));
        }
    }

    private void getResultSalaries() {
        queryResultSalaries.clear();
        queryResultSalaries.addAll(SalaryRepository.getInstance().getSalariesListByStartDateEndDate(
                startDateTextView.getText().toString(), endDateTextView.getText().toString()));

        queryResultEventsIds.clear();
        for (Salary s : queryResultSalaries) {
            if (!queryResultEventsIds.contains(s.getEventId())) {
                queryResultEventsIds.add(s.getEventId());
            }
        }

//        SalaryRepository.sortSalariesListByEventStartDate(resultSalaries);
    }

    private void updateEmployeesSpinner() {
        int defaultEmployeeIndex = 0;
        if (queryResultSalaries.size() > 0) {
            employeesIds.clear();
            for (Salary s : queryResultSalaries) {
                String employeeId = s.getEmployeeId();
                if (!employeesIds.contains(employeeId)) {
                    employeesIds.add(employeeId);
                }
            }
            employeesInfo.clear();
            for (int i = 0; i < employeesIds.size(); i++) {
                Employee e = EmployeeRepository.getInstance().getAllEmployees().get(employeesIds.get(i));
                if (e != null) {
                    employeesInfo.add(e.getHoTen() + " - " + e.getChuyenMon());
                    if (e.getHoTen().equals(Constants.DEFAULT_SALARY_EMPLOYEE_VALUE)) {
                        defaultEmployeeIndex = i;
                    }
                }
            }
            employeesSpinnerAdapter.notifyDataSetChanged();

            if (selectedEmployeeId.isEmpty()) {
                selectEmployeeSpinner.setSelection(defaultEmployeeIndex);
                selectedEmployeeId = employeesIds.get(selectEmployeeSpinner.getSelectedItemPosition());
            } else {
                int position = employeesIds.indexOf(selectedEmployeeId);
                if (position == -1) {
                    selectEmployeeSpinner.setSelection(defaultEmployeeIndex);
                    selectedEmployeeId = employeesIds.get(selectEmployeeSpinner.getSelectedItemPosition());
                } else {
                    selectEmployeeSpinner.setSelection(position);
                }
            }
        }
    }

    public void showResult() {

        if (queryResultSalaries.size() > 0) {
            numberOfEventsTextView.setText(String.format(getResources().getString(R.string.salary_num_of_events),
                    queryResultSalaries.size(), queryResultEventsIds.size()));
        } else {
            numberOfEventsTextView.setText(getResources().getString(R.string.salary_no_salaries));
        }

        boolean isEditedSalariesPaid = true;
        int sum, paid = 0, unpaid = 0;

        selectedEmployeeSalaries.clear();
        for (Salary s : queryResultSalaries) {
            if (s.getEmployeeId().equals(selectedEmployeeId)) {
                Salary clonedSalary = new Salary(s);
                selectedEmployeeSalaries.add(clonedSalary);
                if (s.isPaid()) {
                    paid += s.getSalary();
                } else {
                    unpaid += s.getSalary();
                    isEditedSalariesPaid = false;
                }
            }
        }

        updateSalaryList();

        employeeNumOfEventsTextView.setText(String.format(getResources().getString(R.string.num_of_events),
                selectedEmployeeSalaries.size()));
        sum = paid + unpaid;
        sumTextView.setText(String.valueOf(sum));
        paidTextView.setText(String.valueOf(paid));
        unpaidTextView.setText(String.valueOf(unpaid));
        saveButton.setEnabled(!isEditedSalariesPaid);
        payAllButton.setEnabled(!isEditedSalariesPaid);
    }

    private void updateSalaryList() {
        showedEmployeeSalaries.clear();

        boolean showUnPaidChecked = paidOnlyCheckBox.isChecked();
        boolean showZeroChecked = zeroOnlyCheckBox.isChecked();

        for (Salary s : selectedEmployeeSalaries) {
            boolean satisfied = (!showUnPaidChecked && !showZeroChecked) || // show all
                    ((showUnPaidChecked && !s.isPaid()) && !showZeroChecked) || // show only unPaid
                    ((showZeroChecked && (s.getSalary() == 0)) && !showUnPaidChecked) || // show only zero
                    ((showUnPaidChecked && !s.isPaid()) && (showZeroChecked && (s.getSalary() == 0))); // show only unpaid and zero;
            if (satisfied) {
                showedEmployeeSalaries.add(s);
            }
        }

        SalaryRepository.getInstance().sortSalariesListByEventStartDate(showedEmployeeSalaries);
        calculateSalaryAdapter.notifyDataSetChanged();
    }

    private void confirmBeforeSaving(boolean payAll) {
        int paid = 0, unpaid = 0;
        for (Salary s : selectedEmployeeSalaries) {
            if (!s.isPaid()) {
                if (payAll || s.isChecked()) {
                    paid += s.getEditedSalary();
                } else {
                    unpaid += s.getEditedSalary();
                }
            }
        }
        String title = payAll ? "Bạn có chắc chắn muốn thanh toán tất cả?" : "Bạn có chắc chắn muốn lưu thay đổi?";
        String message = "Sẽ trả: " + paid + "\n" + getResources().getString(R.string.unpaid_left) + ": " + unpaid;
        String positiveButton = payAll ? "Thanh toán tất cả" : "Lưu";

        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_error)
                .setPositiveButton(positiveButton, (dialog, whichButton) -> saveChanges(payAll))
                .setNegativeButton("Hủy", (dialog, which) -> sumTextView.requestFocus()).show();
    }

    private void saveChanges(boolean payAll) {

        for (Salary s : selectedEmployeeSalaries) {
            if (!s.isPaid()) {
                s.setSalary(s.getEditedSalary());
                s.setPaid(payAll || s.isChecked());
            }
        }

        SalaryRepository.getInstance().updateSalaries(selectedEmployeeSalaries);
        showResult();
    }

    private void showSalaryHistory() {
        if (salaryHistoryAdapter.isHistoryEmpty()) {
            salaryHistoryEmptyTextView.setVisibility(View.VISIBLE);
            salaryHistoryRecyclerView.setVisibility(View.GONE);
        } else {
            salaryHistoryEmptyTextView.setVisibility(View.GONE);
            salaryHistoryRecyclerView.setVisibility(View.VISIBLE);
        }

        salaryHistoryDialog.show();
        if (salaryHistoryDialog.getWindow() != null) {
            salaryHistoryDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onResume() {
        EventRepository.getInstance().setListener(this);
        EmployeeRepository.getInstance().setCommonListener(this);
        SalaryRepository.getInstance().setListener(this);
        ScheduleRepository.getInstance().setListener(this);
        getResultSalaries();
        updateEmployeesSpinner();
        showResult();
        super.onResume();
    }

    @Override
    public void onCalculateSalaryItemClicked(String eventId) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_save)
                .setTitle("Chuyển đến Chi tiết sự kiện")
                .setMessage("Lưu thông tin đã thay đổi?")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    saveChanges(false);
                    Intent intent = new Intent(context, ViewEventActivity.class);
                    intent.putExtra("eventId", eventId);
                    startActivity(intent);
                })
                .setNegativeButton("Không lưu", (dialogInterface, i) -> {
                    Intent intent = new Intent(context, ViewEventActivity.class);
                    intent.putExtra("eventId", eventId);
                    startActivity(intent);
                })
                .setNeutralButton("Hủy", null)
                .show();
    }

    @Override
    public void onCalculateSalaryInputLayoutLongClicked(String salaryId) {
        SalaryRepository.getInstance().revertToNotPaid(salaryId);
    }

    @Override
    public void notifyOnLoadComplete() {
        getResultSalaries();
        updateEmployeesSpinner();
        showResult();
    }

    @Override
    public void onCustomDatePickerItemClicked(String selectedDate, String dayOfWeek) {
        datePickerDialogDateTextView.setText(String.format(Locale.US, "%s - %s", dayOfWeek, selectedDate));
    }

}
