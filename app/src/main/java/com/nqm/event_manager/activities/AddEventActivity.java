package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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
import com.nqm.event_manager.adapters.EditEmployeeFromAddEventAdapter;
import com.nqm.event_manager.adapters.EditReminderAdapter;
import com.nqm.event_manager.adapters.EditScheduleRecyclerAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeInAddEventAdapter;
import com.nqm.event_manager.adapters.SelectReminderAdapter;
import com.nqm.event_manager.custom_views.AddScheduleSwipeAndDragCallback;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.fragments.ManageEventFragment;
import com.nqm.event_manager.interfaces.IOnAddScheduleViewClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.ScheduleUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AddEventActivity extends AppCompatActivity implements IOnAddScheduleViewClicked {
    Activity context;

    Toolbar toolbar;
    EditText titleEditText, startDateEditText, startTimeEditText, endDateEditText, endTimeEditText,
            locationEditText, noteEditText;
    TextView startDowTextView, endDowTextView;
    Button addEmployeeButton, scheduleButton;
    CustomListView editEmployeeListView;

    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;
    View currentView;

    ArrayList<String> selectedEmployeesIds;
    ArrayList<Schedule> schedules;
    ArrayList<Reminder> selectedReminders;

    EditEmployeeFromAddEventAdapter editEmployeeAdapter;
    SelectEmployeeInAddEventAdapter selectEmployeeAdapter;

    WindowManager.LayoutParams lWindowParams;
    EditScheduleRecyclerAdapter addScheduleAdapter;
    RecyclerView addScheduleRecyclerView;
    Button saveSchedulesButton, addScheduleButton, sortScheduleButton;
    Dialog addScheduleDialog;
    TextView titleTextView;
    AddScheduleSwipeAndDragCallback addScheduleSwipeAndDragCallback;
    ItemTouchHelper touchHelper;

    CustomListView reminderListView;
    Button addReminderButton;
    EditReminderAdapter editReminderAdapter;

    Calendar calendar = Calendar.getInstance();

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
        addReminderButton = findViewById(R.id.add_event_add_reminder_button);

        editEmployeeListView = findViewById(R.id.employees_list_view);
        reminderListView = findViewById(R.id.add_event_reminder_list_view);
    }

    private void init() {
        context = this;

        //initial data for main view
        selectedEmployeesIds = new ArrayList<>();
        schedules = new ArrayList<>();
        selectedReminders = new ArrayList<>();

        editEmployeeAdapter = new EditEmployeeFromAddEventAdapter(this, selectedEmployeesIds);
        selectEmployeeAdapter = new SelectEmployeeInAddEventAdapter(this, selectedEmployeesIds);
        editEmployeeListView.setAdapter(editEmployeeAdapter);

        //Set start date, end date edit texts from selected date
        String selectedDate = getIntent().getStringExtra("selectedDate");
        startDateEditText.setText(selectedDate);
        endDateEditText.setText(selectedDate);
        try {
            startDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(selectedDate));
            endDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(selectedDate));
            startTimeEditText.setText("12:00 PM");
            endTimeEditText.setText("01:00 PM");
        } catch (Exception e) {
            e.printStackTrace();
        }

        initAddScheduleDialog();

        editReminderAdapter = new EditReminderAdapter(this, selectedReminders);
        reminderListView.setAdapter(editReminderAdapter);
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
        addScheduleAdapter = new EditScheduleRecyclerAdapter();
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

    private void addEventToDatabase() {
        if (!titleEditText.getText().toString().isEmpty()) {

            Event event = new Event("",
                    titleEditText.getText().toString(),
                    startDateEditText.getText().toString(),
                    endDateEditText.getText().toString(),
                    startTimeEditText.getText().toString(),
                    endTimeEditText.getText().toString(),
                    locationEditText.getText().toString(),
                    noteEditText.getText().toString());

            final ArrayList<Salary> salaries = new ArrayList<>();
            for (int i = 0; i < editEmployeeListView.getChildCount(); i++) {
                Salary tempSalary = new Salary("",
                        "",
                        selectedEmployeesIds.get(i),
                        0,
                        false);
                salaries.add(tempSalary);
            }

            EventRepository.getInstance().setListener(ManageEventFragment.thisListener);
            for (Reminder r : selectedReminders) {
                Calendar calendar = Calendar.getInstance();
                Calendar calendarTime = Calendar.getInstance();
                try {
                    calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString()));
                    calendarTime.setTime(CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString()));
                    calendar.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
                    calendar.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                calendar.add(Calendar.MINUTE, r.getMinute() * (-1));
                r.setTime(CalendarUtil.sdfDayMonthYearTime.format(calendar.getTime()));
            }
            EventRepository.getInstance().addEventToDatabase(event, salaries, schedules, selectedReminders);
            context.finish();

        } else {
            if (titleEditText.getText().toString().isEmpty()) {
                titleEditText.setError("Xin mời nhập");
            } else {
                titleEditText.setError(null);
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

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                //Update TextEdits & TextViews;
                if (currentView == startDateEditText) {
                    startDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    startDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(startDateEditText.getText().toString()));
                } else {
                    endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                    endDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(endDateEditText.getText().toString()));
                }

                //Make sure start date + start time < end date + end time
                try {
                    Date startDate = CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString());
                    Date startTime = CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString());
                    Date endDate = CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString());
                    Date endTime = CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString());

                    if (startDate.compareTo(endDate) > 0) {
                        if (currentView == startDateEditText) {
                            endDateEditText.setText(startDateEditText.getText().toString());
                            endDowTextView.setText(startDowTextView.getText().toString());
                        } else {
                            startDateEditText.setText(endDateEditText.getText().toString());
                            startDowTextView.setText(endDowTextView.getText().toString());
                        }
                        if (startTime.compareTo(endTime) > 0) {
                            endTimeEditText.setText(startTimeEditText.getText().toString());
                        }
                    } else if (startDate.compareTo(endDate) == 0) {
                        if (startTime.compareTo(endTime) > 0) {
                            endTimeEditText.setText(startTimeEditText.getText().toString());
                        }
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

                if (currentView == startTimeEditText) {
                    startTimeEditText.setText(CalendarUtil.sdfTime.format(calendar.getTime()));
                } else {
                    endTimeEditText.setText(CalendarUtil.sdfTime.format(calendar.getTime()));
                }

                //Make sure start date + start time < end date + end time
                try {
                    Date startDate = CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString());
                    Date startTime = CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString());
                    Date endDate = CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString());
                    Date endTime = CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString());

                    if (startDate.compareTo(endDate) == 0) {
                        if (startTime.compareTo(endTime) > 0) {
                            if (currentView == startTimeEditText) {
                                endTimeEditText.setText(startTimeEditText.getText().toString());
                            } else {
                                calendar.setTime(endDate);
                                calendar.add(Calendar.DAY_OF_MONTH, 1);
                                endDateEditText.setText(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
                                endDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(endDateEditText.getText().toString()));
                            }
                        }
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
                int d = calendar.get(Calendar.DAY_OF_MONTH);
                int m = calendar.get(Calendar.MONTH);
                int y = calendar.get(Calendar.YEAR);
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                        dateSetListener, y, m, d);
                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
                datePickerDialog.show();
            }
        });

        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                int d = calendar.get(Calendar.DAY_OF_MONTH);
                int m = calendar.get(Calendar.MONTH);
                int y = calendar.get(Calendar.YEAR);
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                        dateSetListener, y, m, d);
                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
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

        addReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddReminderDialog();
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

        selectEmployeeListView.setAdapter(selectEmployeeAdapter);

        //Add events
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox tempCheckbox;
                for (int i = 0; i < selectEmployeeListView.getChildCount(); i++) {
                    tempCheckbox = selectEmployeeListView.getChildAt(i).findViewById(R.id.add_employee_checkbox);
                    if (tempCheckbox.isChecked() &&
                            !selectedEmployeesIds.contains(selectEmployeeAdapter.getAllEmployeesIds()[i])) {
                        selectedEmployeesIds.add(selectEmployeeAdapter.getAllEmployeesIds()[i]);
                    }
                    if (!tempCheckbox.isChecked()) {
                        selectedEmployeesIds.remove(selectEmployeeAdapter.getAllEmployeesIds()[i]);
                    }
                }
                editEmployeeAdapter.notifyDataSetChanged();
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

    private void openAddReminderDialog() {
        final Dialog selectReminderDialog = new Dialog(this);
        selectReminderDialog.setContentView(R.layout.dialog_select_reminder);

        //Connect views
        final ListView selectReminderListView = selectReminderDialog.findViewById(R.id.select_reminder_list_view);
        Button cancelButton = selectReminderDialog.findViewById(R.id.select_reminder_cancel_button);
        Button okButton = selectReminderDialog.findViewById(R.id.select_reminder_ok_button);

        final SelectReminderAdapter selectReminderAdapter;
        selectReminderAdapter = new SelectReminderAdapter(this, selectedReminders);
        selectReminderListView.setAdapter(selectReminderAdapter);

        //Add events
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedReminders.clear();
                for (int i = 0; i < selectReminderListView.getChildCount(); i++) {
                    CheckBox tempCheckbox = selectReminderListView.getChildAt(i)
                            .findViewById(R.id.select_reminder_item_select_check_box);
                    if (tempCheckbox.isChecked()) {
                        selectedReminders.add(selectReminderAdapter.getItem(i));
                    }
                }
                editReminderAdapter.notifyDataSetChanged(selectedReminders);
                selectReminderDialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    selectReminderDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            selectReminderDialog.show();
        }
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

    @Override
    public boolean onSupportNavigateUp() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_error)
                .setTitle("Trở về mà không lưu?")
                .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.finish();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }
}
