package com.nqm.event_manager.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class CalculateSalaryForSingleEmployeeActivity extends AppCompatActivity {

    Context context;

    Button calculateButton, payAllButton, saveButton;
    CustomListView resultListView;
    EditText startDateEditText, endDateEditText, sumEditText, paidEditText, unpaidEditText;
    TextView numberOfEventsTextView, sumTextView, nameTextView, specialityTextView;
    ImageView profileImageView;

    DatePickerDialog.OnDateSetListener dateSetListener;
    Calendar calendar = Calendar.getInstance();
    View currentView;

    ArrayList<String> resultSalariesIds;

    CalculateSalaryAdapter calculateSalaryAdapter;

    String startDate, endDate;
    String selectedEmployeeId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_salary_for_single_employee);

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
        selectedEmployeeId = getIntent().getStringExtra("employeeId");
        nameTextView.setText(EmployeeRepository.getInstance(null).getAllEmployees().get(selectedEmployeeId).getHoTen());
        specialityTextView.setText(EmployeeRepository.getInstance(null).getAllEmployees().get(selectedEmployeeId).getChuyenMon());

        startDate = CalendarUtil.sdfDayMonthYear.format(calendar.getTime());
        endDate = CalendarUtil.sdfDayMonthYear.format(calendar.getTime());
        startDateEditText.setText(startDate);
        endDateEditText.setText(endDate);

        resultSalariesIds = new ArrayList<>();
        calculateSalaryAdapter = new CalculateSalaryAdapter(this, resultSalariesIds);
        resultListView.setAdapter(calculateSalaryAdapter);

        context = this;
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
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
                    endDateEditText.setError("");
                    return;
                } else {
                    endDateEditText.setError(null);
                }

                getResultSalaries();
                showResult();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Bạn có chắn chắn muốn lưu thay đổi?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
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
                new AlertDialog.Builder(context)
                        .setTitle("Bạn có chắn chắn muốn thanh toán tất cả?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
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
        resultSalariesIds = SalaryRepository.getInstance(null)
                .getSalariesIdsByStartDateAndEndDateAndEmployeeId(startDate, endDate, selectedEmployeeId);

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

    public void showResult() {
        int sum = 0, paid = 0, unpaid = 0;
        for (String salaryId : resultSalariesIds) {
            Salary salary = SalaryRepository.getInstance(null).getAllSalaries().get(salaryId);
            if (salary.isPaid()) {
                paid += salary.getSalary();
            } else {
                unpaid += salary.getSalary();
            }
        }
        sum = paid + unpaid;
//        calculateSalaryAdapter.notifyDataSetChanged(selectedSalariesIds);
        calculateSalaryAdapter.notifyDataSetChanged(resultSalariesIds);

        sumEditText.setText("" + sum);
        paidEditText.setText("" + paid);
        unpaidEditText.setText("" + unpaid);

        if (unpaid == 0) {
            saveButton.setEnabled(false);
            payAllButton.setEnabled(false);
        } else {
            saveButton.setEnabled(true);
            payAllButton.setEnabled(true);
        }
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

        sumTextView.requestFocus();

        SalaryRepository.getInstance(null).updateSalaries(resultSalariesIds,
                changedSelectedSalariesAmount, changedSelectedSalariesPaidStatus,
                new SalaryRepository.MyUpdateSalariesCallback() {
                    @Override
                    public void onCallback(boolean updateSucceed) {
                        // Completed updating salaries
                    }
                });
    }

}
