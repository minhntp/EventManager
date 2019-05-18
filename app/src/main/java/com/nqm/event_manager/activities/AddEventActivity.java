package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.nqm.event_manager.adapters.AddScheduleRecyclerAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeInAddEventAdapter;
import com.nqm.event_manager.custom_views.AddScheduleSwipeAndDragCallback;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnAddScheduleViewClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.ScheduleUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity implements IOnAddScheduleViewClicked {
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
    AddScheduleRecyclerAdapter addScheduleAdapter;
    RecyclerView addScheduleRecyclerView;
    Button saveSchedulesButton, addScheduleButton, sortScheduleButton;
    Dialog addScheduleDialog;
    TextView titleTextView;
    AddScheduleSwipeAndDragCallback addScheduleSwipeAndDragCallback;
    ItemTouchHelper touchHelper;


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

        deleteEmployeeListView.setAdapter(deleteAdapter);

        //Set start date, end date edit texts from selected date
        String selectedDate = getIntent().getStringExtra("selectedDate");
        startDateEditText.setText(selectedDate);
        endDateEditText.setText(selectedDate);
        try {
            startDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(selectedDate));
            endDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(selectedDate));
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
        addScheduleRecyclerView = addScheduleDialog.findViewById(R.id.add_schedule_dialog_schedule_recycler_view);
        saveSchedulesButton = addScheduleDialog.findViewById(R.id.ok_button);
        addScheduleButton = addScheduleDialog.findViewById(R.id.add_schedule_add_button);
        sortScheduleButton = addScheduleDialog.findViewById(R.id.add_schedule_sort_button);
        titleTextView = addScheduleDialog.findViewById(R.id.add_schedule_dialog_title_text_view);

        //init add schedule recycler view
        addScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addScheduleAdapter = new AddScheduleRecyclerAdapter();
        addScheduleSwipeAndDragCallback = new AddScheduleSwipeAndDragCallback(addScheduleAdapter);
        touchHelper = new ItemTouchHelper(addScheduleSwipeAndDragCallback);
        addScheduleAdapter.setTouchHelper(touchHelper);
        addScheduleAdapter.setSchedules(schedules);
        addScheduleAdapter.setListener(this);
        addScheduleAdapter.setContext(this);
        addScheduleRecyclerView.setAdapter(addScheduleAdapter);
        touchHelper.attachToRecyclerView(addScheduleRecyclerView);

        //add events
        addScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllSchedulesFromRecyclerView(false);
                schedules.add(new Schedule());
                addScheduleAdapter.setSchedules(schedules);
                addScheduleAdapter.notifyDataSetChanged();
                titleTextView.requestFocus();
            }
        });

        saveSchedulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllSchedulesFromRecyclerView(true);
                addScheduleAdapter.setSchedules(schedules);
                addScheduleDialog.dismiss();
            }
        });

        sortScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllSchedulesFromRecyclerView(false);
                ScheduleUtil.sortSchedulesByStartTime(schedules);
                addScheduleAdapter.setSchedules(schedules);
                addScheduleAdapter.notifyDataSetChanged();
                titleTextView.requestFocus();
            }
        });

    }

    private void connectViews() {
        toolbar = findViewById(R.id.add_event_toolbar);
        setSupportActionBar(toolbar);
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
                    startDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(startDateEditText.getText().toString()));
                } else {
                    endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    endDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(endDateEditText.getText().toString()));
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
                        endDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(endDateEditText.getText().toString()));
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
                int d = 1;
                int m = 1;
                int y = 1990;
                calendar = Calendar.getInstance();
                if (!startDateEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString()));
                        d = calendar.get(Calendar.DAY_OF_MONTH);
                        m = calendar.get(Calendar.MONTH);
                        y = calendar.get(Calendar.YEAR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                currentView = startDateEditText;

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
                int d = 1;
                int m = 1;
                int y = 1990;
                calendar = Calendar.getInstance();
                if (!endDateEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString()));
                        d = calendar.get(Calendar.DAY_OF_MONTH);
                        m = calendar.get(Calendar.MONTH);
                        y = calendar.get(Calendar.YEAR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                currentView = endDateEditText;

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
                int hourOfDay = 18;
                int minute = 0;
                calendar = Calendar.getInstance();
                if (!startTimeEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString()));
                        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                        minute = calendar.get(Calendar.MINUTE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                currentView = startTimeEditText;
//                int HH = calendar.get(Calendar.HOUR_OF_DAY);
//                int mm = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(AddEventActivity.this, timeSetListener, hourOfDay,
                        minute, false).show();
            }
        });
        endTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hourOfDay = 18;
                int minute = 0;
                calendar = Calendar.getInstance();
                if (!endTimeEditText.getText().toString().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString()));
                        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                        minute = calendar.get(Calendar.MINUTE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                currentView = endTimeEditText;
//                int HH = calendar.get(Calendar.HOUR_OF_DAY);
//                int mm = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(AddEventActivity.this, timeSetListener, hourOfDay,
                        minute, false).show();
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
        new android.support.v7.app.AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Trở về mà không lưu?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Intent intent = new Intent();
//                        intent.putExtra("edit?", false);
//                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onTimeEditTextSet(int position, String timeText) {
        getAllSchedulesFromRecyclerView(false);
        schedules.get(position).setTime(timeText);
        addScheduleAdapter.setSchedules(schedules);
        addScheduleAdapter.notifyDataSetChanged();
        titleTextView.requestFocus();
    }

    @Override
    public void onAddScheduleItemMoved() {
        schedules = addScheduleAdapter.getSchedules();
    }

    @Override
    public void onAddScheduleItemRemoved() {
        schedules = addScheduleAdapter.getSchedules();
    }

    private void getAllSchedulesFromRecyclerView(boolean removeEmptySchedules) {
        schedules.clear();
        int index = 0;
        for (int i = 0; i < addScheduleRecyclerView.getChildCount(); i++) {
            EditText timeEditText = addScheduleRecyclerView.getChildAt(i).findViewById(R.id.add_schedule_time_edit_text);
            EditText contentEditText = addScheduleRecyclerView.getChildAt(i).findViewById(R.id.add_schedule_content_edit_text);
            String time = timeEditText.getText().toString();
            String content = contentEditText.getText().toString();
            if (removeEmptySchedules && time.isEmpty() && content.isEmpty()) {
                continue;
            } else {
                schedules.add(new Schedule("", "", time, content, index));
                index++;
            }
        }
    }
}
