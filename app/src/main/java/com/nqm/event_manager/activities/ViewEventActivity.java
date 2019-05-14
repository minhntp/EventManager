package com.nqm.event_manager.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
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
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewEventActivity extends AppCompatActivity {
    Activity context;

    Button addReminderButton, viewScheduleButton;
    TextView titleEditText, timeEditText, locationEditText, noteEditText;
    CustomListView employeeListView, reminderListView;
    android.support.v7.widget.Toolbar toolbar;

    HashMap<String, Salary> salaries;
    ViewSalaryAdapter viewSalaryAdapter;
    Event selectedEvent;
    String eventId;

    ArrayList<Schedule> schedules;
    ViewScheduleAdapter viewScheduleAdapter;

    int RESULT_FROM_EDIT_EVENT_INTENT = 3;
    int RESULT_FROM_EDIT_SALARY_INTENT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        connectViews();

        context = this;

        eventId = getIntent().getStringExtra("eventId");
        selectedEvent = EventRepository.getInstance(null).getAllEvents().get(eventId);
        salaries = SalaryRepository.getInstance(null).getSalariesByEventId(eventId);
        viewSalaryAdapter = new ViewSalaryAdapter(this, salaries);
        employeeListView.setAdapter(viewSalaryAdapter);

        addEvents();
        fillInformation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_event_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    //Add events for menu items
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
                            Intent intent = new Intent();
                            intent.putExtra("delete?", true);
                            intent.putExtra("eventId", eventId);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                    })
                    .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Intent intent = new Intent();
//                            intent.putExtra("deleted?",false);
//                            setResult(RESULT_OK);
//                            finish();
                        }
                    })
                    .show();
            return true;
        }

        //Chỉnh sửa lương
        if (id == R.id.view_event_action_edit_salaries) {
            Intent intent = new Intent(this, EditSalaryFromViewEventActivity.class);
            intent.putExtra("eventId", eventId);
            startActivityForResult(intent, RESULT_FROM_EDIT_SALARY_INTENT);
            return true;
        }

        //Chỉnh sửa sự kiện
        if (id == R.id.view_event_action_edit_event) {
            Intent intent = new Intent(this, EditEventActivity.class);
            intent.putExtra("eventId", eventId);
            startActivityForResult(intent, RESULT_FROM_EDIT_EVENT_INTENT);
            return true;
        }
        //Gửi thông báo
        if (id == R.id.view_event_action_send_notification) {
            Toast.makeText(this, "Gửi thông báo cho nhân viên", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        employeeListView = findViewById(R.id.view_event_employees_listview);
        reminderListView = findViewById(R.id.view_event_reminder_listview);
    }

    private void addEvents() {
        viewScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openViewScheduleDialog();
            }
        });
    }

    private void openViewScheduleDialog() {
        final Dialog viewScheduleDialog = new Dialog(this);
        viewScheduleDialog.setContentView(R.layout.dialog_view_schedule);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(viewScheduleDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //Connect views
        final ListView viewScheduleListView = viewScheduleDialog.findViewById(R.id.view_schedule_dialog_schedule_list_view);
        Button backButton = viewScheduleDialog.findViewById(R.id.back_button);

        schedules = ScheduleRepository.getInstance(null).getSchedulesInArrayListByEventId(eventId);
        viewScheduleAdapter = new ViewScheduleAdapter(this, schedules);
        viewScheduleListView.setAdapter(viewScheduleAdapter);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewScheduleDialog.dismiss();
            }
        });

        if (!isFinishing()) {
            viewScheduleDialog.show();
            viewScheduleDialog.getWindow().setAttributes(lWindowParams);
            if (schedules.size() == 0) {
                Toast toast = Toast.makeText(this, "Không có lịch trình nào", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 1000);
                toast.show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_FROM_EDIT_EVENT_INTENT && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("edit event", false)) {
                if (data.getBooleanExtra("edit event succeed", false)) {
                    selectedEvent = EventRepository.getInstance(null).getAllEvents().get(eventId);
                    fillInformation();
                    salaries = SalaryRepository.getInstance(null).getSalariesByEventId(eventId);
                    viewSalaryAdapter.notifyDataSetChanged(salaries);
                    Toast.makeText(this, "Cập nhật sự kiện thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cập nhật sự kiện thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == RESULT_FROM_EDIT_SALARY_INTENT && resultCode == RESULT_OK) {
            if (data.getBooleanExtra("edit salaries", false)) {
                if (data.getBooleanExtra("edit salaries succeed", false)) {
                    fillInformation();
                    salaries = SalaryRepository.getInstance(null).getSalariesByEventId(eventId);
                    viewSalaryAdapter.notifyDataSetChanged(salaries);
                    Toast.makeText(this, "Cập nhật lương thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cập nhật lương thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        context.finish();
        return super.onSupportNavigateUp();
    }
}
