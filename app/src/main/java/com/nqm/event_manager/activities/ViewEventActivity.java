package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.EditReminderAdapter;
import com.nqm.event_manager.adapters.SelectReminderAdapter;
import com.nqm.event_manager.adapters.ViewSalaryAdapter;
import com.nqm.event_manager.adapters.ViewScheduleAdapter;
import com.nqm.event_manager.adapters.ViewTaskAdapter;
import com.nqm.event_manager.fragments.ManageEventFragment;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnEditReminderItemClicked;
import com.nqm.event_manager.interfaces.IOnSelectReminderItemClicked;
import com.nqm.event_manager.interfaces.IOnViewSalaryItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.EventTask;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
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

public class ViewEventActivity extends BaseActivity implements IOnViewSalaryItemClicked,
        IOnDataLoadComplete, IOnEditReminderItemClicked, IOnSelectReminderItemClicked {
    Activity context;

    Button viewScheduleButton, viewTaskButton;
    TextView titleEditText, timeEditText, locationEditText, noteEditText;
    androidx.appcompat.widget.Toolbar toolbar;

    String eventId;
    Event selectedEvent;

    ArrayList<Salary> salaries;
    ViewSalaryAdapter viewSalaryAdapter;
    RecyclerView salaryRecyclerView;

    Dialog viewTaskDialog;
    ArrayList<EventTask> eventTasks;
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
    RecyclerView editReminderRecyclerView;
    EditReminderAdapter editReminderAdapter;
    Button selectReminderButton;

    Dialog selectReminderDialog;
    ListView selectReminderListView;
    Button selectReminderOkButton;
    SelectReminderAdapter selectReminderAdapter;
    boolean isRemindersChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        DatabaseAccess.setDatabaseListener(this, context);

        connectViews();
        eventId = getIntent().getStringExtra(Constants.INTENT_EVENT_ID);
        init();
        addEvents();

    }

    private void connectViews() {
        toolbar = findViewById(R.id.view_event_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.view_event_activity_label);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        viewTaskButton = findViewById(R.id.view_event_task_button);
        viewScheduleButton = findViewById(R.id.view_event_schedule_button);

        titleEditText = findViewById(R.id.view_event_title_text_view);
        timeEditText = findViewById(R.id.view_event_time_text_view);
        locationEditText = findViewById(R.id.view_event_location_text_view);
        noteEditText = findViewById(R.id.view_event_note_text_view);

        salaryRecyclerView = findViewById(R.id.view_event_salaries_list_view);

        editReminderRecyclerView = findViewById(R.id.view_event_edit_reminder_recycler_view);
        selectReminderButton = findViewById(R.id.view_event_select_reminder_button);
    }

    private void init() {
        context = this;
        selectedEvent = EventRepository.getInstance().getAllEvents().get(eventId);
//        if (selectedEvent != null) {
        fillInformation();
//        }
        salaries = SalaryRepository.getInstance().getSalariesByEventId(eventId);
//        if (salaries == null) {
//            salaries = new ArrayList<>();
//        }
        viewSalaryAdapter = new ViewSalaryAdapter(salaries);
        viewSalaryAdapter.setListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        salaryRecyclerView.setLayoutManager(linearLayoutManager);
        salaryRecyclerView.setAdapter(viewSalaryAdapter);

        initViewTaskDialog();

        initViewScheduleDialog();
        viewScheduleButton.setEnabled(!(schedules.size() == 0));

        selectedReminders = ReminderRepository.getInstance(context).getRemindersInArrayListByEventId(eventId);
//        if (selectedReminders == null) {
//            selectedReminders = new ArrayList<>();
//        }
        editReminderAdapter = new EditReminderAdapter(selectedReminders);
        editReminderAdapter.setListener(this);
        LinearLayoutManager linearLayoutManagerReminder = new LinearLayoutManager(this);
        linearLayoutManagerReminder.setOrientation(RecyclerView.VERTICAL);
        editReminderRecyclerView.setLayoutManager(linearLayoutManagerReminder);
        editReminderRecyclerView.setAdapter(editReminderAdapter);

        initSelectReminderDialog();
        isRemindersChanged = false;
    }

    private void initViewScheduleDialog() {
        schedules = ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId);
//        if (schedules == null) {
//            schedules = new ArrayList<>();
//        }
        viewScheduleDialog = new Dialog(this);
        viewScheduleDialog.setContentView(R.layout.dialog_view_schedule);

        lWindowParams = new WindowManager.LayoutParams();
        if (viewScheduleDialog.getWindow() != null) {
            lWindowParams.copyFrom(viewScheduleDialog.getWindow().getAttributes());
        }
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        scheduleRecyclerView = viewScheduleDialog.findViewById(R.id.view_schedule_dialog_recycler_view);
        scheduleBackButton = viewScheduleDialog.findViewById(R.id.back_button);

        viewScheduleAdapter = new ViewScheduleAdapter(schedules);
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleRecyclerView.setAdapter(viewScheduleAdapter);
        scheduleRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        scheduleBackButton.setOnClickListener(view -> viewScheduleDialog.dismiss());
    }

    private void initViewTaskDialog() {
        eventTasks = TaskRepository.getInstance().getTasksInArrayListByEventId(eventId);
//        if (eventTasks == null) {
//            eventTasks = new ArrayList<>();
//        }
        viewTaskDialog = new Dialog(this);
        viewTaskDialog.setContentView(R.layout.dialog_view_task);

        taskCompletedTextView = viewTaskDialog.findViewById(R.id.view_task_dialog_completed_text_view);
        taskProgressBar = viewTaskDialog.findViewById(R.id.view_task_dialog_progress_bar);
        taskRecyclerView = viewTaskDialog.findViewById(R.id.view_task_dialog_recycler_view);
        taskBackButton = viewTaskDialog.findViewById(R.id.view_task_dialog_back_button);

        viewTaskAdapter = new ViewTaskAdapter(eventTasks);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskRecyclerView.setAdapter(viewTaskAdapter);
        taskRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        taskBackButton.setOnClickListener(view -> viewTaskDialog.dismiss());
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
//            Log.wtf("debug", "deleting " + eventId);
            deleteEvent();
            return true;
        }

        //Chỉnh sửa lương
        if (id == R.id.view_event_action_edit_salaries) {
            editSalaries();
            return true;
        }

        //Chỉnh sửa sự kiện
        if (id == R.id.view_event_action_edit_event) {
            editEvent();
            return true;
        }

        //Gửi thông báo
        if (id == R.id.view_event_action_send_notification) {
            sendNotification();
            return true;
        }

        if (id == R.id.view_event_action_copy) {
            copyEvent();
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteEvent() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_error)
                .setTitle("Xóa sự kiện")
                .setMessage("Bạn có chắc chắn muốn xóa sự kiện này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    DatabaseAccess.setDatabaseListener(ManageEventFragment.thisListener, context);
                    EventRepository.getInstance().deleteEventFromDatabase(eventId, context);
                    context.finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void editSalaries() {
        if (allSalariesArePaid()) {
            Toast.makeText(context, "Không có bản lương nào hoặc tất cả các bản lương đều " +
                    "đã được trả", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(this, EditSalaryFromViewEventActivity.class);
            intent.putExtra("eventId", eventId);
            startActivity(intent);
        }
    }

    private void editEvent() {
        Intent intent = new Intent(this, EditEventActivity.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    private void sendNotification() {
        Intent intent = new Intent(this, SendEventInfo.class);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }

    private void copyEvent() {
        Intent intent = new Intent(this, AddEventActivity.class);
        intent.putExtra(Constants.INTENT_EVENT_ID, eventId);
        startActivity(intent);
        finish();
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
        viewScheduleButton.setOnClickListener(view -> {
//                schedules = ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId);
            if (schedules.size() > 0) {
                showViewScheduleDialog();
            } else {
                Toast.makeText(context, "Sự kiện không có lịch trình nào", Toast.LENGTH_SHORT).show();
            }
        });

        viewTaskButton.setOnClickListener(v -> {
            if (eventTasks.size() > 0) {
                showViewTaskDialog();
            } else {
                Toast.makeText(context, "Sự kiện không có công việc nào", Toast.LENGTH_SHORT).show();
            }
        });

        selectReminderButton.setOnClickListener(v -> showSelectReminderDialog());
    }

    private void showViewScheduleDialog() {
        if (!isFinishing()) {
            viewScheduleDialog.show();
            if (viewScheduleDialog.getWindow() != null) {
                viewScheduleDialog.getWindow().setAttributes(lWindowParams);
            }
        }
    }

    private void showViewTaskDialog() {
        if (!isFinishing()) {
            viewTaskDialog.show();
            if (viewTaskDialog.getWindow() != null) {
                viewTaskDialog.getWindow().setAttributes(lWindowParams);
            }
            int count = 0;
            for (EventTask t : eventTasks) {
                if (t.isDone()) {
                    count++;
                }
            }
            String progressString = String.format(getResources().getString(R.string.task_progress), count, eventTasks.size());
            taskCompletedTextView.setText(progressString);
            taskProgressBar.setProgress(100 * count / eventTasks.size());
        }
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

    private void showSelectReminderDialog() {
        if (!isFinishing()) {
            selectReminderAdapter.notifyDataSetChanged();
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
        DatabaseAccess.setDatabaseListener(this, context);

        selectedEvent = EventRepository.getInstance().getAllEvents().get(eventId);
        fillInformation();

        salaries.clear();
        salaries.addAll(SalaryRepository.getInstance().getSalariesByEventId(eventId));
        viewSalaryAdapter.customNotifyDataSetChanged();

        eventTasks.clear();
        eventTasks.addAll(TaskRepository.getInstance().getTasksInArrayListByEventId(eventId));
        TaskRepository.sortTasksByOrder(eventTasks);
        viewTaskAdapter.notifyDataSetChanged();
        viewTaskButton.setEnabled(!(eventTasks.size() == 0));

        schedules.clear();
        schedules.addAll(ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId));
        ScheduleRepository.sortSchedulesByOrder(schedules);
        viewScheduleAdapter.notifyDataSetChanged();
        viewScheduleButton.setEnabled(!(schedules.size() == 0));

        selectedReminders.clear();
        selectedReminders.addAll(ReminderRepository.getInstance(context).getRemindersInArrayListByEventId(eventId));
        editReminderAdapter.customNotifyDataSetChanged();

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isRemindersChanged) {
            saveRemindersToDatabase();
        }
        super.onPause();
    }


    @Override
    public void notifyOnLoadComplete() {
        selectedEvent = EventRepository.getInstance().getAllEvents().get(eventId);
        fillInformation();

        salaries.clear();
        salaries.addAll(SalaryRepository.getInstance().getSalariesByEventId(eventId));
        viewSalaryAdapter.customNotifyDataSetChanged();

        eventTasks.clear();
        eventTasks.addAll(TaskRepository.getInstance().getTasksInArrayListByEventId(eventId));
        TaskRepository.sortTasksByOrder(eventTasks);
        viewTaskAdapter.notifyDataSetChanged();
        viewTaskButton.setEnabled(!(eventTasks.size() == 0));

        schedules.clear();
        schedules.addAll(ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId));
        ScheduleRepository.sortSchedulesByOrder(schedules);
        viewScheduleAdapter.notifyDataSetChanged();
        viewScheduleButton.setEnabled(!(schedules.size() == 0));

        selectedReminders.clear();
        selectedReminders.addAll(ReminderRepository.getInstance(context).getRemindersInArrayListByEventId(eventId));
        editReminderAdapter.customNotifyDataSetChanged();
    }

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
        isRemindersChanged = true;
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
                System.out.println( Log.getStackTraceString(e));
            }
            calendar.add(Calendar.MINUTE, r.getMinute() * (-1));
            r.setTime(CalendarUtil.sdfDayMonthYearTime.format(calendar.getTime()));
        }
        ReminderRepository.getInstance(context).updateRemindersByEventId(selectedReminders, eventId);
    }
}