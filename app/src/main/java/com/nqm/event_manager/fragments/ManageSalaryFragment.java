package com.nqm.event_manager.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.ViewEventActivity;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ManageSalaryFragment extends Fragment implements IOnCalculateSalaryItemClicked,
        IOnDataLoadComplete {

    Button calculateButton, payAllButton, saveButton;
    CustomListView resultListView;
    EditText startDateEditText, endDateEditText, sumEditText, paidEditText, unpaidEditText;
    Spinner selectEmployeeSpinner;
    TextView numberOfEventsTextView, sumTextView;

    DatePickerDialog.OnDateSetListener dateSetListener;
    Calendar calendar = Calendar.getInstance();
    View currentView;

    ArrayList<String> employeesIds;
    ArrayList<String> employeesInfo;
    ArrayList<Salary> resultSalaries;
    ArrayList<Salary> selectedSalaries;
    int resultEventSize;

    ArrayAdapter<String> employeesSpinnerAdapter;
    CalculateSalaryAdapter calculateSalaryAdapter;

    String startDate, endDate;
    String selectedEmployeeId = "";

    Resources res;
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
        return inflater.inflate(R.layout.fragment_calculate_salary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        res = getResources();
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

        calculateSalaryAdapter = new CalculateSalaryAdapter(getActivity(), selectedSalaries);
        calculateSalaryAdapter.setListener(this);
        resultListView.setAdapter(calculateSalaryAdapter);

        startDate = CalendarUtil.sdfDayMonthYear.format(calendar.getTime());
        endDate = CalendarUtil.sdfDayMonthYear.format(calendar.getTime());
        startDateEditText.setText(startDate);
        endDateEditText.setText(endDate);

    }

    private void connectViews(View view) {
        calculateButton = view.findViewById(R.id.calculate_salaries_calculate_button);
        payAllButton = view.findViewById(R.id.calculate_salaries_pay_all_button);
        saveButton = view.findViewById(R.id.calculate_salaries_save_button);

        resultListView = view.findViewById(R.id.calculate_salaries_result_list_view);

        selectEmployeeSpinner = view.findViewById(R.id.calculate_salaries_employee_spinner);

        sumEditText = view.findViewById(R.id.calculate_salaries_sum_edit_text);
        paidEditText = view.findViewById(R.id.calculate_salaries_paid_edit_text);
        unpaidEditText = view.findViewById(R.id.calculate_salaries_unpaid_edit_text);
        startDateEditText = view.findViewById(R.id.calculate_salaries_start_date_edit_text);
        endDateEditText = view.findViewById(R.id.calculate_salaries_end_date_edit_text);

        numberOfEventsTextView = view.findViewById(R.id.calculate_salaries_number_of_events_text_view);
        sumTextView = view.findViewById(R.id.calculate_salary_sum_text_view);
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

                //Make sure start date <= end date
                try {
                    Date startDate = CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString());
                    Date endDate = CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString());

                    if (startDate.compareTo(endDate) > 0) {
                        if (currentView == startDateEditText) {
                            endDateEditText.setText(startDateEditText.getText().toString());
                        } else {
                            startDateEditText.setText(endDateEditText.getText().toString());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, dateSetListener, y, m, d);
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, dateSetListener, y, m, d);
                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
                datePickerDialog.show();
            }
        });

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startDateEditText.getText().toString().isEmpty()) {
                    startDateEditText.setError("Xin mời nhập");
                    return;
                } else {
                    startDateEditText.setError(null);
                }
                if (endDateEditText.getText().toString().isEmpty()) {
                    endDateEditText.setError("");
                    return;
                } else {
                    endDateEditText.setError(null);
                }

                getResultSalaries();
                updateEmployeesSpinner();
                showResult();
            }
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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
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
                                sumTextView.requestFocus();
                                Log.d("debug", "no button clicked");
                                calculateSalaryAdapter.notifyDataSetChanged();
                            }
                        }).show();
            }
        });

        payAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
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
                                Log.d("debug", "no button clicked");
                                calculateSalaryAdapter.notifyDataSetChanged();
                            }
                        }).show();
            }
        });
    }

    private void getResultSalaries() {
//        SalaryRepository.getInstance().getSalariesByStartDateEndDate(startDate, endDate,
//                new SalaryRepository.MySalaryQueryCallback() {
//                    @Override
//                    public void onCallback(ArrayList<String> salariesIds, ArrayList<String> eventIds) {
//                        resultSalariesIds.clear();
//                        resultSalariesIds.addAll(salariesIds);
//                        resultEventsIds.clear();
//                        resultEventsIds.addAll(eventIds);
//                    }
//                });
        resultSalaries.clear();
        resultSalaries.addAll(SalaryRepository.getInstance().getSalariesByStartDateEndDate(startDate, endDate));
        ArrayList<String> eventsIds = new ArrayList<>();
        for (Salary s : resultSalaries) {
            if (!eventsIds.contains(s.getEventId())) {
                eventsIds.add(s.getEventId());
            }
        }
        resultEventSize = eventsIds.size();
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
                Employee e = EmployeeRepository.getInstance(null).getAllEmployees().get(employeeId);
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
        numberOfEventsTextView.setText(String.format(getResources().getString(R.string.salary_num_of_events), resultEventSize));

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
        sum = paid + unpaid;

        sumEditText.setText(String.format(res.getString(R.string.number), sum));
        paidEditText.setText(String.format(res.getString(R.string.number), paid));
        unpaidEditText.setText(String.format(res.getString(R.string.number), unpaid));

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
        Intent intent = new Intent(getContext(), ViewEventActivity.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    @Override
    public void notifyOnLoadComplete() {
        getResultSalaries();
        updateEmployeesSpinner();
        showResult();
    }
}
