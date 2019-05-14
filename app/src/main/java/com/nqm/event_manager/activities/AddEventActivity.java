package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.AddEmployeeFromAddEventAdapter;
import com.nqm.event_manager.adapters.AddScheduleAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeInAddEventAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnCustomViewClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity implements IOnCustomViewClicked {
    Toolbar toolbar;

    EditText titleEditText, startDateEditText, startTimeEditText, endDateEditText, endTimeEditText,
            locationEditText, noteEditText;
    TextView startDowTextView, endDowTextView;
    Button addEmployeeButton, scheduleButton;
    CustomListView deleteEmployeeListView;

    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;
    View currentView;

    ArrayList<String> selectedEmployeesIds;
    ArrayList<Schedule> schedules;

    AddEmployeeFromAddEventAdapter deleteAdapter;
    SelectEmployeeInAddEventAdapter selectAdapter;

    WindowManager.LayoutParams lWindowParams;
    AddScheduleAdapter addScheduleAdapter;
    ListView addScheduleListView;
    Button okButton;
    Button addScheduleButton;
    Dialog addScheduleDialog;
    TextView titleTextView;

    Calendar calendar = Calendar.getInstance();

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        connectViews();
        init();
        addEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_event_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add_event_action_add_event) {
            addEventToDatabase();
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        context = this;

        //initial data for main view
        selectedEmployeesIds = new ArrayList<>();
        schedules = new ArrayList<>();

        deleteAdapter = new AddEmployeeFromAddEventAdapter(this, selectedEmployeesIds);
        selectAdapter = new SelectEmployeeInAddEventAdapter(this, selectedEmployeesIds);
        addScheduleAdapter = new AddScheduleAdapter(this, schedules, this);

        deleteEmployeeListView.setAdapter(deleteAdapter);

        //Set start date, end date edit texts from selected date
        String selectedDate = getIntent().getStringExtra("selectedDate");
        startDateEditText.setText(selectedDate);
        endDateEditText.setText(selectedDate);
        try {
            startDowTextView.setText(CalendarUtil.sdfDayOfWeek.format(CalendarUtil.sdfDayMonthYear.parse(selectedDate)));
            endDowTextView.setText(CalendarUtil.sdfDayOfWeek.format(CalendarUtil.sdfDayMonthYear.parse(selectedDate)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        initAddScheduleDialog();
    }

    private void initAddScheduleDialog() {
        //initial dialog
        addScheduleDialog = new Dialog(this);
        addScheduleDialog.setContentView(R.layout.dialog_add_schedule);
        lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(addScheduleDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //connect views
        addScheduleListView = addScheduleDialog.findViewById(R.id.add_schedule_dialog_schedule_list_view);
        okButton = addScheduleDialog.findViewById(R.id.ok_button);
        addScheduleButton = addScheduleDialog.findViewById(R.id.add_schedule_add_button);
        titleTextView = addScheduleDialog.findViewById(R.id.add_schedule_dialog_title_text_view);

        addScheduleListView.setAdapter(addScheduleAdapter);

        addScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllSchedulesFromListView(false);
                schedules.add(new Schedule());
                addScheduleAdapter.notifyDataSetChanged();
                titleTextView.requestFocus();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllSchedulesFromListView(true);
                addScheduleDialog.dismiss();
            }
        });
    }

    private void connectViews() {
        toolbar = findViewById(R.id.add_event_toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle(R.string.add_event_activity_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titleEditText = findViewById(R.id.title_edit_text);
        startDateEditText = findViewById(R.id.start_date_edit_text);
        startTimeEditText = findViewById(R.id.start_time_edit_text);
        endDateEditText = findViewById(R.id.end_date_edit_text);
        endTimeEditText = findViewById(R.id.end_time_edit_text);
        locationEditText = findViewById(R.id.location_edit_text);
        noteEditText = findViewById(R.id.note_edit_text);

        startDowTextView = findViewById(R.id.start_dow_textview);
        endDowTextView = findViewById(R.id.end_dow_textview);

        addEmployeeButton = findViewById(R.id.add_employee_button);
        scheduleButton = findViewById(R.id.add_event_schedule_button);

        deleteEmployeeListView = findViewById(R.id.employees_list_view);
    }

    private void addEventToDatabase() {
        if (!titleEditText.getText().toString().isEmpty() &&
                !startTimeEditText.getText().toString().isEmpty() &&
                !endTimeEditText.getText().toString().isEmpty()) {

            Event event = new Event("",
                    titleEditText.getText().toString(),
                    startDateEditText.getText().toString(),
                    endDateEditText.getText().toString(),
                    startTimeEditText.getText().toString(),
                    endTimeEditText.getText().toString(),
                    locationEditText.getText().toString(),
                    noteEditText.getText().toString());

            final ArrayList<Salary> salaries = new ArrayList<>();
            for (int i = 0; i < deleteEmployeeListView.getChildCount(); i++) {
                Salary tempSalary = new Salary("", "",
                        selectedEmployeesIds.get(i),
                        0,
                        false);
                salaries.add(tempSalary);
            }
            //Add event to sukien collection & salaries to luong collection
            EventRepository.getInstance(null).addEventToDatabase(event, salaries, schedules, new EventRepository.MyAddEventCallback() {
                @Override
                public void onCallback(String eventId) {
                    Intent intent = new Intent();
                    intent.putExtra("added?", true);
                    setResult(RESULT_OK, intent);
                    ((Activity) context).finish();
                }
            });
        } else {
            if (titleEditText.getText().toString().isEmpty()) {
                titleEditText.setError("Xin mời nhập");
            } else {
                titleEditText.setError(null);
            }
            if (startTimeEditText.getText().toString().isEmpty()) {
                startTimeEditText.setError("");
            } else {
                startTimeEditText.setError(null);
            }
            if (endTimeEditText.getText().toString().isEmpty()) {
                endTimeEditText.setError("");
            } else {
                endTimeEditText.setError(null);
            }
        }
    }

    private void addEvents() {
        addEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddEmployeeDialog();
            }
        });

        //Date set and Time set Listeners
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

//                Update TextEdits & TextViews;
                if (currentView == startDateEditText) {
                    startDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    startDowTextView.setText(CalendarUtil.sdfDayOfWeek.format(calendar.getTime()));
                } else {
                    endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    endDowTextView.setText(CalendarUtil.sdfDayOfWeek.format(calendar.getTime()));
                }

//                Set (end time = start time) if (end date == start date) and (end time < start time)
                try {
                    if (endDateEditText.getText().toString().equals(startDateEditText.getText().toString())
                            && CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString()).getTime() <
                            CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString()).getTime()) {
                        endTimeEditText.setText(startTimeEditText.getText().toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                //Update TextEdits & TextViews;
                if (currentView == startTimeEditText) {
                    startTimeEditText.setText(CalendarUtil.sdfTime.format(calendar.getTime()));
                    startTimeEditText.setError(null);
                } else {
                    endTimeEditText.setText(CalendarUtil.sdfTime.format(calendar.getTime()));
                    endTimeEditText.setError(null);
                }

                //Set (end date += 1) if (end date == start date) and (end time < start time)
                boolean increaseEndDateCondition = !(endTimeEditText.getText().toString().isEmpty() ||
                        startTimeEditText.getText().toString().isEmpty());
                try {
                    if (increaseEndDateCondition
                            && startDateEditText.getText().toString().equals(endDateEditText.getText().toString())
                            && (CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString()).getTime()
                            > CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString()).getTime())) {
                        calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString()));
                        calendar.add(Calendar.DATE, 1);
                        endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                        endDowTextView.setText(CalendarUtil.sdfDayOfWeek.format(calendar.getTime()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //set start Date, start Time, end Date, end Time
        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this, dateSetListener, y,
                        m, d);
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
            public void onClick(View view) {
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this, dateSetListener, y,
                        m, d);
                if (!startDateEditText.getText().toString().isEmpty()) {
                    try {
                        datePickerDialog.getDatePicker().setMinDate(CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString()).getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                datePickerDialog.show();
            }
        });

        startTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                if (!startTimeEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                currentView = startTimeEditText;
//                int HH = calendar.get(Calendar.HOUR_OF_DAY);
//                int mm = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(AddEventActivity.this, timeSetListener, 18,
                        0, false).show();
            }
        });
        endTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                if (!endTimeEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                currentView = endTimeEditText;
//                int HH = calendar.get(Calendar.HOUR_OF_DAY);
//                int mm = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(AddEventActivity.this, timeSetListener, 18,
                        0, false).show();
            }
        });
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddScheduleDialog();
            }
        });
    }

    private void showAddScheduleDialog() {
        if (!isFinishing()) {
            addScheduleDialog.show();
            addScheduleDialog.getWindow().setAttributes(lWindowParams);
        }
    }

    private void openAddEmployeeDialog() {
        final Dialog addEmployeeDialog = new Dialog(this);
        addEmployeeDialog.setContentView(R.layout.dialog_select_employee);

        //Connect views
        final ListView selectEmployeeListView = addEmployeeDialog.findViewById(R.id.select_employee_list_view);
        Button cancelButton = addEmployeeDialog.findViewById(R.id.cancel_button);
        Button okButton = addEmployeeDialog.findViewById(R.id.ok_button);

        selectEmployeeListView.setAdapter(selectAdapter);

        //Add events
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox tempCheckbox;
                for (int i = 0; i < selectEmployeeListView.getChildCount(); i++) {
                    tempCheckbox = selectEmployeeListView.getChildAt(i).findViewById(R.id.add_employee_checkbox);
                    if (tempCheckbox.isChecked() &&
                            !selectedEmployeesIds.contains(selectAdapter.getAllEmployeesIds()[i])) {
                        selectedEmployeesIds.add(selectAdapter.getAllEmployeesIds()[i]);
                    }
                    if (!tempCheckbox.isChecked() &&
                            selectedEmployeesIds.contains(selectAdapter.getAllEmployeesIds()[i])) {
                        selectedEmployeesIds.remove(selectAdapter.getAllEmployeesIds()[i]);
                    }
                }
                deleteAdapter.notifyDataSetChanged();
                addEmployeeDialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEmployeeDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            addEmployeeDialog.show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onDeleteButtonClicked(int position) {
        //Save all data from add schedule adapter -> remove item[position] in adapter -> notifyDataSetChanged
        getAllSchedulesFromListView(false);
        schedules.remove(position);
        addScheduleAdapter.notifyDataSetChanged();
        titleTextView.requestFocus();
    }

    @Override
    public void onTimeEditTextSet(int position, String timeText) {
        getAllSchedulesFromListView(false);
        schedules.get(position).setTime(timeText);
        addScheduleAdapter.notifyDataSetChanged();
        titleTextView.requestFocus();
    }

    @Override
    public void onEmployeeListItemClicked(String employeeId) {

    }

    private void getAllSchedulesFromListView(boolean removeEmptySchedules) {
        schedules.clear();
        for (int i = 0; i < addScheduleListView.getChildCount(); i++) {
            EditText timeEditText = addScheduleListView.getChildAt(i).findViewById(R.id.add_schedule_time_edit_text);
            EditText contentEditText = addScheduleListView.getChildAt(i).findViewById(R.id.add_schedule_content_edit_text);
            String time = timeEditText.getText().toString();
            String content = contentEditText.getText().toString();
            if (removeEmptySchedules && time.isEmpty() && content.isEmpty()) {
                continue;
            } else {
                schedules.add(new Schedule("", "", time, content));
            }
        }
    }
}
