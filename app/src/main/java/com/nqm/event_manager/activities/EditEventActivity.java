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
import android.support.v7.widget.SearchView;
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
import com.nqm.event_manager.adapters.EditEmployeeFromEditEventAdapter;
import com.nqm.event_manager.adapters.EditReminderAdapter;
import com.nqm.event_manager.adapters.EditScheduleRecyclerAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeInEditEventAdapter;
import com.nqm.event_manager.adapters.SelectReminderAdapter;
import com.nqm.event_manager.custom_views.AddScheduleSwipeAndDragCallback;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnAddScheduleViewClicked;
import com.nqm.event_manager.interfaces.IOnSelectEmployeeViewClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ReminderRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EditEventActivity extends AppCompatActivity implements IOnAddScheduleViewClicked,
        IOnSelectEmployeeViewClicked {
    android.support.v7.widget.Toolbar toolbar;

    EditText titleEditText, startDateEditText, startTimeEditText, endDateEditText, endTimeEditText,
            locationEditText, noteEditText;
    TextView startDowTextView, endDowTextView;
    Button addEmployeesButton, scheduleButton;
    CustomListView employeeListView;

    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;
    View currentView;

    String eventId;
    Event event;
    ArrayList<String> selectedEmployeesIds;
    ArrayList<Schedule> schedules;

    EditEmployeeFromEditEventAdapter editEmployeeAdapter;

    WindowManager.LayoutParams lWindowParams;
    EditScheduleRecyclerAdapter addScheduleAdapter;
    RecyclerView addScheduleRecyclerView;
    Button saveSchedulesButton, addScheduleButton, sortScheduleButton;
    Dialog addScheduleDialog;
    TextView titleTextView;
    AddScheduleSwipeAndDragCallback addScheduleSwipeAndDragCallback;
    ItemTouchHelper touchHelper;

    ArrayList<Reminder> selectedReminders;
    CustomListView editReminderListView;
    EditReminderAdapter editReminderAdapter;
    Button selectReminderButton;

    RecyclerView selectEmployeeRecyclerView;
    SelectEmployeeInEditEventAdapter selectEmployeeAdapter;
    String searchString = "";

    Calendar calendar = Calendar.getInstance();

    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        connectViews();
        init();
        addEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_event_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit_event_action_save_event) {
            updateEventToDatabase();
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectViews() {
        toolbar = findViewById(R.id.edit_event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.edit_event_activity_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        titleEditText = findViewById(R.id.event_edit_title_edit_text);
        startDateEditText = findViewById(R.id.event_edit_start_date_edit_text);
        startTimeEditText = findViewById(R.id.event_edit_start_time_edit_text);
        endDateEditText = findViewById(R.id.event_edit_end_date_edit_text);
        endTimeEditText = findViewById(R.id.event_edit_end_time_edit_text);

        startDowTextView = findViewById(R.id.event_edit_start_dow_textview);
        endDowTextView = findViewById(R.id.event_edit_end_dow_textview);

        locationEditText = findViewById(R.id.event_edit_location_edit_text);
        noteEditText = findViewById(R.id.event_edit_note_edit_text);

        addEmployeesButton = findViewById(R.id.event_edit_add_employee_button);
        scheduleButton = findViewById(R.id.edit_event_schedule_button);

        employeeListView = findViewById(R.id.event_edit_employee_list_view);

        editReminderListView = findViewById(R.id.edit_event_reminder_list_view);
        selectReminderButton = findViewById(R.id.edit_event_add_reminder_button);
    }

    private void init() {
        context = this;

        eventId = getIntent().getStringExtra("eventId");
        event = EventRepository.getInstance(null).getAllEvents().get(eventId);

        selectedEmployeesIds = EmployeeRepository.getInstance().getEmployeesIdsByEventId(eventId);
        schedules = ScheduleRepository.getInstance(null).getSchedulesInArrayListByEventId(eventId);
        ScheduleRepository.sortSchedulesByOrder(schedules);

        editEmployeeAdapter = new EditEmployeeFromEditEventAdapter(this, eventId, selectedEmployeesIds);

        fillInformation();
        initAddScheduleDialog();

        selectedReminders = ReminderRepository.getInstance().getRemindersInArrayListByEventId(eventId);
        editReminderAdapter = new EditReminderAdapter(this, selectedReminders);
        editReminderListView.setAdapter(editReminderAdapter);
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
                ScheduleRepository.sortSchedulesByStartTime(schedules);
                addScheduleAdapter.setSchedules(schedules);
                addScheduleAdapter.notifyDataSetChanged();
                titleTextView.requestFocus();
            }
        });
    }

    private void fillInformation() {
        titleEditText.setText(event.getTen());
        startDateEditText.setText(event.getNgayBatDau());
        startTimeEditText.setText(event.getGioBatDau());
        endDateEditText.setText(event.getNgayKetThuc());
        endTimeEditText.setText(event.getGioKetThuc());

        try {
            startDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(startDateEditText.getText().toString()));
            endDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(endDateEditText.getText().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        locationEditText.setText(event.getDiaDiem());
        noteEditText.setText(event.getGhiChu());

        employeeListView.setAdapter(editEmployeeAdapter);
    }

    private void addEvents() {
        addEmployeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSelectEmployeeDialog();
            }
        });

        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditScheduleDialog();
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

        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                int d = 1;
                int m = 1;
                int y = 1990;
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

                DatePickerDialog datePickerDialog = new DatePickerDialog(EditEventActivity.this,
                        dateSetListener, y, m, d);
                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditEventActivity.this,
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
                new TimePickerDialog(EditEventActivity.this, timeSetListener, hourOfDay,
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
                new TimePickerDialog(EditEventActivity.this, timeSetListener, hourOfDay,
                        minute, false).show();
            }
        });

        selectReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddReminderDialog();
            }
        });
    }

    private void updateEventToDatabase() {
        if (titleEditText.getText().toString().isEmpty()) {
            titleEditText.setError("Xin mời nhập");
            return;
        } else {
            titleEditText.setError(null);
        }

        //Event
        Event changedEvent = new Event(eventId, titleEditText.getText().toString(),
                startDateEditText.getText().toString(), endDateEditText.getText().toString(),
                startTimeEditText.getText().toString(), endTimeEditText.getText().toString(),
                locationEditText.getText().toString(), noteEditText.getText().toString());

        //Salaries
        ArrayList<String> unchangedEmployeesIds = EmployeeRepository.getInstance(null)
                .getEmployeesIdsByEventId(eventId);

        ArrayList<String> deleteEmployeesIds = new ArrayList<>();
        ArrayList<String> addEmployeesIds = new ArrayList<>();

        for (String employeeId : unchangedEmployeesIds) {
            if (!selectedEmployeesIds.contains(employeeId)) {
                //Delete
                deleteEmployeesIds.add(employeeId);
            }
        }
        for (String employeeId : selectedEmployeesIds) {
            if (!unchangedEmployeesIds.contains(employeeId)) {
                //Add
                addEmployeesIds.add(employeeId);
            }
        }

        for (Reminder r : selectedReminders) {
            Calendar calendarDateTime = Calendar.getInstance();
            Calendar calendarTime = Calendar.getInstance();
            try {
                calendarDateTime.setTime(CalendarUtil.sdfDayMonthYear.parse(changedEvent.getNgayBatDau()));
                calendarTime.setTime(CalendarUtil.sdfTime.parse(changedEvent.getGioBatDau()));
                calendarDateTime.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
                calendarDateTime.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
            } catch (Exception e) {
                e.printStackTrace();
            }
            calendarDateTime.add(Calendar.MINUTE, r.getMinute() * (-1));
            r.setTime(CalendarUtil.sdfDayMonthYearTime.format(calendarDateTime.getTime()));
        }

        EventRepository.getInstance().updateEventToDatabase(changedEvent, deleteEmployeesIds,
                addEmployeesIds, schedules, selectedReminders);
        context.finish();
    }

    private void openSelectEmployeeDialog() {
        final Dialog selectEmployeeDialog = new Dialog(this);
        selectEmployeeDialog.setContentView(R.layout.dialog_select_employee);
        lWindowParams.copyFrom(selectEmployeeDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        selectEmployeeRecyclerView = selectEmployeeDialog.findViewById(R.id.select_employee_recycler_view);
        Button okButton = selectEmployeeDialog.findViewById(R.id.ok_button);

        selectEmployeeAdapter = new SelectEmployeeInEditEventAdapter(this, eventId, selectedEmployeesIds,
                EmployeeRepository.getInstance().getEmployeesBySearchString(searchString));
        selectEmployeeAdapter.setListener(this);
//        selectEmployeeListView.setAdapter(selectEmployeeAdapter);

        SearchView searchView = selectEmployeeDialog.findViewById(R.id.select_employee_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //set adapter search string = newText
                //notifyOnDataSetChanged()
                searchString = newText;
                ArrayList<Employee> employees = EmployeeRepository.getInstance().getEmployeesBySearchString(newText);
                selectEmployeeAdapter.notifyDataSetChanged(selectedEmployeesIds, employees);
                return true;
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedEmployeesIds = selectEmployeeAdapter.getSelectedEmployeesIds();
                editEmployeeAdapter.notifyDataSetChanged(selectedEmployeesIds);
                selectEmployeeDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            selectEmployeeDialog.show();
        }
    }

    private void showEditScheduleDialog() {
        if (!isFinishing()) {
            addScheduleDialog.show();
            addScheduleDialog.getWindow().setAttributes(lWindowParams);
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
        for (int i = 0; i < addScheduleRecyclerView.getChildCount(); i++) {
            EditText timeEditText = addScheduleRecyclerView.getChildAt(i).findViewById(R.id.add_schedule_time_edit_text);
            EditText contentEditText = addScheduleRecyclerView.getChildAt(i).findViewById(R.id.add_schedule_content_edit_text);
            String time = timeEditText.getText().toString();
            String content = contentEditText.getText().toString();
            if (removeEmptySchedules && time.isEmpty() && content.isEmpty()) {
                continue;
            } else {
                schedules.add(new Schedule("", "", time, content, i));
            }
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

    @Override
    public void onCheckBoxClicked(String employeeId, boolean isChecked) {
        if (isChecked) {
            selectedEmployeesIds.add(employeeId);
        } else {
            selectedEmployeesIds.remove(employeeId);
        }

        selectEmployeeAdapter.notifyDataSetChanged(selectedEmployeesIds,
                EmployeeRepository.getInstance().getEmployeesBySearchString(searchString));
    }
}
