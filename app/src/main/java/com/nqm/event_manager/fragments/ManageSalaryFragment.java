package com.nqm.event_manager.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.ViewEventActivity;
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
import com.nqm.event_manager.utils.StringUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageSalaryFragment extends Fragment implements IOnCalculateSalaryItemClicked,
        IOnDataLoadComplete, IOnCustomDatePickerItemClicked {

    Button calculateButton, payAllButton, saveButton;
    RecyclerView resultRecyclerView;
    EditText startDateEditText, endDateEditText, sumEditText, paidEditText, unpaidEditText;
    Spinner selectEmployeeSpinner;
    TextView numberOfEventsTextView, sumTextView, employeeNumOfEventsTextView;

    Calendar calendar = Calendar.getInstance();

    Dialog datePickerDialog;
    TextView datePickerDialogDateTextView;
    CustomDatePicker datePicker;
    Button datePickerTodayButton, datePickerDialogOkButton, datePickerDialogCancelButton;
    String clickedEditText;
    String selectedDateText;

    List<String> employeesIds;
    List<String> employeesInfo;
    List<Salary> queryResultSalaries;
    List<String> queryResultEventsIds;
    List<Salary> selectedEmployeeSalaries;

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
        queryResultSalaries = new ArrayList<>();
        queryResultEventsIds = new ArrayList<>();
        selectedEmployeeSalaries = new ArrayList<>();

        employeesSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, employeesInfo);
        employeesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        selectEmployeeSpinner.setAdapter(employeesSpinnerAdapter);

        calculateSalaryAdapter = new CalculateSalaryAdapter(selectedEmployeeSalaries);
        calculateSalaryAdapter.setListener(this);
        resultRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        resultRecyclerView.setAdapter(calculateSalaryAdapter);

        startDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
        endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));

        initDatePickerDialog();
    }

    private void connectViews(View view) {
        calculateButton = view.findViewById(R.id.fragment_manage_salary_calculate_button);
        payAllButton = view.findViewById(R.id.fragment_manage_salary_pay_all_button);
        saveButton = view.findViewById(R.id.fragment_manage_salary_save_button);

        resultRecyclerView = view.findViewById(R.id.fragment_manage_salary_result_list_view);

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

        datePickerTodayButton.setOnClickListener(v -> {
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

        saveButton.setOnClickListener(v -> confirmBeforeSaving(false));

        payAllButton.setOnClickListener(v -> confirmBeforeSaving(true));
    }

    private void showDatePickerDialog() {
        try {
            Date dateFromEditText = CalendarUtil.sdfDayMonthYear.parse(selectedDateText);
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
        queryResultSalaries.clear();
        queryResultSalaries.addAll(SalaryRepository.getInstance().getSalariesListByStartDateEndDate(
                startDateEditText.getText().toString(), endDateEditText.getText().toString()));

        queryResultEventsIds.clear();
        for (Salary s : queryResultSalaries) {
            if (!queryResultEventsIds.contains(s.getEventId())) {
                queryResultEventsIds.add(s.getEventId());
            }
        }

//        SalaryRepository.sortSalariesListByEventStartDate(resultSalaries);
    }

    private void updateEmployeesSpinner() {
        if (queryResultSalaries.size() > 0) {
            employeesIds.clear();
            for (Salary s : queryResultSalaries) {
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

    @SuppressLint("NotifyDataSetChanged")
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
                selectedEmployeeSalaries.add(new Salary(s));
                if (s.isPaid()) {
                    paid += s.getSalary();
                } else {
                    unpaid += s.getSalary();
                    isEditedSalariesPaid = false;
                }
            }
        }

        SalaryRepository.getInstance().sortSalariesListByEventStartDate(selectedEmployeeSalaries);

        calculateSalaryAdapter.notifyDataSetChanged();

        employeeNumOfEventsTextView.setText(String.format(getResources().getString(R.string.num_of_events),
                selectedEmployeeSalaries.size()));

        sum = paid + unpaid;

        sumEditText.setText(String.valueOf(sum));
        paidEditText.setText(String.valueOf(paid));
        unpaidEditText.setText(String.valueOf(unpaid));

        saveButton.setEnabled(!isEditedSalariesPaid);
        payAllButton.setEnabled(!isEditedSalariesPaid);
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
        String message = "Sẽ trả: " + paid + "\n" + getResources().getString(R.string.unpaid) + ": " + unpaid;
        String positiveButton = payAll ? "Thanh toán tất cả" : "Lưu";

        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_error)
                .setPositiveButton(positiveButton, (dialog, whichButton) -> saveChanges(payAll))
                .setNegativeButton("Hủy", (dialog, which) -> {
                    sumTextView.requestFocus();
                }).show();
    }

    private void saveChanges(boolean payAll) {

        for (Salary s : selectedEmployeeSalaries) {
            s.setSalary(s.getEditedSalary());
            s.setPaid(payAll || s.isChecked());
        }

        SalaryRepository.getInstance().updateSalaries(selectedEmployeeSalaries);
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
                .setTitle("Chuyển đến Chi tiết sự kiện...")
                .setMessage("Dữ liệu vừa nhập sẽ không được lưu.\nLưu thông tin đã nhập?")
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
    public void notifyOnLoadCompleteWithContext(Context context) {
        Toast.makeText(context, "ManageSalaryFragment: wrong notifyOnLoadComplete()",
                Toast.LENGTH_SHORT).show();
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
