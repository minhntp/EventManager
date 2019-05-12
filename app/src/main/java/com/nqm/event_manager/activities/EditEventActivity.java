package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
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

import com.nqm.event_manager.adapters.AddScheduleAdapter;
import com.nqm.event_manager.adapters.DeleteEmployeeFromEditEventAdapter;
import com.nqm.event_manager.adapters.SelectEmployeeInEditEventAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.R;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class EditEventActivity extends AppCompatActivity {
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

    DeleteEmployeeFromEditEventAdapter deleteEmployeeAdapter;
    AddScheduleAdapter addScheduleAdapter;

    Calendar calendar = Calendar.getInstance();

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        context = this;

        connectViews();

        eventId = getIntent().getStringExtra("eventId");
        event = EventRepository.getInstance(null).getAllEvents().get(eventId);

        selectedEmployeesIds = EmployeeRepository.getInstance(null).getEmployeesIdsByEventId(eventId);
        schedules = ScheduleRepository.getInstance(null).getSchedulesInArrayListByEventId(eventId);
        Log.d("debug", "got " + schedules.size() + " schedules for current event");

        deleteEmployeeAdapter = new DeleteEmployeeFromEditEventAdapter(this, eventId,
                selectedEmployeesIds);
        addScheduleAdapter = new AddScheduleAdapter(this, schedules);

        fillInformation();
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
        getSupportActionBar().setTitle("Chỉnh sửa sự kiện");
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

    private void fillInformation() {
        titleEditText.setText(event.getTen());
        startDateEditText.setText(event.getNgayBatDau());
        startTimeEditText.setText(event.getGioBatDau());
        endDateEditText.setText(event.getNgayKetThuc());
        endTimeEditText.setText(event.getGioKetThuc());

        try {
            startDowTextView.setText(CalendarUtil.sdfDayOfWeek
                    .format(CalendarUtil.sdfDayMonthYear.parse(startDateEditText.getText().toString())));
            endDowTextView.setText(CalendarUtil.sdfDayOfWeek
                    .format(CalendarUtil.sdfDayMonthYear.parse(endDateEditText.getText().toString())));
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
                openEditScheduleDialog();
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
                        endDowTextView.setText(CalendarUtil.sdfDayOfWeek.format(calendar.getTime()));
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
                int HH = calendar.get(Calendar.HOUR_OF_DAY);
                int mm = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(EditEventActivity.this, timeSetListener, HH,
                        mm, false).show();
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
                int HH = calendar.get(Calendar.HOUR_OF_DAY);
                int mm = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(EditEventActivity.this, timeSetListener, HH,
                        mm, false).show();
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
        if (startTimeEditText.getText().toString().isEmpty()) {
            startTimeEditText.setError("");
            return;
        } else {
            startTimeEditText.setError(null);
        }
        if (endTimeEditText.getText().toString().isEmpty()) {
            endTimeEditText.setError("");
            return;
        } else {
            endTimeEditText.setError(null);
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

//    private void saveToDatabaseOld(Event editedEvent) {
//        //Update event
//        EventRepository.getInstance(null).updateEventToDatabase(editedEvent, new EventRepository.MyUpdateEventCallback() {
//            @Override
//            public void onCallback(boolean updateSucceed) {
//                ScheduleRepository.getInstance(null).deleteSchedulesByEventId(eventId, new ScheduleRepository.MyDeleteSchedulesByEventIdCallback() {
//                    @Override
//                    public void onCallback(boolean deleteSucceed) {
//                        //Delete schedules done -> Add new list of schedules
//                        for (int i = 0; i < schedules.size(); i++) {
//                            schedules.get(i).setEventId(eventId);
//                            final int tempI = i;
//                            ScheduleRepository.getInstance(null).addScheduleToDatabase(schedules.get(i), new ScheduleRepository.MyAddScheduleCallback() {
//                                @Override
//                                public void onCallback(String scheduleId) {
//                                    if (tempI == schedules.size() - 1) {
//                                        ArrayList<String> unchangedEmployeesIds = EmployeeRepository.getInstance(null)
//                                                .getEmployeesIdsByEventId(eventId);
//
//                                        ArrayList<String> mergedEmployeesIds = new ArrayList<>();
//                                        mergedEmployeesIds.addAll(unchangedEmployeesIds);
//                                        for (String id : selectedEmployeesIds) {
//                                            if (!mergedEmployeesIds.contains(id)) {
//                                                mergedEmployeesIds.add(id);
//                                            }
//                                        }
//
//                                        Log.d("debug", "got unchanged employees Ids size = " + unchangedEmployeesIds.size());
//                                        Log.d("debug", "merged employees Ids size = " + mergedEmployeesIds.size());
//                                        addNewSalariesAndDeleteOldSalaries(unchangedEmployeesIds, mergedEmployeesIds);
//                                    }
//                                }
//                            });
//                        }
//                    }
//                });
//            }
//        });
//    }

//    private void addNewSalariesAndDeleteOldSalaries(final ArrayList<String> unchangedEmployeesIds, ArrayList<String> mergedEmployeesIds) {
//        Log.d("debug", "selected employees size = " + selectedEmployeesIds.size());
//        ArrayList<String> toBeRemovedEmployeesIds = new ArrayList<>();
//        ArrayList<String> toBeAddedEmployeesIds = new ArrayList<>();
//        for (String id : mergedEmployeesIds) {
//            if (!selectedEmployeesIds.contains(id)) {
//                toBeRemovedEmployeesIds.add(id);
//            } else if (!unchangedEmployeesIds.contains(id)) {
//                toBeAddedEmployeesIds.add(id);
//            }
//        }

//        final ArrayList<String> tempToBeAddedEmployeesIds = new ArrayList<>();
//        tempToBeAddedEmployeesIds.addAll(toBeAddedEmployeesIds);
//        final ArrayList<String> tempToBeRemovedEmployeesIds = new ArrayList<>();
//        tempToBeRemovedEmployeesIds.addAll(toBeRemovedEmployeesIds);
//
//        if (tempToBeAddedEmployeesIds.size() > 0) {
//            for (final String addId : tempToBeAddedEmployeesIds) {
//                //Add new salaries - contained in selected but not in unchanged
//                SalaryRepository.getInstance(null).addSalaryToDatabase(
//                        new Salary("", eventId, addId, 0, false),
//                        new SalaryRepository.MyAddSalaryCallback() {
//                            @Override
//                            public void onCallback(String salaryId) {
//                                Log.d("debug", "add new salaries");
////                                if (tempI == selectedEmployeesIds.size() - 1) {
////                                    deleteOldSalaries(unchangedEmployeesIds);
////                                }
//                                if (tempToBeAddedEmployeesIds.indexOf(addId) == (tempToBeAddedEmployeesIds.size() - 1)) {
//                                    deleteOldSalaries(tempToBeRemovedEmployeesIds);
//                                }
//                            }
//                        });
//            }
//        } else {
//            deleteOldSalaries(tempToBeRemovedEmployeesIds);
//        }
//    }
//
//    private void deleteOldSalaries(final ArrayList<String> toBeRemovedEmployeesIds) {
//        if (toBeRemovedEmployeesIds.size() > 0) {
//            for (final String removeId : toBeRemovedEmployeesIds) {
//                SalaryRepository.getInstance(null).deleteSalaryByEventIdAndEmployeeId(eventId,
//                        removeId, new SalaryRepository.MyDeleteSalaryByEventIdAndEmployeeIdCallback() {
//                            @Override
//                            public void onCallback(boolean deleteSucceed) {
//                                Log.d("debug", "delete old salaries");
//                                if (toBeRemovedEmployeesIds.indexOf(removeId) == (toBeRemovedEmployeesIds.size() - 1)) {
//                                    Intent intent = new Intent();
//                                    intent.putExtra("edit event", true);
//                                    intent.putExtra("edit event succeed", true);
//                                    setResult(RESULT_OK, intent);
//                                    Log.d("debug", "EditEventActivity: update event complete");
//                                    ((Activity) context).finish();
//                                }
//                            }
//                        });
//            }
//        } else {
//            Intent intent = new Intent();
//            intent.putExtra("edit event", true);
//            intent.putExtra("edit event succeed", true);
//            setResult(RESULT_OK, intent);
//            Log.d("debug", "EditEventActivity: update event complete");
//            ((Activity) context).finish();
//        }
//    }

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

    private void openEditScheduleDialog() {
        final Dialog addScheduleDialog = new Dialog(this);
        addScheduleDialog.setContentView(R.layout.dialog_add_schedule);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(addScheduleDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //Connect views
        final ListView addScheduleListView = addScheduleDialog.findViewById(R.id.add_schedule_dialog_schedule_list_view);
        Button okButton = addScheduleDialog.findViewById(R.id.ok_button);
        Button addScheduleButton = addScheduleDialog.findViewById(R.id.add_schedule_add_button);

        addScheduleListView.setAdapter(addScheduleAdapter);

        //Add events
        addScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schedules.clear();
                for (int i = 0; i < addScheduleListView.getChildCount(); i++) {
                    EditText timeEditText = addScheduleListView.getChildAt(i).findViewById(R.id.add_schedule_time_edit_text);
                    EditText contentEditText = addScheduleListView.getChildAt(i).findViewById(R.id.add_schedule_content_edit_text);
                    String time = timeEditText.getText().toString();
                    String content = contentEditText.getText().toString().trim();
                    if (!time.isEmpty() || !content.isEmpty()) {
                        schedules.add(new Schedule("", "", time, content));
                    }
                }
                schedules.add(new Schedule());
                addScheduleAdapter.notifyDataSetChanged();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schedules.clear();
                for (int i = 0; i < addScheduleListView.getChildCount(); i++) {
                    EditText timeEditText = addScheduleListView.getChildAt(i).findViewById(R.id.add_schedule_time_edit_text);
                    EditText contentEditText = addScheduleListView.getChildAt(i).findViewById(R.id.add_schedule_content_edit_text);
                    String time = timeEditText.getText().toString();
                    String content = contentEditText.getText().toString();
                    if (!time.isEmpty() || !content.isEmpty()) {
                        schedules.add(new Schedule("", "", time, content));
                    }
                }
                addScheduleDialog.dismiss();
            }
        });

        if (!isFinishing()) {
            addScheduleDialog.show();
            addScheduleDialog.getWindow().setAttributes(lWindowParams);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
