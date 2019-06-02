package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.EditEmployeeAddEventAdapter;
import com.nqm.event_manager.adapters.EditReminderAdapter;
import com.nqm.event_manager.adapters.EditScheduleAdapter;
import com.nqm.event_manager.adapters.EditTaskAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeAddEventAdapter;
import com.nqm.event_manager.adapters.SelectReminderAdapter;
import com.nqm.event_manager.callbacks.ItemDraggedOrSwipedCallback;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.fragments.ManageEventFragment;
import com.nqm.event_manager.interfaces.IOnEditEmployeeItemClicked;
import com.nqm.event_manager.interfaces.IOnEditReminderItemClicked;
import com.nqm.event_manager.interfaces.IOnEditTaskItemClicked;
import com.nqm.event_manager.interfaces.IOnSelectEmployeeItemClicked;
import com.nqm.event_manager.interfaces.IOnSelectReminderItemClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.models.Task;
import com.nqm.event_manager.repositories.DefaultReminderRepository;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.repositories.TaskRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AddEventActivity extends AppCompatActivity implements IOnSelectEmployeeItemClicked,
        IOnEditEmployeeItemClicked, IOnSelectReminderItemClicked, IOnEditReminderItemClicked,
        IOnEditTaskItemClicked {
    Activity context;

    Toolbar toolbar;
    EditText titleEditText, startDateEditText, startTimeEditText, endDateEditText, endTimeEditText,
            locationEditText, noteEditText;
    TextView startDowTextView, endDowTextView;
    Button selectEmployeeButton, scheduleButton, conflictButton, taskButton;

    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;
    View currentView;

    ArrayList<String> selectedEmployeesIds;
    HashMap<String, ArrayList<String>> conflictsMap;
    EditEmployeeAddEventAdapter editEmployeeAdapter;
    RecyclerView editEmployeeRecyclerView;

    ArrayList<Employee> employees;
    Dialog selectEmployeeDialog;
    Button selectEmployeeOkButton;
    SearchView selectEmployeeSearchView;
    RecyclerView selectEmployeeRecyclerView;
    SelectEmployeeAddEventAdapter selectEmployeeAdapter;

    ArrayList<Task> tasks;
    int count = 0;
    EditTaskAdapter editTaskAdapter;
    RecyclerView editTaskRecyclerView;
    Button editTaskOkButton, editTaskAddButton, editTaskSortButton;
    Dialog editTaskDialog;
    TextView editTaskCompletedTextView;
    ProgressBar editTaskProgressBar;
    ItemDraggedOrSwipedCallback editTaskCallback;
    ItemTouchHelper editTaskTouchHelper;

    ArrayList<Schedule> schedules;
    WindowManager.LayoutParams lWindowParams;
    EditScheduleAdapter editScheduleAdapter;
    RecyclerView editScheduleRecyclerView;
    Button editScheduleOkButton, editScheduleAddButton, editScheduleSortButton;
    Dialog editScheduleDialog;
    ItemDraggedOrSwipedCallback editScheduleCallback;
    ItemTouchHelper editScheduleTouchHelper;

    ArrayList<Reminder> selectedReminders;
    CustomListView reminderListView;
    Button selectReminderButton;
    EditReminderAdapter editReminderAdapter;

    Dialog selectReminderDialog;
    ListView selectReminderListView;
    Button selectReminderOkButton;
    SelectReminderAdapter selectReminderAdapter;

    String startTime, endTime;

    Calendar calendar = Calendar.getInstance();
    Calendar calendar2 = Calendar.getInstance();
    long startMili, endMili;

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        titleEditText = findViewById(R.id.add_event_title_edit_text);
        startDateEditText = findViewById(R.id.add_event_start_date_edit_text);
        startTimeEditText = findViewById(R.id.add_event_start_time_edit_text);
        endDateEditText = findViewById(R.id.add_event_end_date_edit_text);
        endTimeEditText = findViewById(R.id.add_event_end_time_edit_text);
        locationEditText = findViewById(R.id.add_event_location_edit_text);
        noteEditText = findViewById(R.id.add_event_note_edit_text);

        startDowTextView = findViewById(R.id.add_event_start_dow_text_view);
        endDowTextView = findViewById(R.id.add_event_end_dow_textview);

        conflictButton = findViewById(R.id.add_event_conflict_button);
        selectEmployeeButton = findViewById(R.id.add_event_select_employee_button);
        taskButton = findViewById(R.id.add_event_task_button);
        scheduleButton = findViewById(R.id.add_event_schedule_button);
        selectReminderButton = findViewById(R.id.add_event_add_reminder_button);

        editEmployeeRecyclerView = findViewById(R.id.add_event_edit_employee_recycler_view);
        reminderListView = findViewById(R.id.add_event_reminder_list_view);
    }

    private void init() {
        context = this;

        //Set start date, end date edit texts from selected date
        String selectedDate = getIntent().getStringExtra(Constants.INTENT_SELECTED_DATE);
        startDateEditText.setText(selectedDate);
        endDateEditText.setText(selectedDate);
        try {
            startDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(selectedDate));
            endDowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(selectedDate));
            calendar.set(Calendar.HOUR_OF_DAY, 12);
            calendar.set(Calendar.MINUTE, 0);
            startTimeEditText.setText(CalendarUtil.sdfTime.format(calendar.getTime()));
            calendar.set(Calendar.HOUR_OF_DAY, 13);
            endTimeEditText.setText(CalendarUtil.sdfTime.format(calendar.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        selectedEmployeesIds = new ArrayList<>();
        conflictsMap = new HashMap<>();
        editEmployeeAdapter = new EditEmployeeAddEventAdapter(selectedEmployeesIds, conflictsMap);
        editEmployeeAdapter.setListener(this);
        editEmployeeRecyclerView.setAdapter(editEmployeeAdapter);
        editEmployeeRecyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        editEmployeeRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        initAddScheduleDialog();
        initEditTaskDialog();
        initSelectEmployeeDialog();

        selectedReminders = new ArrayList<>();
        for (int minute : DefaultReminderRepository.getInstance().getDefaultReminders()) {
            selectedReminders.add(new Reminder("", "", minute, ""));
        }
        editReminderAdapter = new EditReminderAdapter(this, selectedReminders);
        editReminderAdapter.setListener(this);
        reminderListView.setAdapter(editReminderAdapter);

        initSelectReminderDialog();
    }

    private void initAddScheduleDialog() {
        //data
        schedules = new ArrayList<>();

        //initial dialog
        editScheduleDialog = new Dialog(this);
        editScheduleDialog.setContentView(R.layout.dialog_edit_schedule);
        lWindowParams = new WindowManager.LayoutParams();
        if (editScheduleDialog.getWindow() != null) {
            lWindowParams.copyFrom(editScheduleDialog.getWindow().getAttributes());
        }
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //connect views
        editScheduleRecyclerView = editScheduleDialog.findViewById(R.id.add_schedule_dialog_schedule_recycler_view);
        editScheduleOkButton = editScheduleDialog.findViewById(R.id.add_schedule_ok_button);
        editScheduleAddButton = editScheduleDialog.findViewById(R.id.add_schedule_add_button);
        editScheduleSortButton = editScheduleDialog.findViewById(R.id.add_schedule_sort_button);

        //init add schedule recycler view
        editScheduleAdapter = new EditScheduleAdapter(schedules);
        editScheduleCallback = new ItemDraggedOrSwipedCallback();
        editScheduleCallback.setListener(editScheduleAdapter);
        editScheduleTouchHelper = new ItemTouchHelper(editScheduleCallback);
        editScheduleAdapter.setItemTouchHelper(editScheduleTouchHelper);
        editScheduleRecyclerView.setAdapter(editScheduleAdapter);
        editScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        editScheduleRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        editScheduleTouchHelper.attachToRecyclerView(editScheduleRecyclerView);

        //add events
        editScheduleAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = editScheduleAdapter.getItemCount();
                schedules.add(new Schedule("", "", startTimeEditText.getText().toString(), "", 0));
                editScheduleAdapter.notifyItemInserted(i);
            }
        });

        editScheduleOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editScheduleDialog.dismiss();
            }
        });

        editScheduleSortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScheduleRepository.sortSchedulesByStartTime(schedules);
                editScheduleAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initEditTaskDialog() {
        tasks = new ArrayList<>();

        editTaskDialog = new Dialog(this);
        editTaskDialog.setContentView(R.layout.dialog_edit_task);

        editTaskRecyclerView = editTaskDialog.findViewById(R.id.edit_task_dialog_recycler_view);
        editTaskOkButton = editTaskDialog.findViewById(R.id.edit_task_dialog_ok_button);
        editTaskAddButton = editTaskDialog.findViewById(R.id.edit_task_dialog_add_button);
        editTaskSortButton = editTaskDialog.findViewById(R.id.edit_task_dialog_sort_button);
        editTaskCompletedTextView = editTaskDialog.findViewById(R.id.edit_task_dialog_completed_text_view);
        editTaskProgressBar = editTaskDialog.findViewById(R.id.edit_task_dialog_progress_bar);

        editTaskAdapter = new EditTaskAdapter(tasks);
        editTaskAdapter.setListener(this);
        editTaskCallback = new ItemDraggedOrSwipedCallback();
        editTaskCallback.setListener(editTaskAdapter);
        editTaskTouchHelper = new ItemTouchHelper(editTaskCallback);
        editTaskAdapter.setItemTouchHelper(editTaskTouchHelper);
        editTaskRecyclerView.setAdapter(editTaskAdapter);
        editTaskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        editTaskRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        editTaskTouchHelper.attachToRecyclerView(editTaskRecyclerView);

        //add events
        editTaskAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = editTaskAdapter.getItemCount();
                tasks.add(new Task("", "", startDateEditText.getText().toString(), "", "", false, 0));
                editTaskAdapter.notifyItemInserted(i);
                updateEditTaskDialogHeader();
            }
        });

        editTaskOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTaskDialog.dismiss();
            }
        });

        editTaskSortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskRepository.sortTasksByStartDateTime(tasks);
                editTaskAdapter.notifyDataSetChanged();
            }
        });

    }

    private void initSelectReminderDialog() {
        selectReminderDialog = new Dialog(this);
        selectReminderDialog.setContentView(R.layout.dialog_select_reminder);

        //Connect views
        selectReminderListView = selectReminderDialog.findViewById(R.id.select_reminder_list_view);
        selectReminderOkButton = selectReminderDialog.findViewById(R.id.select_reminder_ok_button);

        selectReminderAdapter = new SelectReminderAdapter(this, selectedReminders);
        selectReminderAdapter.setListener(this);
        selectReminderListView.setAdapter(selectReminderAdapter);

        //Add events
        selectReminderOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editReminderAdapter.notifyDataSetChanged();
                selectReminderDialog.dismiss();
            }
        });
    }

    private void initSelectEmployeeDialog() {
        selectEmployeeDialog = new Dialog(this);
        selectEmployeeDialog.setContentView(R.layout.dialog_select_employee);

        //Connect views
        selectEmployeeRecyclerView = selectEmployeeDialog.findViewById(R.id.select_employee_recycler_view);
        selectEmployeeOkButton = selectEmployeeDialog.findViewById(R.id.add_schedule_ok_button);

        employees = EmployeeRepository.getInstance().getEmployeesBySearchString("");
        selectEmployeeAdapter = new SelectEmployeeAddEventAdapter(selectedEmployeesIds,
                employees);
        selectEmployeeAdapter.setListener(this);
        selectEmployeeRecyclerView.setAdapter(selectEmployeeAdapter);
        selectEmployeeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectEmployeeRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        //SEARCH employees
        selectEmployeeSearchView = selectEmployeeDialog.findViewById(R.id.select_employee_search_view);
        selectEmployeeSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Employee> resultEmployees = EmployeeRepository.getInstance().getEmployeesBySearchString(newText);
                employees.clear();
                employees.addAll(resultEmployees);
                selectEmployeeAdapter.notifyDataSetChanged();
                return true;
            }
        });

        //Add events
        selectEmployeeOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editEmployeeAdapter.notifyDataSetChanged();
                selectEmployeeDialog.dismiss();
            }
        });
    }

    private void addEvents() {
        selectEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectEmployeeDialog();
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

        conflictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedEmployeesIds.size() > 0) {
                    conflictButton.setEnabled(false);
                    startTime = startDateEditText.getText().toString() + " - " + startTimeEditText.getText().toString();
                    endTime = endDateEditText.getText().toString() + " - " + endTimeEditText.getText().toString();
                    checkForConflict();
                } else {
                    Toast.makeText(context, "Xin mời chọn nhân sự trước", Toast.LENGTH_SHORT).show();
                }
            }
        });

        taskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTaskDialog();
            }
        });

        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddScheduleDialog();
            }
        });

        selectReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectReminderAdapter.notifyDataSetChanged();
                showAddReminderDialog();
            }
        });
    }

    private void checkForConflict() {
        try {
            calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString()));
            calendar2.setTime(CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString()));
            calendar.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar2.get(Calendar.MINUTE));
            startMili = calendar.getTimeInMillis();

            calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString()));
            calendar2.setTime(CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString()));
            calendar.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar2.get(Calendar.MINUTE));
            endMili = calendar.getTimeInMillis();

            EventRepository.getInstance().getConflictEventsIdsAdd(startMili, endMili, selectedEmployeesIds,
                    new EventRepository.MyConflictEventCallback() {
                        @Override
                        public void onCallback(HashMap<String, ArrayList<String>> conflictMap) {
                            conflictsMap.clear();
                            conflictsMap.putAll(conflictMap);
                            editEmployeeAdapter.notifyDataSetChanged();
                            conflictButton.setEnabled(true);
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAddScheduleDialog() {
        if (!isFinishing()) {
            editScheduleDialog.show();
            if (editScheduleDialog.getWindow() != null) {
                editScheduleDialog.getWindow().setAttributes(lWindowParams);
            }
        }
    }

    private void showEditTaskDialog() {
        if (!isFinishing()) {
            editTaskDialog.show();
            if (editTaskDialog.getWindow() != null) {
                editTaskDialog.getWindow().setAttributes(lWindowParams);
            }
            if (tasks.size() > 0) {
                count = 0;
                for (Task t : tasks) {
                    if (t.isDone()) {
                        count++;
                    }
                }
                int progress = 100 * count / tasks.size();
                editTaskProgressBar.setProgress(progress);
                String progressString = String.format(getResources().getString(R.string.task_progress), count, tasks.size());
                editTaskCompletedTextView.setText(progressString);
            } else {
                editTaskProgressBar.setProgress(0);
                editTaskCompletedTextView.setText("");
            }
        }
    }

    private void showSelectEmployeeDialog() {
        if (!isFinishing()) {
            selectEmployeeDialog.show();
            if (selectEmployeeDialog.getWindow() != null) {
                selectEmployeeDialog.getWindow().setAttributes(lWindowParams);
            }
        }
    }

    private void showAddReminderDialog() {
        if (!isFinishing()) {
            selectReminderDialog.show();
            if (selectReminderDialog.getWindow() != null) {
                selectReminderDialog.getWindow().setAttributes(lWindowParams);
            }
        }
    }

    //SAVE EVENT
    //----------------------------------------------------------------------------------------------
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
            for (String employeeId : selectedEmployeesIds) {
                Salary tempSalary = new Salary("",
                        "",
                        employeeId,
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

            for (int i = 0; i < editScheduleRecyclerView.getChildCount(); i++) {
                schedules.get(i).setOrder(i);
            }
            for (int i = 0; i < editTaskRecyclerView.getChildCount(); i++) {
                tasks.get(i).setOrder(i);
            }

            EventRepository.getInstance().addEventToDatabase(event, salaries, tasks, schedules, selectedReminders);
            context.finish();

        } else {
            if (titleEditText.getText().toString().isEmpty()) {
                titleEditText.setError("Xin mời nhập");
            } else {
                titleEditText.setError(null);
            }
        }
    }
    //----------------------------------------------------------------------------------------------

    //ACTIVITY STUFFS
    //----------------------------------------------------------------------------------------------
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
    //----------------------------------------------------------------------------------------------

    //EDIT TASK LIST
    //----------------------------------------------------------------------------------------------

    private void updateEditTaskDialogHeader() {
        if (tasks.size() > 0) {
            int count = 0;
            for (Task t : tasks) {
                if (t.isDone()) {
                    count++;
                }
            }
            int progress = 100 * count / tasks.size();
            editTaskProgressBar.setProgress(progress);
            String progressString = String.format(getResources().getString(R.string.task_progress), count, tasks.size());
            editTaskCompletedTextView.setText(progressString);
        } else {
            editTaskProgressBar.setProgress(0);
            editTaskCompletedTextView.setText("");
        }
    }

    @Override
    public void onEditTaskItemCheckBoxClicked(boolean isChecked) {
        updateEditTaskDialogHeader();
    }

    @Override
    public void onEditTaskItemRemoved() {
        updateEditTaskDialogHeader();
    }
    //----------------------------------------------------------------------------------------------

    //SELECT EMPLOYEE DIALOG
    //----------------------------------------------------------------------------------------------
    @Override
    public void onCheckBoxClicked(String employeeId, boolean isChecked) {
        if (isChecked) {
            selectedEmployeesIds.add(employeeId);
            conflictsMap.put(employeeId, null);
        } else {
            selectedEmployeesIds.remove(employeeId);
            conflictsMap.remove(employeeId);
        }
    }

    @Override
    public void onDeleteButtonClicked(String employeeId) {
        selectedEmployeesIds.remove(employeeId);
        conflictsMap.remove(employeeId);
        editEmployeeAdapter.notifyDataSetChanged();
        selectEmployeeAdapter.notifyDataSetChanged();
    }
    //----------------------------------------------------------------------------------------------

    //EDIT EMPLOYEE LIST
    //----------------------------------------------------------------------------------------------
    @Override
    public void onListItemClicked(String employeeId) {
        ArrayList<String> conflictEvents = conflictsMap.get(employeeId);
        if (conflictEvents != null) {
            if (conflictEvents.size() > 0) {
                //Show conflict activity
                Intent conflictIntent = new Intent(this, ShowConflictActivity.class);
                conflictIntent.putExtra(Constants.INTENT_EMPLOYEE_ID, employeeId);
                conflictIntent.putExtra(Constants.INTENT_START_TIME, startTime);
                conflictIntent.putExtra(Constants.INTENT_END_TIME, endTime);
                conflictIntent.putExtra(Constants.INTENT_CONFLICT_EVENTS_IDS, conflictEvents);
                startActivity(conflictIntent);
            }
        }
    }
    //----------------------------------------------------------------------------------------------

    //EDIT REMINDER LIST
    //----------------------------------------------------------------------------------------------
    @Override
    public void onReminderClearButtonClicked(int minute) {
        for (Reminder r : selectedReminders) {
            if (r.getMinute() == minute) {
                selectedReminders.remove(r);
                editReminderAdapter.notifyDataSetChanged();
                return;
            }
        }
    }
    //----------------------------------------------------------------------------------------------

    //SELECT REMINDER LIST
    //----------------------------------------------------------------------------------------------
    @Override
    public void onSelectReminderCheckBoxClicked(int minute, boolean isChecked) {
        if (isChecked) {
            selectedReminders.add(new Reminder("", "", minute, ""));
        } else {
            for (Reminder r : selectedReminders) {
                if (r.getMinute() == minute) {
                    selectedReminders.remove(r);
                    editReminderAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }
    //----------------------------------------------------------------------------------------------
}
