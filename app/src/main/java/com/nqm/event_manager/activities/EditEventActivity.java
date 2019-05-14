package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.nqm.event_manager.adapters.AddEmployeeFromEditEventAdapter;
import com.nqm.event_manager.adapters.AddScheduleAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeInEditEventAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnCustomViewClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class EditEventActivity extends AppCompatActivity implements IOnCustomViewClicked {
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

    AddEmployeeFromEditEventAdapter deleteEmployeeAdapter;

    WindowManager.LayoutParams lWindowParams;
    AddScheduleAdapter addScheduleAdapter;
    ListView addScheduleListView;
    Button okButton;
    Button addScheduleButton;
    Dialog addScheduleDialog;
    TextView titleTextView;

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
    }

    private void init() {
        context = this;

        eventId = getIntent().getStringExtra("eventId");
        event = EventRepository.getInstance(null).getAllEvents().get(eventId);

        selectedEmployeesIds = EmployeeRepository.getInstance(null).getEmployeesIdsByEventId(eventId);
        schedules = ScheduleRepository.getInstance(null).getSchedulesInArrayListByEventId(eventId);
        Log.d("debug", "got " + schedules.size() + " schedules for current event");

        deleteEmployeeAdapter = new AddEmployeeFromEditEventAdapter(this, eventId,
                selectedEmployeesIds);
        addScheduleAdapter = new AddScheduleAdapter(this, schedules, this);

        fillInformation();

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

        employeeListView.setAdapter(deleteEmployeeAdapter);
    }

    private void addEvents() {
        addEmployeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddEmployeeDialog();
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

                if (currentView == startTimeEditText) {
                    startTimeEditText.setText(CalendarUtil.sdfTime.format(calendar.getTime()));
                } else {
                    endTimeEditText.setText(CalendarUtil.sdfTime.format(calendar.getTime()));
                }
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditEventActivity.this, dateSetListener, y,
                        m, d);
                if (!endDateEditText.getText().toString().isEmpty()) {
                    try {
                        datePickerDialog.getDatePicker().setMaxDate(CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString()).getTime());
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditEventActivity.this, dateSetListener, y,
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
                new TimePickerDialog(EditEventActivity.this, timeSetListener, 18,
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
                new TimePickerDialog(EditEventActivity.this, timeSetListener, 18,
                        0, false).show();
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

        //Update
        EventRepository.getInstance(null).updateEventToDatabase(changedEvent, deleteEmployeesIds,
                addEmployeesIds, schedules, new EventRepository.MyUpdateEventCallback() {
                    @Override
                    public void onCallback(boolean updateEventSucceed) {
                        Intent intent = new Intent();
                        intent.putExtra("edit event", true);
                        intent.putExtra("edit event succeed", true);
                        setResult(RESULT_OK, intent);
                        Log.d("debug", "EditEventActivity: update event complete");
                        ((Activity) context).finish();
                    }
                });
    }

    private void openAddEmployeeDialog() {
        final Dialog addEmployeeDialog = new Dialog(this);
        addEmployeeDialog.setContentView(R.layout.dialog_select_employee);

        final SelectEmployeeInEditEventAdapter selectEmployeeInAddEventAdapter =
                new SelectEmployeeInEditEventAdapter(this, eventId, selectedEmployeesIds);
        final ListView selectEmployeeListView = addEmployeeDialog.findViewById(R.id.select_employee_list_view);
        Button cancelButton = addEmployeeDialog.findViewById(R.id.cancel_button);
        Button okButton = addEmployeeDialog.findViewById(R.id.ok_button);

        selectEmployeeListView.setAdapter(selectEmployeeInAddEventAdapter);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox tempCheckbox;
                for (int i = 0; i < selectEmployeeListView.getChildCount(); i++) {
                    tempCheckbox = selectEmployeeListView.getChildAt(i).findViewById(R.id.add_employee_checkbox);
                    if (tempCheckbox.isChecked() &&
                            !selectedEmployeesIds.contains(selectEmployeeInAddEventAdapter.getAllEmployeesIds()[i])) {
                        selectedEmployeesIds.add(selectEmployeeInAddEventAdapter.getAllEmployeesIds()[i]);
                    }
                    if (!tempCheckbox.isChecked() &&
                            selectedEmployeesIds.contains(selectEmployeeInAddEventAdapter.getAllEmployeesIds()[i])) {
                        selectedEmployeesIds.remove(selectEmployeeInAddEventAdapter.getAllEmployeesIds()[i]);
                    }
                }
                deleteEmployeeAdapter.notifyDataSetChanged();
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

    private void showEditScheduleDialog() {
        if (!isFinishing()) {
            addScheduleDialog.show();
            addScheduleDialog.getWindow().setAttributes(lWindowParams);
        }
    }

    @Override
    public void onDeleteButtonClicked(int position) {
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

    @Override
    public boolean onSupportNavigateUp() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Trở về mà không lưu")
                .setMessage("Bạn có chắc chắn không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra("edit event", false);
                        setResult(RESULT_OK, intent);
                        context.finish();
                    }

                })
                .show();
        return super.onSupportNavigateUp();
    }
}
