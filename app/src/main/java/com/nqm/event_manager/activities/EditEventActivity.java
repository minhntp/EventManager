package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.EditEmployeeEditEventAdapter;
import com.nqm.event_manager.adapters.EditReminderAdapter;
import com.nqm.event_manager.adapters.EditScheduleAdapter;
import com.nqm.event_manager.adapters.EditTaskAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeEditEventAdapter;
import com.nqm.event_manager.adapters.SelectReminderAdapter;
import com.nqm.event_manager.callbacks.ItemDraggedOrSwipedCallback;
import com.nqm.event_manager.custom_views.CustomDatePicker;
import com.nqm.event_manager.interfaces.IOnCustomDatePickerItemClicked;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnEditEmployeeItemClicked;
import com.nqm.event_manager.interfaces.IOnEditReminderItemClicked;
import com.nqm.event_manager.interfaces.IOnEditTaskItemClicked;
import com.nqm.event_manager.interfaces.IOnSelectEmployeeItemClicked;
import com.nqm.event_manager.interfaces.IOnSelectReminderItemClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.EventTask;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ReminderRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.repositories.TaskRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.EmployeeUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class EditEventActivity extends BaseActivity implements IOnSelectEmployeeItemClicked,
        IOnEditEmployeeItemClicked, IOnDataLoadComplete, IOnSelectReminderItemClicked,
        IOnEditReminderItemClicked, IOnEditTaskItemClicked, IOnCustomDatePickerItemClicked {
    androidx.appcompat.widget.Toolbar toolbar;

    EditText startDateEditText, startTimeEditText, endDateEditText, endTimeEditText, noteEditText;
    AutoCompleteTextView titleAutoCompleteTextView, locationAutoCompleteTextView;
    ArrayAdapter<String> titleAdapter, locationAdapter;
    TextView startDowTextView, endDowTextView;
    Button selectEmployeesButton, taskButton, scheduleButton;

    TimePickerDialog.OnTimeSetListener timeSetListener;

    String eventId;
    Event event;

    Dialog datePickerDialog;
    TextView datePickerDialogDateTextView;
    CustomDatePicker datePicker;
    Button datePickerDialogOkButton, datePickerDialogCancelButton;
    TextView selectedDowTextView;
    EditText selectedTimeEditText;
    EditText selectedDateEditText;

    Button conflictButton;
    ArrayList<String> selectedEmployeesIds;
    HashMap<String, ArrayList<String>> conflictsMap;
    RecyclerView editEmployeeRecyclerView;
    EditEmployeeEditEventAdapter editEmployeeAdapter;

    ArrayList<EventTask> eventTasks;
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
    Dialog editScheduleDialog;
    Button editScheduleOkButton, editScheduleAddButton, editScheduleSortButton;
    EditScheduleAdapter editScheduleAdapter;
    RecyclerView editScheduleRecyclerView;
    ItemDraggedOrSwipedCallback editScheduleCallback;
    ItemTouchHelper editScheduleTouchHelper;

    ArrayList<Reminder> selectedReminders;
    RecyclerView editReminderRecyclerView;
    EditReminderAdapter editReminderAdapter;
    Button selectReminderButton;

    ArrayList<Employee> employees;
    Dialog selectEmployeeDialog;
    SearchView selectEmployeeSearchView;
    Button selectEmployeeOkButton;
    RecyclerView selectEmployeeRecyclerView;
    SelectEmployeeEditEventAdapter selectEmployeeAdapter;

    Dialog selectReminderDialog;
    ListView selectReminderListView;
    Button selectReminderOkButton;
    SelectReminderAdapter selectReminderAdapter;

    String startTime, endTime;

    Calendar calendar = Calendar.getInstance();
    Calendar calendar2 = Calendar.getInstance();
    long startMili = 0, endMili = 0;

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
            if (titleAutoCompleteTextView.getText().toString().isEmpty()) {
                titleAutoCompleteTextView.setError("Xin mời nhập");
            } else {
                titleAutoCompleteTextView.setError(null);
                if (selectedEmployeesIds.size() > 0) {
//                    Log.d("debug", "here1. conflict size = " + conflictsMap.size());
                    checkAndUpdate();
                } else {
                    updateEventToDatabase();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectViews() {
        toolbar = findViewById(R.id.edit_event_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.edit_event_activity_label);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

//        titleEditText = findViewById(R.id.edit_event_title_edit_text);
        startDateEditText = findViewById(R.id.edit_event_start_date_edit_text);
        startTimeEditText = findViewById(R.id.edit_event_start_time_edit_text);
        endDateEditText = findViewById(R.id.edit_event_end_date_edit_text);
        endTimeEditText = findViewById(R.id.edit_event_end_time_edit_text);
        titleAutoCompleteTextView = findViewById(R.id.edit_event_title_auto_complete_text_view);
        locationAutoCompleteTextView = findViewById(R.id.edit_event_location_auto_complete_text_view);


        startDowTextView = findViewById(R.id.edit_event_start_dow_text_view);
        endDowTextView = findViewById(R.id.edit_event_end_dow_text_view);

//        locationEditText = findViewById(R.id.edit_event_location_edit_text);
        noteEditText = findViewById(R.id.edit_event_note_edit_text);

        conflictButton = findViewById(R.id.edit_event_conflict_button);
        selectEmployeesButton = findViewById(R.id.edit_event_add_employee_button);
        taskButton = findViewById(R.id.edit_event_task_button);
        scheduleButton = findViewById(R.id.edit_event_schedule_button);

        editEmployeeRecyclerView = findViewById(R.id.edit_event_employee_recycler_view);

        editReminderRecyclerView = findViewById(R.id.edit_event_reminder_list_view);
        selectReminderButton = findViewById(R.id.edit_event_add_reminder_button);
    }

    private void init() {
        context = this;
        ReminderRepository.getInstance().setListener(this);

        eventId = getIntent().getStringExtra(Constants.INTENT_EVENT_ID);
        event = EventRepository.getInstance().getAllEvents().get(eventId);

        titleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                EventRepository.getInstance().getTitles());
        titleAutoCompleteTextView.setAdapter(titleAdapter);

        locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                EventRepository.getInstance().getLocations());
        locationAutoCompleteTextView.setAdapter(locationAdapter);

        initDatePickerDialog();

        selectedEmployeesIds = EmployeeRepository.getInstance().getEmployeesIdsByEventId(eventId);
        conflictsMap = new HashMap<>();
        for (String id : selectedEmployeesIds) {
            conflictsMap.put(id, new ArrayList<String>());
        }
        editEmployeeAdapter = new EditEmployeeEditEventAdapter(eventId, selectedEmployeesIds, conflictsMap);
        editEmployeeAdapter.setListener(this);
        editEmployeeRecyclerView.setLayoutManager(new LinearLayoutManager(this) /*{
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        }*/);
        editEmployeeRecyclerView.setAdapter(editEmployeeAdapter);
        editEmployeeRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        fillInformation();

        initEditTaskDialog();
        initEditScheduleDialog();
        initSelectEmployeeDialog();

        selectedReminders = ReminderRepository.getInstance().getRemindersInArrayListByEventId(eventId);
        editReminderAdapter = new EditReminderAdapter(selectedReminders);
        editReminderAdapter.setListener(this);
        LinearLayoutManager linearLayoutManagerReminder = new LinearLayoutManager(this);
        linearLayoutManagerReminder.setOrientation(RecyclerView.VERTICAL);
        editReminderRecyclerView.setLayoutManager(linearLayoutManagerReminder);
        editReminderRecyclerView.setAdapter(editReminderAdapter);

        initSelectReminderDialog();
    }

    private void fillInformation() {
        titleAutoCompleteTextView.setText(event.getTen());
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

        locationAutoCompleteTextView.setText(event.getDiaDiem());
        noteEditText.setText(event.getGhiChu());
    }

    private void initDatePickerDialog() {
        datePickerDialog = new Dialog(this);
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
            String selectedDate = datePicker.getSelectedDateString();
            String selectedDow = CalendarUtil.dayOfWeekInVietnamese(selectedDate);
            selectedDateEditText.setText(selectedDate);
            selectedDowTextView.setText(selectedDow);
            try {
                Date startDate = CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString());
                Date startTime = CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString());
                Date endDate = CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString());
                Date endTime = CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString());

                if (startDate.compareTo(endDate) > 0) {
                    if (selectedDateEditText == startDateEditText) {
                        endDateEditText.setText(selectedDate);
                        endDowTextView.setText(selectedDow);
                    } else {
                        startDateEditText.setText(selectedDate);
                        startDowTextView.setText(selectedDow);
                    }
                    if (startTime.compareTo(endTime) > 0) {
                        endTimeEditText.setText(startTimeEditText.getText().toString());
                    }
                } else if (startDate.compareTo(endDate) == 0) {
                    if (startTime.compareTo(endTime) > 0) {
                        endTimeEditText.setText(startTimeEditText.getText().toString());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            datePickerDialog.dismiss();
        });
    }

    private void initEditScheduleDialog() {
        schedules = ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId);
        ScheduleRepository.sortSchedulesByOrder(schedules);

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

        editScheduleAdapter = new EditScheduleAdapter(schedules);
        editScheduleCallback = new ItemDraggedOrSwipedCallback();
        editScheduleCallback.setListener(editScheduleAdapter);
        editScheduleTouchHelper = new ItemTouchHelper(editScheduleCallback);
        editScheduleAdapter.setItemTouchHelper(editScheduleTouchHelper);
        editScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        editScheduleRecyclerView.setAdapter(editScheduleAdapter);
        editScheduleRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        editScheduleTouchHelper.attachToRecyclerView(editScheduleRecyclerView);

        //add events
        editScheduleAddButton.setOnClickListener(view -> {
            int i = editScheduleAdapter.getItemCount();
            schedules.add(new Schedule("", "", startTimeEditText.getText().toString(), "", i));
            editScheduleAdapter.notifyItemInserted(i);
        });

        editScheduleOkButton.setOnClickListener(view -> editScheduleDialog.dismiss());

        editScheduleSortButton.setOnClickListener(v -> {
            ScheduleRepository.sortSchedulesByStartTime(schedules);
            editScheduleAdapter.notifyDataSetChanged();
        });
    }

    private void initEditTaskDialog() {
        eventTasks = TaskRepository.getInstance().getTasksInArrayListByEventId(eventId);
        TaskRepository.sortTasksByOrder(eventTasks);

        editTaskDialog = new Dialog(this);
        editTaskDialog.setContentView(R.layout.dialog_edit_task);

        editTaskRecyclerView = editTaskDialog.findViewById(R.id.edit_task_dialog_recycler_view);
        editTaskOkButton = editTaskDialog.findViewById(R.id.edit_task_dialog_ok_button);
        editTaskAddButton = editTaskDialog.findViewById(R.id.edit_task_dialog_add_button);
        editTaskSortButton = editTaskDialog.findViewById(R.id.edit_task_dialog_sort_button);
        editTaskCompletedTextView = editTaskDialog.findViewById(R.id.edit_task_dialog_completed_text_view);
        editTaskProgressBar = editTaskDialog.findViewById(R.id.edit_task_dialog_progress_bar);

        editTaskAdapter = new EditTaskAdapter(eventTasks);
        editTaskAdapter.setListener(this);
        editTaskCallback = new ItemDraggedOrSwipedCallback();
        editTaskCallback.setListener(editTaskAdapter);
        editTaskTouchHelper = new ItemTouchHelper(editTaskCallback);
        editTaskAdapter.setItemTouchHelper(editTaskTouchHelper);
        editTaskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        editTaskRecyclerView.setAdapter(editTaskAdapter);
        editTaskRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        editTaskTouchHelper.attachToRecyclerView(editTaskRecyclerView);

        //add events
        editTaskAddButton.setOnClickListener(view -> {
            int i = editTaskAdapter.getItemCount();
            eventTasks.add(new EventTask("", "", startDateEditText.getText().toString(), "", "", false, i));
            editTaskAdapter.notifyItemInserted(i);
            updateEditTaskDialogHeader();
        });

        editTaskOkButton.setOnClickListener(view -> editTaskDialog.dismiss());

        editTaskSortButton.setOnClickListener(v -> {
            TaskRepository.sortTasksByStartDateTime(eventTasks);
            editTaskAdapter.notifyDataSetChanged();
        });

    }

    private void initSelectEmployeeDialog() {
        selectEmployeeDialog = new Dialog(this);
        selectEmployeeDialog.setContentView(R.layout.dialog_select_employee);

        //Connect views
        selectEmployeeRecyclerView = selectEmployeeDialog.findViewById(R.id.select_employee_recycler_view);
        selectEmployeeOkButton = selectEmployeeDialog.findViewById(R.id.add_schedule_ok_button);

        employees = EmployeeRepository.getInstance().getEmployeesBySearchString("");
        selectEmployeeAdapter = new SelectEmployeeEditEventAdapter(selectedEmployeesIds,
                employees, eventId);
        selectEmployeeAdapter.setListener(this);
        selectEmployeeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        selectEmployeeRecyclerView.setAdapter(selectEmployeeAdapter);
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
        selectEmployeeOkButton.setOnClickListener(view -> {
//            editEmployeeAdapter.notifyDataSetChanged();
            selectEmployeeDialog.dismiss();
        });

        selectEmployeeDialog.setOnDismissListener(dialog -> editEmployeeAdapter.customNotifyDataSetChanged());
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
        selectReminderOkButton.setOnClickListener(view -> {
//            editReminderAdapter.customNotifyDataSetChanged();
            selectReminderDialog.dismiss();
        });

        selectReminderDialog.setOnDismissListener(dialog -> editReminderAdapter.customNotifyDataSetChanged());

    }

    private void addEvents() {
        selectEmployeesButton.setOnClickListener(view -> showSelectEmployeeDialog());

        taskButton.setOnClickListener(v -> showEditTaskDialog());

        scheduleButton.setOnClickListener(view -> showEditScheduleDialog());

        timeSetListener = (timePicker, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            String selectedTime = CalendarUtil.sdfTime.format(calendar.getTime());
            selectedTimeEditText.setText(selectedTime);

            //Make sure start date + start time < end date + end time
            try {
                Date startDate = CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString());
                Date startTime = CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString());
                Date endDate = CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString());
                Date endTime = CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString());

                if (startDate.compareTo(endDate) == 0) {
                    if (startTime.compareTo(endTime) > 0) {
                        if (selectedTimeEditText == startTimeEditText) {
                            endTimeEditText.setText(selectedTime);
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
        };

        startDateEditText.setOnClickListener(view -> {
            selectedDateEditText = startDateEditText;
            selectedDowTextView = startDowTextView;
            showDatePickerDialog();
        });

        endDateEditText.setOnClickListener(view -> {
            selectedDateEditText = endDateEditText;
            selectedDowTextView = endDowTextView;
            showDatePickerDialog();
        });

        startTimeEditText.setOnClickListener(view -> {
            int hourOfDay = 18;
            int minute = 0;
            calendar = Calendar.getInstance();
            try {
                calendar.setTime(CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString()));
                hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            selectedTimeEditText = startTimeEditText;
            new TimePickerDialog(EditEventActivity.this, timeSetListener, hourOfDay,
                    minute, false).show();
        });

        endTimeEditText.setOnClickListener(view -> {
            int hourOfDay = 18;
            int minute = 0;
            calendar = Calendar.getInstance();
            try {
                calendar.setTime(CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString()));
                hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            selectedTimeEditText = endTimeEditText;
            new TimePickerDialog(EditEventActivity.this, timeSetListener, hourOfDay,
                    minute, false).show();
        });

        conflictButton.setOnClickListener(v -> {
            if (selectedEmployeesIds.size() > 0) {
                conflictButton.setEnabled(false);
                startTime = startDateEditText.getText().toString() + " - " + startTimeEditText.getText().toString();
                endTime = endDateEditText.getText().toString() + " - " + endTimeEditText.getText().toString();
                checkForConflict();
            } else {
                Toast.makeText(context, "Xin mời chọn nhân sự trước", Toast.LENGTH_SHORT).show();
            }
        });

        selectReminderButton.setOnClickListener(v -> {
            selectReminderAdapter.notifyDataSetChanged();
            showSelectReminderDialog();
        });
    }

    private void showDatePickerDialog() {
        try {
            Date dateFromEditText = CalendarUtil.sdfDayMonthYear.parse(selectedDateEditText.getText().toString());
            datePicker.setSelectedDate(dateFromEditText);
            datePicker.setViewDate(dateFromEditText);
            String txt = CalendarUtil.sdfDayOfWeek.format(dateFromEditText) +
                    " - " + CalendarUtil.sdfDayMonthYear.format(dateFromEditText);
            datePickerDialogDateTextView.setText(txt);
            datePicker.setEventId(eventId);
            datePickerDialog.show();
            if (datePickerDialog.getWindow() != null) {
                datePickerDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            EventRepository.getInstance().getConflictEventsIdsEdit(startMili, endMili, selectedEmployeesIds,
                    eventId, conflictMap -> {
                        conflictsMap.clear();
                        conflictsMap.putAll(conflictMap);
                        editEmployeeAdapter.customNotifyDataSetChanged();
                        conflictButton.setEnabled(true);
                    }
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showSelectEmployeeDialog() {
        if (!isFinishing()) {
            selectEmployeeAdapter.notifyDataSetChanged();
            selectEmployeeDialog.show();
            if (selectEmployeeDialog.getWindow() != null) {
                selectEmployeeDialog.getWindow().setAttributes(lWindowParams);
            }
        }
    }

    private void showEditTaskDialog() {
        if (!isFinishing()) {
            editTaskDialog.show();
            if (editTaskDialog.getWindow() != null) {
                editTaskDialog.getWindow().setAttributes(lWindowParams);
            }
            if (eventTasks.size() > 0) {
                int count = 0;
                for (EventTask t : eventTasks) {
                    if (t.isDone()) {
                        count++;
                    }
                }
                int progress = 100 * count / eventTasks.size();
                editTaskProgressBar.setProgress(progress);
                String progressString = String.format(getResources().getString(R.string.task_progress), count, eventTasks.size());
                editTaskCompletedTextView.setText(progressString);
            } else {
                editTaskProgressBar.setProgress(0);
                editTaskCompletedTextView.setText("");
            }
        }
    }

    private void showEditScheduleDialog() {
        if (!isFinishing()) {
            editScheduleDialog.show();
            if (editScheduleDialog.getWindow() != null) {
                editScheduleDialog.getWindow().setAttributes(lWindowParams);
            }
        }
    }

    private void showSelectReminderDialog() {
        if (!isFinishing()) {
            selectReminderAdapter.notifyDataSetChanged();
            selectReminderDialog.show();
            if (selectReminderDialog.getWindow() != null) {
                selectReminderDialog.getWindow().setAttributes(lWindowParams);
            }
        }
    }

    //ACTIVITY
    //----------------------------------------------------------------------------------------------
    @Override
    public boolean onSupportNavigateUp() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_error)
                .setTitle("Trở về mà không lưu?")
                .setPositiveButton("Đồng ý", (dialog, which) -> context.finish())
                .setNegativeButton("Hủy", null)
                .show();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }
    //----------------------------------------------------------------------------------------------

    // EDIT TASK LIST
    //----------------------------------------------------------------------------------------------
    private void updateEditTaskDialogHeader() {
        if (eventTasks.size() > 0) {
            int count = 0;
            for (EventTask t : eventTasks) {
                if (t.isDone()) {
                    count++;
                }
            }
            int progress = 100 * count / eventTasks.size();
            editTaskProgressBar.setProgress(progress);
            String progressString = String.format(getResources().getString(R.string.task_progress), count, eventTasks.size());
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

    //SELECT EMPLOYEE LIST
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

    //EDIT EMPLOYEE LIST
    //----------------------------------------------------------------------------------------------
    @Override
    public void onDeleteButtonClicked(String employeeId) {
        selectedEmployeesIds.remove(employeeId);
        conflictsMap.remove(employeeId);
        editEmployeeAdapter.customNotifyDataSetChanged();
//        selectEmployeeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClicked(String employeeId) {
        ArrayList<String> conflictEventsIds = conflictsMap.get(employeeId);
        if (conflictEventsIds != null && conflictEventsIds.size() > 0) {
            //Show conflict activity
            Intent conflictIntent = new Intent(this, ShowConflictActivity.class);
            conflictIntent.putExtra(Constants.INTENT_EMPLOYEE_ID, employeeId);
            conflictIntent.putExtra(Constants.INTENT_START_TIME, startTime);
            conflictIntent.putExtra(Constants.INTENT_END_TIME, endTime);
            conflictIntent.putExtra(Constants.INTENT_CONFLICT_EVENTS_IDS, conflictsMap.get(employeeId));
            startActivity(conflictIntent);
        }
    }
    //----------------------------------------------------------------------------------------------

    @Override
    public void notifyOnLoadComplete() {
        selectedReminders.clear();
        selectedReminders.addAll(ReminderRepository.getInstance().getRemindersInArrayListByEventId(eventId));
        editReminderAdapter.customNotifyDataSetChanged();
    }

    //UPDATE EVENT
    //----------------------------------------------------------------------------------------------
    private void checkAndUpdate() {
        startTime = startDateEditText.getText().toString() + " - " + startTimeEditText.getText().toString();
        endTime = endDateEditText.getText().toString() + " - " + endTimeEditText.getText().toString();
//        Log.d(Constants.DEBUG, "entered checkAndUpdate()");

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

//            Log.d(Constants.DEBUG, "entered checkAndUpdate() try catch");

            EventRepository.getInstance().getConflictEventsIdsEdit(startMili, endMili, selectedEmployeesIds,
                    eventId, conflictMap -> {
//                        Log.d("debug", "here5 conflictMap size = " + conflictMap.size());
                        conflictsMap.clear();
                        conflictsMap.putAll(conflictMap);
                        editEmployeeAdapter.customNotifyDataSetChanged();
                        boolean isConflictExist = false;
                        for (ArrayList<String> arr : conflictMap.values()) {
                            if (arr != null && arr.size() > 0) {
                                isConflictExist = true;
                                break;
                            }
                        }
                        if (isConflictExist) {
                            new androidx.appcompat.app.AlertDialog.Builder(context)
                                    .setIcon(R.drawable.ic_error)
                                    .setTitle("Có xung đột về nhân viên. Vẫn tiếp tục Lưu?")
                                    .setPositiveButton("Đồng ý", (dialog, which) -> updateEventToDatabase())
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        } else {
                            updateEventToDatabase();
                        }
                    });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateEventToDatabase() {
        //Event
        Event changedEvent = new Event(eventId, titleAutoCompleteTextView.getText().toString(),
                startDateEditText.getText().toString(), endDateEditText.getText().toString(),
                startTimeEditText.getText().toString(), endTimeEditText.getText().toString(),
                locationAutoCompleteTextView.getText().toString(), noteEditText.getText().toString());

        //Salaries
        ArrayList<String> unchangedEmployeesIds = EmployeeRepository.getInstance().getEmployeesIdsByEventId(eventId);

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

        for (int i = 0; i < editScheduleRecyclerView.getChildCount(); i++) {
            schedules.get(i).setOrder(i);
        }
        for (int i = 0; i < editTaskRecyclerView.getChildCount(); i++) {
            eventTasks.get(i).setOrder(i);
        }

        EventRepository.getInstance().updateEventToDatabase(changedEvent, deleteEmployeesIds,
                addEmployeesIds, eventTasks, schedules, selectedReminders);
        context.finish();
    }
    //----------------------------------------------------------------------------------------------

    //REMINDER LIST
    //----------------------------------------------------------------------------------------------
    @Override
    public void onReminderClearButtonClicked(int minute) {
        for (Reminder r : selectedReminders) {
            if (r.getMinute() == minute) {
                selectedReminders.remove(r);
                editReminderAdapter.customNotifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void remindersChanged() {

    }

    @Override
    public void onSelectReminderCheckBoxClicked(int minute, boolean isChecked) {
        if (isChecked) {
            selectedReminders.add(new Reminder("", eventId, minute, ""));
        } else {
            for (Reminder r : selectedReminders) {
                if (r.getMinute() == minute) {
                    selectedReminders.remove(r);
//                    editReminderAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    @Override
    public void onCustomDatePickerItemClicked(String selectedDate, String dayOfWeek) {
        datePickerDialogDateTextView.setText(String.format(Locale.US, "%s - %s", dayOfWeek, selectedDate));
    }
    //----------------------------------------------------------------------------------------------
}