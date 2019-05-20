package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.ViewSalaryAdapter;
import com.nqm.event_manager.adapters.ViewScheduleAdapter;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnViewSalaryItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewEventActivity extends AppCompatActivity implements IOnViewSalaryItemClicked {
    Activity context;

    Button addReminderButton, viewScheduleButton;
    TextView titleEditText, timeEditText, locationEditText, noteEditText;
    CustomListView reminderListView;
    android.support.v7.widget.Toolbar toolbar;

    String eventId;
    Event selectedEvent;

    HashMap<String, Salary> salaries;
    ViewSalaryAdapter viewSalaryAdapter;
    CustomListView salaryListView;

    Dialog viewScheduleDialog;
    WindowManager.LayoutParams lWindowParams;
    ArrayList<Schedule> schedules;
    ViewScheduleAdapter viewScheduleAdapter;
    CustomListView scheduleListView;
    Button scheduleBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        connectViews();
        init();
        addEvents();
    }

    private void init() {
        context = this;

        eventId = getIntent().getStringExtra("eventId");
        selectedEvent = EventRepository.getInstance(null).getAllEvents().get(eventId);

        fillInformation();

        // SALARY LIST VIEW
        salaries = SalaryRepository.getInstance(null).getSalariesByEventId(eventId);
        viewSalaryAdapter = new ViewSalaryAdapter(this, salaries);
        viewSalaryAdapter.setListener(this);
        salaryListView.setAdapter(viewSalaryAdapter);

        // SCHEDULE DIALOG
        schedules = ScheduleRepository.getInstance(null).getSchedulesInArrayListByEventId(eventId);
        viewScheduleDialog = new Dialog(this);
        viewScheduleDialog.setContentView(R.layout.dialog_view_schedule);

        lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(viewScheduleDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        scheduleListView = viewScheduleDialog.findViewById(R.id.view_schedule_dialog_schedule_list_view);
        scheduleBackButton = viewScheduleDialog.findViewById(R.id.back_button);

        viewScheduleAdapter = new ViewScheduleAdapter(this, schedules);
        scheduleListView.setAdapter(viewScheduleAdapter);

        scheduleBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewScheduleDialog.dismiss();
            }
        });

        //DISABLE BUTTONS
        viewScheduleButton.setEnabled(!(schedules.size() == 0));
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
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Xóa sự kiện")
                    .setMessage("Bạn có chắc chắn không?")
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EventRepository.getInstance(null).deleteEventFromDatabase(eventId, new EventRepository.MyDeleteEventCallback() {
                                @Override
                                public void onCallback(boolean deleteEventSucceed) {
                                    if (deleteEventSucceed) {
                                        Toast.makeText(context, "Xóa sự kiện thành công", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Xóa sự kiện thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                    finish();
                                }
                            });
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
        if (salaries.size() > 0) {
            for (Salary s : salaries.values()) {
                if (!s.isPaid()) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    private void fillInformation() {
        titleEditText.setText(selectedEvent.getTen());
        String time = CalendarUtil.dayOfWeekInVietnamese(selectedEvent.getNgayBatDau()) + " - " +
                selectedEvent.getNgayBatDau() + " - " + selectedEvent.getGioBatDau();
        time += "\n" + CalendarUtil.dayOfWeekInVietnamese(selectedEvent.getNgayKetThuc()) + " - " +
                selectedEvent.getNgayKetThuc() + " - " + selectedEvent.getGioKetThuc();
        timeEditText.setText(time);
        locationEditText.setText(selectedEvent.getDiaDiem());
        noteEditText.setText(selectedEvent.getGhiChu());
    }

    private void connectViews() {
        toolbar = findViewById(R.id.view_event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chi tiết sự kiện");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        addReminderButton = findViewById(R.id.view_event_add_reminder_button);
        viewScheduleButton = findViewById(R.id.view_event_schedule_button);

        titleEditText = findViewById(R.id.view_event_title_text_view);
        timeEditText = findViewById(R.id.view_event_time_text_view);
        locationEditText = findViewById(R.id.view_event_location_text_view);
        noteEditText = findViewById(R.id.view_event_note_text_view);

        salaryListView = findViewById(R.id.view_event_salaries_listview);
        reminderListView = findViewById(R.id.view_event_reminder_listview);
    }

    private void addEvents() {
        viewScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schedules = ScheduleRepository.getInstance(null).getSchedulesInArrayListByEventId(eventId);
                if (schedules.size() > 0) {
                    showViewScheduleDialog();
                } else {
                    Toast.makeText(context, "Sự kiện không có lịch trình nào", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showViewScheduleDialog() {
        if (schedules.size() == 0) {
            Toast toast = Toast.makeText(this, "Không có lịch trình nào", Toast.LENGTH_SHORT);
            toast.show();
        } else if (!isFinishing()) {
            viewScheduleDialog.show();
            viewScheduleDialog.getWindow().setAttributes(lWindowParams);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        context.finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onViewSalaryItemClicked(String employeeId) {
        Intent intent = new Intent(this, ViewEmployeeActivity.class);
        intent.putExtra("employeeId", employeeId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectedEvent = EventRepository.getInstance(null).getAllEvents().get(eventId);
        salaries = SalaryRepository.getInstance(null).getSalariesByEventId(eventId);
        schedules = ScheduleRepository.getInstance(null).getSchedulesInArrayListByEventId(eventId);
        fillInformation();
        viewSalaryAdapter.notifyDataSetChanged(salaries);
        viewScheduleAdapter.notifyDataSetChanged(schedules);

        viewScheduleButton.setEnabled(!(schedules.size() == 0));
    }
}
