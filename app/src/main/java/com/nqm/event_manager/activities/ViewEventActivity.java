package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.EditReminderAdapter;
import com.nqm.event_manager.adapters.SelectReminderAdapter;
import com.nqm.event_manager.adapters.ViewSalaryAdapter;
import com.nqm.event_manager.adapters.ViewScheduleAdapter;
import com.nqm.event_manager.adapters.ViewTaskAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.fragments.ManageEventFragment;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnEditReminderViewClicked;
import com.nqm.event_manager.interfaces.IOnSelectReminderViewClicked;
import com.nqm.event_manager.interfaces.IOnViewSalaryItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.models.Task;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ReminderRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.repositories.TaskRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.ArrayList;
import java.util.Calendar;

public class ViewEventActivity extends AppCompatActivity implements IOnViewSalaryItemClicked,
        IOnDataLoadComplete, IOnEditReminderViewClicked, IOnSelectReminderViewClicked {
    Activity context;

    Button viewScheduleButton, viewTaskButton;
    TextView titleEditText, timeEditText, locationEditText, noteEditText;
    android.support.v7.widget.Toolbar toolbar;

    String eventId;
    Event selectedEvent;

    ArrayList<Salary> salaries;
    ViewSalaryAdapter viewSalaryAdapter;
    CustomListView salaryListView;

    Dialog viewTaskDialog;
    ArrayList<Task> tasks;
    ViewTaskAdapter viewTaskAdapter;
    RecyclerView taskRecyclerView;
    Button taskBackButton;
    TextView taskCompletedTextView;
    ProgressBar taskProgressBar;

    Dialog viewScheduleDialog;
    WindowManager.LayoutParams lWindowParams;
    ArrayList<Schedule> schedules;
    ViewScheduleAdapter viewScheduleAdapter;
    RecyclerView scheduleRecyclerView;
    Button scheduleBackButton;

    ArrayList<Reminder> selectedReminders;
    CustomListView editReminderListView;
    EditReminderAdapter editReminderAdapter;
    Button selectReminderButton;

    Dialog selectReminderDialog;
    ListView selectReminderListView;
    Button selecReminderOkButton;
    SelectReminderAdapter selectReminderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        DatabaseAccess.setDatabaseListener(this);

        connectViews();
        eventId = getIntent().getStringExtra(Constants.INTENT_EVENT_ID);
        init();
        addEvents();

    }

    private void connectViews() {
        toolbar = findViewById(R.id.view_event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.view_event_activity_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        viewTaskButton = findViewById(R.id.view_event_task_button);
        viewScheduleButton = findViewById(R.id.view_event_schedule_button);

        titleEditText = findViewById(R.id.view_event_title_text_view);
        timeEditText = findViewById(R.id.view_event_time_text_view);
        locationEditText = findViewById(R.id.view_event_location_text_view);
        noteEditText = findViewById(R.id.view_event_note_text_view);

        salaryListView = findViewById(R.id.view_event_salaries_listview);

        editReminderListView = findViewById(R.id.view_event_edit_reminder_list_view);
        selectReminderButton = findViewById(R.id.view_event_select_reminder_button);
    }

    private void init() {
        context = this;
        selectedEvent = EventRepository.getInstance().getAllEvents().get(eventId);
        Log.d("debug", "events size = " + EventRepository.getInstance().getAllEvents().size());
        Log.d("debug", "eventId = " + eventId);
//        if (selectedEvent != null) {
        fillInformation();
//        }
        salaries = SalaryRepository.getInstance().getSalariesByEventId(eventId);
//        if (salaries == null) {
//            salaries = new ArrayList<>();
//        }
        viewSalaryAdapter = new ViewSalaryAdapter(this, salaries);
        viewSalaryAdapter.setListener(this);
        salaryListView.setAdapter(viewSalaryAdapter);

        initViewTaskDialog();

        initViewScheduleDialog();
        viewScheduleButton.setEnabled(!(schedules.size() == 0));

        selectedReminders = ReminderRepository.getInstance().getRemindersInArrayListByEventId(eventId);
//        if (selectedReminders == null) {
//            selectedReminders = new ArrayList<>();
//        }
        editReminderAdapter = new EditReminderAdapter(this, selectedReminders);
        editReminderAdapter.setListener(this);
        editReminderListView.setAdapter(editReminderAdapter);

        initSelectReminderDialog();
    }

    private void initViewScheduleDialog() {
        schedules = ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId);
//        if (schedules == null) {
//            schedules = new ArrayList<>();
//        }
        viewScheduleDialog = new Dialog(this);
        viewScheduleDialog.setContentView(R.layout.dialog_view_schedule);

        lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(viewScheduleDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        scheduleRecyclerView = viewScheduleDialog.findViewById(R.id.view_schedule_dialog_recycler_view);
        scheduleBackButton = viewScheduleDialog.findViewById(R.id.back_button);

        viewScheduleAdapter = new ViewScheduleAdapter(schedules);
        scheduleRecyclerView.setAdapter(viewScheduleAdapter);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        scheduleBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewScheduleDialog.dismiss();
            }
        });
    }

    private void initViewTaskDialog() {
        tasks = TaskRepository.getInstance().getTasksInArrayListByEventId(eventId);
//        if (tasks == null) {
//            tasks = new ArrayList<>();
//        }
        viewTaskDialog = new Dialog(this);
        viewTaskDialog.setContentView(R.layout.dialog_view_task);

        taskCompletedTextView = viewTaskDialog.findViewById(R.id.view_task_dialog_completed_text_view);
        taskProgressBar = viewTaskDialog.findViewById(R.id.view_task_dialog_progress_bar);
        taskRecyclerView = viewTaskDialog.findViewById(R.id.view_task_dialog_recycler_view);
        taskBackButton = viewTaskDialog.findViewById(R.id.view_task_dialog_back_button);

        viewTaskAdapter = new ViewTaskAdapter(tasks);
        taskRecyclerView.setAdapter(viewTaskAdapter);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        taskBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewTaskDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_event_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Xóa sự kiện
        if (id == R.id.view_event_action_delete_event) {
            Log.d("debug", "deleting " + eventId);
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Xóa sự kiện")
                    .setMessage("Bạn có chắc chắn không?")
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseAccess.setDatabaseListener(ManageEventFragment.thisListener);
                            EventRepository.getInstance().deleteEventFromDatabase(eventId);
                            context.finish();
                        }
                    })
                    .setNegativeButton("Không", null)
                    .show();
            return true;
        }

        //Chỉnh sửa lương
        if (id == R.id.view_event_action_edit_salaries) {
            if (allSalariesArePaid()) {
                Toast.makeText(context, "Không có bản lương nào hoặc tất cả các bản lương đều " +
                        "đã được trả", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(this, EditSalaryFromViewEventActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            }
            return true;
        }

        //Chỉnh sửa sự kiện
        if (id == R.id.view_event_action_edit_event) {
            Intent intent = new Intent(this, EditEventActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
            return true;
        }

        //Gửi thông báo
        if (id == R.id.view_event_action_send_notification) {
            Intent intent = new Intent(this, SendEventInfo.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean allSalariesArePaid() {
        for (Salary s : salaries) {
            if (!s.isPaid()) {
                return false;
            }
        }
        return true;
    }

    private void fillInformation() {
        titleEditText.setText(selectedEvent.getTen());
        String time = CalendarUtil.dayOfWeekInVietnamese(selectedEvent.getNgayBatDau()) + "  " +
                selectedEvent.getNgayBatDau() + " - " + selectedEvent.getGioBatDau();
        time += "\n" + CalendarUtil.dayOfWeekInVietnamese(selectedEvent.getNgayKetThuc()) + "  " +
                selectedEvent.getNgayKetThuc() + " - " + selectedEvent.getGioKetThuc();
        timeEditText.setText(time);
        locationEditText.setText(selectedEvent.getDiaDiem());
        noteEditText.setText(selectedEvent.getGhiChu());
    }

    private void addEvents() {
        viewScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                schedules = ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId);
                if (schedules.size() > 0) {
                    showViewScheduleDialog();
                } else {
                    Toast.makeText(context, "Sự kiện không có lịch trình nào", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tasks.size() > 0) {
                    showViewTaskDialog();
                } else {
                    Toast.makeText(context, "Sự kiện không có công việc nào", Toast.LENGTH_SHORT).show();
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

    private void showViewScheduleDialog() {
        if (!isFinishing()) {
            viewScheduleDialog.show();
            viewScheduleDialog.getWindow().setAttributes(lWindowParams);
        }
    }

    private void showViewTaskDialog() {
        if (!isFinishing()) {
            viewTaskDialog.show();
            viewTaskDialog.getWindow().setAttributes(lWindowParams);
            int count = 0;
            for (Task t : tasks) {
                if (t.isDone()) {
                    count++;
                }
            }
            taskCompletedTextView.setText("Đã hoàn thành " + count + "/" + tasks.size());
            taskProgressBar.setProgress(100 * count / tasks.size());
        }
    }

    private void initSelectReminderDialog() {
        selectReminderDialog = new Dialog(this);
        selectReminderDialog.setContentView(R.layout.dialog_select_reminder);

        //Connect views
        selectReminderListView = selectReminderDialog.findViewById(R.id.select_reminder_list_view);
        selecReminderOkButton = selectReminderDialog.findViewById(R.id.select_reminder_ok_button);

        selectReminderAdapter = new SelectReminderAdapter(this, selectedReminders);
        selectReminderAdapter.setListener(this);
        selectReminderListView.setAdapter(selectReminderAdapter);

        //Add events
        selecReminderOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editReminderAdapter.notifyDataSetChanged();
                selectReminderDialog.dismiss();
            }
        });

    }

    private void showSelectReminderDialog() {
        if (!isFinishing()) {
            selectReminderDialog.show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onViewSalaryItemClicked(String employeeId) {
        Intent intent = new Intent(this, ViewEmployeeActivity.class);
        intent.putExtra(Constants.INTENT_EMPLOYEE_ID, employeeId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        DatabaseAccess.setDatabaseListener(this);

        selectedEvent = EventRepository.getInstance().getAllEvents().get(eventId);
        fillInformation();

        salaries.clear();
        salaries.addAll(SalaryRepository.getInstance().getSalariesByEventId(eventId));
        viewSalaryAdapter.notifyDataSetChanged();

        tasks.clear();
        tasks.addAll(TaskRepository.getInstance().getTasksInArrayListByEventId(eventId));
        TaskRepository.sortTasksByOrder(tasks);
        viewTaskAdapter.notifyDataSetChanged();
        viewTaskButton.setEnabled(!(tasks.size() == 0));

        schedules.clear();
        schedules.addAll(ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId));
        ScheduleRepository.sortSchedulesByOrder(schedules);
        viewScheduleAdapter.notifyDataSetChanged();
        viewScheduleButton.setEnabled(!(schedules.size() == 0));

        selectedReminders.clear();
        selectedReminders.addAll(ReminderRepository.getInstance().getRemindersInArrayListByEventId(eventId));
        editReminderAdapter.notifyDataSetChanged();

        super.onResume();
    }

    @Override
    protected void onPause() {
        saveRemindersToDatabase();
        super.onPause();
    }

    @Override
    public void notifyOnLoadComplete() {
        selectedEvent = EventRepository.getInstance().getAllEvents().get(eventId);
        fillInformation();

        salaries.clear();
        salaries.addAll(SalaryRepository.getInstance().getSalariesByEventId(eventId));
        viewSalaryAdapter.notifyDataSetChanged();

        tasks.clear();
        tasks.addAll(TaskRepository.getInstance().getTasksInArrayListByEventId(eventId));
        TaskRepository.sortTasksByOrder(tasks);
        viewTaskAdapter.notifyDataSetChanged();
        viewTaskButton.setEnabled(!(tasks.size() == 0));

        schedules.clear();
        schedules.addAll(ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId));
        ScheduleRepository.sortSchedulesByOrder(schedules);
        viewScheduleAdapter.notifyDataSetChanged();
        viewScheduleButton.setEnabled(!(schedules.size() == 0));

        selectedReminders.clear();
        selectedReminders.addAll(ReminderRepository.getInstance().getRemindersInArrayListByEventId(eventId));
        editReminderAdapter.notifyDataSetChanged();
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

    private void saveRemindersToDatabase() {
        for (Reminder r : selectedReminders) {
            Calendar calendar = Calendar.getInstance();
            Calendar calendarTime = Calendar.getInstance();
            try {
                calendar.setTime(CalendarUtil.sdfDayMonthYear.parse(selectedEvent.getNgayBatDau()));
                calendarTime.setTime(CalendarUtil.sdfTime.parse(selectedEvent.getGioBatDau()));
                calendar.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
            } catch (Exception e) {
                e.printStackTrace();
            }
            calendar.add(Calendar.MINUTE, r.getMinute() * (-1));
            r.setTime(CalendarUtil.sdfDayMonthYearTime.format(calendar.getTime()));
        }
        ReminderRepository.getInstance().updateRemindersByEventId(selectedReminders, eventId);
    }
}