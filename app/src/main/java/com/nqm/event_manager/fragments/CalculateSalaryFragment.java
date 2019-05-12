package com.nqm.event_manager.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.nqm.event_manager.adapters.CalculateSalaryAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class CalculateSalaryFragment extends Fragment {

    Button calculateButton, payAllButton, saveButton;
    CustomListView resultListView;
    EditText startDateEditText, endDateEditText, sumEditText, paidEditText, unpaidEditText;
    Spinner selectEmployeeSpinner;
    TextView numberOfEventsTextView;

    DatePickerDialog.OnDateSetListener dateSetListener;
    Calendar calendar = Calendar.getInstance();
    View currentView;

    ArrayList<String> employeesIds;
    ArrayList<String> employeesInfo;
    ArrayList<String> resultSalariesIds;
    ArrayList<String> selectedSalariesIds;

    ArrayAdapter<String> employeesSpinnerAdapter;
    CalculateSalaryAdapter calculateSalaryAdapter;

    String startDate, endDate;
    String selectedEmployeeId = "";

    public CalculateSalaryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calculate_salary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectViews(view);
        addEvents();

        employeesIds = new ArrayList<>();
        employeesInfo = new ArrayList<>();
        resultSalariesIds = new ArrayList<>();
        selectedSalariesIds = new ArrayList<>();

        employeesSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, employeesInfo);
        employeesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        selectEmployeeSpinner.setAdapter(employeesSpinnerAdapter);

        calculateSalaryAdapter = new CalculateSalaryAdapter(getActivity(), selectedSalariesIds);
        resultListView.setAdapter(calculateSalaryAdapter);

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
                } else {
                    endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                }
                startDate = startDateEditText.getText().toString();
                endDate = endDateEditText.getText().toString();
//                updateEmployeesSpinner();
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        dateSetListener, y, m, d);
                if (!endDateEditText.getText().toString().isEmpty()) {
                    try {
                        datePickerDialog.getDatePicker().setMaxDate(CalendarUtil.sdfDayMonthYear
                                .parse(endDateEditText.getText().toString()).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        dateSetListener, y, m, d);
                if (!startDateEditText.getText().toString().isEmpty()) {
                    try {
                        datePickerDialog.getDatePicker().setMinDate(CalendarUtil.sdfDayMonthYear
                                .parse(startDateEditText.getText().toString()).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
                    endDateEditText.setError("Xin mời nhập");
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
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                saveChanges(false);
                            }
                        })
                        .setNegativeButton("Không", null).show();
            }
        });

        payAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Bạn có chắn chắn muốn thanh toán tất cả?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                saveChanges(true);
                            }
                        })
                        .setNegativeButton("Không", null).show();
            }
        });
    }

    private void getResultSalaries() {
        resultSalariesIds = SalaryRepository.getInstance(null)
                .getSalariesIdsByStartDateAndEndDate(startDate, endDate);

        ArrayList<Salary> resultSalaries = new ArrayList<>();
        ArrayList<String> resultEventIds = new ArrayList<>();
        for (String salaryId : resultSalariesIds) {
            Salary salary = SalaryRepository.getInstance(null).getAllSalaries().get(salaryId);
            resultSalaries.add(salary);
            if (!resultEventIds.contains(salary.getEventId())) {
                resultEventIds.add(salary.getEventId());
            }
        }

        Collections.sort(resultSalaries, new Comparator<Salary>() {
            @Override
            public int compare(Salary s1, Salary s2) {
                Date d1 = Calendar.getInstance().getTime();
                Date d2 = Calendar.getInstance().getTime();
                try {
                    d1 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getAllEvents().get(s1.getEventId()).getNgayBatDau());
                    d2 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getAllEvents().get(s2.getEventId()).getNgayBatDau());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return d1.compareTo(d2);
            }
        });

        resultSalariesIds.clear();
        for (Salary s : resultSalaries) {
            resultSalariesIds.add(s.getSalaryId());
        }
        numberOfEventsTextView.setText(resultEventIds.size() + " sự kiện có bản lương");
    }

    private void updateEmployeesSpinner() {
        if (resultSalariesIds.size() > 0) {
            employeesIds = EmployeeRepository.getInstance(null).getEmployeesIdsFromSalariesIds(resultSalariesIds);
            employeesInfo.clear();
            for (String employeeId : employeesIds) {
                Employee e = EmployeeRepository.getInstance(null).getAllEmployees().get(employeeId);
                employeesInfo.add(e.getHoTen() + " - " + e.getChuyenMon());
            }
            employeesSpinnerAdapter.notifyDataSetChanged();
            selectEmployeeSpinner.setSelection(0);
            selectedEmployeeId = employeesIds.get(selectEmployeeSpinner.getSelectedItemPosition());
        }
    }

    public void showResult() {
        int sum = 0, paid = 0, unpaid = 0;
        selectedSalariesIds.clear();
        for (String salaryId : resultSalariesIds) {
            Salary salary = SalaryRepository.getInstance(null).getAllSalaries().get(salaryId);
            if (salary.getEmployeeId().equals(selectedEmployeeId)) {
                selectedSalariesIds.add(salaryId);
                if (salary.isPaid()) {
                    paid += salary.getSalary();
                } else {
                    unpaid += salary.getSalary();
                }
            }
        }
        sum = paid + unpaid;
//        calculateSalaryAdapter.notifyDataSetChanged(selectedSalariesIds);
        calculateSalaryAdapter.notifyDataSetChanged();

        sumEditText.setText("" + sum);
        paidEditText.setText("" + paid);
        unpaidEditText.setText("" + unpaid);
    }

    private void saveChanges(boolean payAll) {
        // Update salaries to database based on selectedSalariesIds & resultListView
        ArrayList<Integer> changedSelectedSalariesAmount = new ArrayList<>();
        ArrayList<Boolean> changedSelectedSalariesPaidStatus = new ArrayList<>();
        int sum = 0, paid = 0, unpaid = 0;

        for (int i = 0; i < resultListView.getChildCount(); i++) {
            EditText salaryEditText = resultListView.getChildAt(i)
                    .findViewById(R.id.calculate_salaries_list_item_salary_edit_text);
            CheckBox isPaidCheckBox = resultListView.getChildAt(i)
                    .findViewById(R.id.calculate_salaries_list_item_paid_checkbox);

            int amount;
            if (salaryEditText.getText().toString().equals("")) {
                amount = 0;
            } else {
                amount = Integer.parseInt(salaryEditText.getText().toString());
            }

            boolean isPaid;
            if (payAll) {
                if (!isPaidCheckBox.isChecked()) {
                    isPaidCheckBox.setChecked(true);
                }
                isPaid = true;
            } else {
                isPaid = isPaidCheckBox.isChecked();
            }

            if (isPaidCheckBox.isChecked()) {
                isPaidCheckBox.setEnabled(false);
                salaryEditText.setEnabled(false);
            } else {
                isPaidCheckBox.setEnabled(true);
                salaryEditText.setEnabled(true);
            }

            changedSelectedSalariesAmount.add(amount);
            changedSelectedSalariesPaidStatus.add(isPaid);

            if (isPaid) {
                paid += amount;
            } else {
                unpaid += amount;
            }
        }
        sum = paid + unpaid;

        sumEditText.setText("" + sum);
        paidEditText.setText("" + paid);
        unpaidEditText.setText("" + unpaid);

        SalaryRepository.getInstance(null).updateSalaries(selectedSalariesIds,
                changedSelectedSalariesAmount, changedSelectedSalariesPaidStatus,
                new SalaryRepository.MyUpdateSalariesCallback() {
                    @Override
                    public void onCallback(boolean updateSucceed) {
                        // Completed updating salaries
                    }
                });
    }
}
