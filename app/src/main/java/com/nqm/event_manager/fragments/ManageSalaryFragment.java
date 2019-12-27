package com.nqm.event_manager.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.ViewEventActivity;
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ManageSalaryFragment extends Fragment implements IOnCalculateSalaryItemClicked,
        IOnDataLoadComplete, IOnCustomDatePickerItemClicked {

    Button calculateButton, payAllButton, saveButton;
    CustomListView resultListView;
    EditText startDateEditText, endDateEditText, sumEditText, paidEditText, unpaidEditText;
    Spinner selectEmployeeSpinner;
    TextView numberOfEventsTextView, sumTextView, employeeNumOfEventsTextView;

    Calendar calendar = Calendar.getInstance();

    Dialog datePickerDialog;
    TextView datePickerDialogDateTextView;
    CustomDatePicker datePicker;
    Button datePickerDialogOkButton, datePickerDialogCancelButton;
    EditText selectedDateEditText;

    ArrayList<String> employeesIds;
    ArrayList<String> employeesInfo;
    ArrayList<Salary> resultSalaries;
    ArrayList<Salary> selectedSalaries;
    int resultEventsSize;

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
        EmployeeRepository.getInstance().setListener(this);
        SalaryRepository.getInstance().setListener(this);
        ScheduleRepository.getInstance().setListener(this);

        employeesIds = new ArrayList<>();
        employeesInfo = new ArrayList<>();
        resultSalaries = new ArrayList<>();
        selectedSalaries = new ArrayList<>();

        employeesSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, employeesInfo);
        employeesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        selectEmployeeSpinner.setAdapter(employeesSpinnerAdapter);

        calculateSalaryAdapter = new CalculateSalaryAdapter(context, selectedSalaries);
        calculateSalaryAdapter.setListener(this);
        resultListView.setAdapter(calculateSalaryAdapter);

        startDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
        endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));

        initDatePickerDialog();
    }

    private void connectViews(View view) {
        calculateButton = view.findViewById(R.id.fragment_manage_salary_calculate_button);
        payAllButton = view.findViewById(R.id.fragment_manage_salary_pay_all_button);
        saveButton = view.findViewById(R.id.fragment_manage_salary_save_button);

        resultListView = view.findViewById(R.id.fragment_manage_salary_result_list_view);

        selectEmployeeSpinner = view.findViewById(R.id.fragment_manage_salary_employee_spinner);

        sumEditText = view.findViewById(R.id.fragment_manage_salary_sum_edit_text);
        paidEditText = view.findViewById(R.id.fragment_manage_salary_paid_edit_text);
        unpaidEditText = view.findViewById(R.id.fragment_manage_salary_unpaid_edit_text);
        startDateEditText = view.findViewById(R.id.fragment_manage_salary_start_date_edit_text);
        endDateEditText = view.findViewById(R.id.fragment_manage_salary_end_date_edit_text);

        numberOfEventsTextView = view.findViewById(R.id.fragment_manage_salary_number_of_events_text_view);
        sumTextView = view.findViewById(R.id.fragment_manage_salary_sum_text_view);
        employeeNumOfEventsTextView = view.findViewById((R.id.fragment_manage_salary_employee_number_of_events_text_view));
    }

    private void initDatePickerDialog() {
        datePickerDialog = new Dialog(context);
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

//                if (startDate.compareTo(endDate) > 0) {
//                    if (selectedDateEditText == startDateEditText) {
//                        endDateEditText.setText(selectedDate);
//                    } else {
//                        startDateEditText.setText(selectedDate);
//                    }
//                }

                if (selectedDateEditText == startDateEditText) {
                    calendar.setTime(startDate);
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                } else {
                    if (endDate.before(startDate)) {
                        calendar.setTime(endDate);
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        startDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
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
            updateEmployeesSpinner();
            showResult();
        });

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

        saveButton.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Bạn có chắn chắn muốn lưu thay đổi?")
                .setIcon(R.drawable.ic_error)
                .setPositiveButton("Có", (dialog, whichButton) -> saveChanges(false))
                .setNegativeButton("Không", (dialog, which) -> {
                    sumTextView.requestFocus();
                    calculateSalaryAdapter.notifyDataSetChanged();
                }).show());

        payAllButton.setOnClickListener(v -> new AlertDialog.Builder(getContext())
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
            datePicker.setSelectedDate(dateFromEditText);
            datePicker.setViewDate(dateFromEditText);
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
        resultSalaries.clear();
        resultSalaries.addAll(SalaryRepository.getInstance()
                .getSalariesByStartDateEndDate(startDateEditText.getText().toString(),
                        endDateEditText.getText().toString()));
        ArrayList<String> eventsIds = new ArrayList<>();
        for (Salary s : resultSalaries) {
            if (!eventsIds.contains(s.getEventId())) {
                eventsIds.add(s.getEventId());
            }
        }
        resultEventsSize = eventsIds.size();
        SalaryRepository.sortSalariesByEventStartDate(resultSalaries);
    }

    private void updateEmployeesSpinner() {
        if (resultSalaries.size() > 0) {
            employeesIds.clear();
            for (Salary s : resultSalaries) {
                String employeeId = s.getEmployeeId();
                if (!employeesIds.contains(employeeId)) {
                    employeesIds.add(employeeId);
                }
            }
            employeesInfo.clear();
            for (String employeeId : employeesIds) {
                Employee e = EmployeeRepository.getInstance().getAllEmployees().get(employeeId);
                if (e != null) {
                    employeesInfo.add(e.getHoTen() + " - " + e.getChuyenMon());
                }
            }
            employeesSpinnerAdapter.notifyDataSetChanged();

            if (selectedEmployeeId.isEmpty()) {
                selectEmployeeSpinner.setSelection(0);
                selectedEmployeeId = employeesIds.get(selectEmployeeSpinner.getSelectedItemPosition());
            } else {
                int position = employeesIds.indexOf(selectedEmployeeId);
                if (position == -1) {
                    selectEmployeeSpinner.setSelection(0);
                    selectedEmployeeId = employeesIds.get(selectEmployeeSpinner.getSelectedItemPosition());
                } else {
                    selectEmployeeSpinner.setSelection(position);
                }
            }
        }
    }

    public void showResult() {
        if (resultSalaries.size() > 0) {
            numberOfEventsTextView.setText(String.format(getResources().getString(R.string.salary_num_of_events),
                    resultSalaries.size(), resultEventsSize));
        } else {
            numberOfEventsTextView.setText(getResources().getString(R.string.salary_no_salaries));
        }

        calculateSalaryAdapter.notifyDataSetChanged(selectedSalaries);

        boolean allSelectedSalariesPaid = true;
        int sum, paid = 0, unpaid = 0;

        selectedSalaries.clear();
        for (Salary s : resultSalaries) {
            if (s.getEmployeeId().equals(selectedEmployeeId)) {
                selectedSalaries.add(s);
                if (s.isPaid()) {
                    paid += s.getSalary();
                } else {
                    unpaid += s.getSalary();
                    allSelectedSalariesPaid = false;
                }
            }
        }
        employeeNumOfEventsTextView.setText(String.valueOf(selectedSalaries.size()));
        sum = paid + unpaid;

        sumEditText.setText(String.valueOf(sum));
        paidEditText.setText(String.valueOf(paid));
        unpaidEditText.setText(String.valueOf(unpaid));

        saveButton.setEnabled(!allSelectedSalariesPaid);
        payAllButton.setEnabled(!allSelectedSalariesPaid);
    }

    private void saveChanges(boolean payAll) {
        for (int i = 0; i < resultListView.getChildCount(); i++) {
            EditText salaryEditText = resultListView.getChildAt(i)
                    .findViewById(R.id.calculate_salaries_list_item_salary_edit_text);
            CheckBox isPaidCheckBox = resultListView.getChildAt(i)
                    .findViewById(R.id.calculate_salaries_list_item_paid_checkbox);

            if (salaryEditText.getText().toString().equals("")) {
                selectedSalaries.get(i).setSalary(0);
            } else {
                selectedSalaries.get(i).setSalary(Integer.parseInt(salaryEditText.getText().toString()));
            }

            if (isPaidCheckBox.isChecked() || payAll) {
                selectedSalaries.get(i).setPaid(true);
            } else {
                selectedSalaries.get(i).setPaid(false);
            }
        }

        SalaryRepository.getInstance().updateSalaries(selectedSalaries);
        showResult();
    }

    @Override
    public void onResume() {
        EventRepository.getInstance().setListener(this);
        EmployeeRepository.getInstance().setListener(this);
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
                .setTitle("Lưu thông tin đã nhập?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveChanges(false);
                        Intent intent = new Intent(getContext(), ViewEventActivity.class);
                        intent.putExtra("eventId", eventId);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getContext(), ViewEventActivity.class);
                        intent.putExtra("eventId", eventId);
                        startActivity(intent);
                    }
                })
                .show();
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
