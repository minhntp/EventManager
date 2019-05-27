package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.EditEmployeeEditEventAdapter;
import com.nqm.event_manager.adapters.EditReminderAdapter;
import com.nqm.event_manager.adapters.EditScheduleRecyclerAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeEditEventAdapter;
import com.nqm.event_manager.adapters.SelectReminderAdapter;
import com.nqm.event_manager.custom_views.AddScheduleSwipeAndDragCallback;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnAddScheduleViewClicked;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnEditEmployeeViewClicked;
import com.nqm.event_manager.interfaces.IOnEditReminderViewClicked;
import com.nqm.event_manager.interfaces.IOnSelectEmployeeViewClicked;
import com.nqm.event_manager.interfaces.IOnSelectReminderViewClicked;
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
import java.util.HashMap;

public class EditEventActivity extends AppCompatActivity implements IOnAddScheduleViewClicked,
        IOnSelectEmployeeViewClicked, IOnEditEmployeeViewClicked, IOnDataLoadComplete,
        IOnSelectReminderViewClicked, IOnEditReminderViewClicked {
    android.support.v7.widget.Toolbar toolbar;

    EditText titleEditText, startDateEditText, startTimeEditText, endDateEditText, endTimeEditText,
            locationEditText, noteEditText;
    TextView startDowTextView, endDowTextView;
    Button addEmployeesButton, scheduleButton;

    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;
    View currentView;

    String eventId;
    Event event;
    ArrayList<Schedule> schedules;

    Button conflictButton;
    ArrayList<String> selectedEmployeesIds;
    HashMap<String, ArrayList<String>> conflictsMap;
    RecyclerView editEmployeeRecyclerView;
    EditEmployeeEditEventAdapter editEmployeeAdapter;

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

        titleEditText = findViewById(R.id.edit_event_title_edit_text);
        startDateEditText = findViewById(R.id.edit_event_start_date_edit_text);
        startTimeEditText = findViewById(R.id.edit_event_start_time_edit_text);
        endDateEditText = findViewById(R.id.edit_event_end_date_edit_text);
        endTimeEditText = findViewById(R.id.edit_event_end_time_edit_text);

        startDowTextView = findViewById(R.id.edit_event_start_dow_text_view);
        endDowTextView = findViewById(R.id.edit_event_end_dow_text_view);

        locationEditText = findViewById(R.id.edit_event_location_edit_text);
        noteEditText = findViewById(R.id.edit_event_note_edit_text);

        conflictButton = findViewById(R.id.edit_event_conflict_button);
        addEmployeesButton = findViewById(R.id.edit_event_add_employee_button);
        scheduleButton = findViewById(R.id.edit_event_schedule_button);

        editEmployeeRecyclerView = findViewById(R.id.edit_event_employee_recycler_view);

        editReminderListView = findViewById(R.id.edit_event_reminder_list_view);
        selectReminderButton = findViewById(R.id.edit_event_add_reminder_button);
    }

    private void init() {
        context = this;
        ReminderRepository.getInstance().setListener(this);

        eventId = getIntent().getStringExtra("eventId");
        event = EventRepository.getInstance(null).getAllEvents().get(eventId);

        schedules = ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId);
        ScheduleRepository.sortSchedulesByOrder(schedules);

        selectedEmployeesIds = EmployeeRepository.getInstance().getEmployeesIdsByEventId(eventId);
        conflictsMap = new HashMap<>();
        for (String id : selectedEmployeesIds) {
            conflictsMap.put(id, new ArrayList<String>());
        }
        editEmployeeAdapter = new EditEmployeeEditEventAdapter(eventId, selectedEmployeesIds, conflictsMap);
        editEmployeeAdapter.setListener(this);
        editEmployeeRecyclerView.setAdapter(editEmployeeAdapter);
        editEmployeeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fillInformation();

        initAddScheduleDialog();
        initSelectEmployeeDialog();

        selectedReminders = ReminderRepository.getInstance().getRemindersInArrayListByEventId(eventId);
        editReminderAdapter = new EditReminderAdapter(this, selectedReminders);
        editReminderAdapter.setListener(this);
        editReminderListView.setAdapter(editReminderAdapter);

        initSelectReminderDialog();
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

    private void initSelectEmployeeDialog() {
        selectEmployeeDialog = new Dialog(this);
        selectEmployeeDialog.setContentView(R.layout.dialog_select_employee);
        lWindowParams.copyFrom(selectEmployeeDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //Connect views
        selectEmployeeRecyclerView = selectEmployeeDialog.findViewById(R.id.select_employee_recycler_view);
        selectEmployeeOkButton = selectEmployeeDialog.findViewById(R.id.ok_button);

        employees = EmployeeRepository.getInstance().getEmployeesBySearchString("");
        selectEmployeeAdapter = new SelectEmployeeEditEventAdapter(selectedEmployeesIds,
                employees);
        selectEmployeeAdapter.setListener(this);
        selectEmployeeRecyclerView.setAdapter(selectEmployeeAdapter);
        selectEmployeeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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

    private void initSelectReminderDialog() {
        selectReminderDialog= new Dialog(this);
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

    private void addEvents() {
        addEmployeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectEmployeeDialog();
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

        selectReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectReminderAdapter.notifyDataSetChanged();
                showSelectReminderDialog();
            }
        });
    }

    private void checkForConflict() {
        try {
            Calendar calendar = Calendar.getInstance();
            Calendar tempCalendar = Calendar.getInstance();

            calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString()));
            tempCalendar.setTime(CalendarUtil.sdfTime.parse(startTimeEditText.getText().toString()));
            calendar.set(Calendar.HOUR_OF_DAY, tempCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, tempCalendar.get(Calendar.MINUTE));
            String startTime = CalendarUtil.sdfDayMonthYearTime.format(calendar.getTime());

            calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString()));
            tempCalendar.setTime(CalendarUtil.sdfTime.parse(endTimeEditText.getText().toString()));
            calendar.set(Calendar.HOUR_OF_DAY, tempCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, tempCalendar.get(Calendar.MINUTE));
            String endTime = CalendarUtil.sdfDayMonthYearTime.format(calendar.getTime());

            for (int i = 0; i < selectedEmployeesIds.size(); i++) {
                final int tempI = i;
                EventRepository.getInstance().getConflictEventsIds(startTime, endTime, selectedEmployeesIds.get(i), eventId,
                        new EventRepository.MyConflictEventCallback() {
                            @Override
                            public void onCallback(ArrayList<String> conflictEventsIds) {
//                                Log.d("debug", "confict size = " + conflictEventsIds.size());
                                conflictsMap.put(selectedEmployeesIds.get(tempI), conflictEventsIds);
                                if (tempI == selectedEmployeesIds.size() - 1) {
                                    editEmployeeAdapter.notifyDataSetChanged();
                                    conflictButton.setEnabled(true);
                                }
                            }
                        });

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showSelectEmployeeDialog() {
        if (!isFinishing()) {
            selectEmployeeDialog.show();
            selectEmployeeDialog.getWindow().setAttributes(lWindowParams);
        }
    }

    private void showEditScheduleDialog() {
        if (!isFinishing()) {
            addScheduleDialog.show();
            addScheduleDialog.getWindow().setAttributes(lWindowParams);
        }
    }

    private void showSelectReminderDialog() {
        if (!isFinishing()) {
            selectReminderDialog.show();
            selectReminderDialog.getWindow().setAttributes(lWindowParams);
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

    @Override
    public void onListItemClicked(String employeeId) {
        if (conflictsMap.get(employeeId) != null && conflictsMap.get(employeeId).size() > 0) {
            //Show conflict activity
            Intent conflictIntent = new Intent(this, ShowConflictActivity.class);
            conflictIntent.putExtra("employeeId", employeeId);
            conflictIntent.putExtra("startTime", startTime);
            conflictIntent.putExtra("endTime", endTime);
            conflictIntent.putExtra("conflictEventsIds", conflictsMap.get(employeeId));
            startActivity(conflictIntent);
        } else {
            //Do nothing
        }
    }

    @Override
    public void notifyOnLoadComplete() {
        selectedReminders.clear();
        selectedReminders.addAll(ReminderRepository.getInstance().getRemindersInArrayListByEventId(eventId));
        editReminderAdapter.notifyDataSetChanged();
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

    @Override
    public void onSelectReminderCheckBoxClicked(int minute, boolean isChecked) {
        if (isChecked) {
            selectedReminders.add(new Reminder("", eventId, minute, ""));
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
}