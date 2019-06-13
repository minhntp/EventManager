package com.nqm.event_manager.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.SendEventEmployeeAdapter;
import com.nqm.event_manager.adapters.SendEventSectionAdapter;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.models.Task;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.repositories.TaskRepository;
import com.nqm.event_manager.utils.Constants;

import java.util.ArrayList;

public class SendEventInfo extends AppCompatActivity {

    Toolbar toolbar;

    Button selectAllEmployeesButton, deselectAllEmployeesButton, selectAllSectionsButton,
            deselectAllSectionsButton, cancelButton, sendButton;
    ListView employeeListView, sectionListView;
    RadioGroup ccBccRadioGroup;
    RadioButton ccRadioButton, bccRadioButton;

    Activity context;
    String eventId;
    Event event;
    ArrayList<String> employeesIds;
    String[] sectionsTitles;

    SendEventEmployeeAdapter employeeAdapter;
    SendEventSectionAdapter sectionAdapter;

    StringBuilder stringBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_event_info);

        connectViews();
        init();
        addEvents();
    }

    private void connectViews() {
        selectAllEmployeesButton = findViewById(R.id.send_event_select_all_employee_button);
        deselectAllEmployeesButton = findViewById(R.id.send_event_deselect_all_employee_button);
        selectAllSectionsButton = findViewById(R.id.send_event_select_all_section_button);
        deselectAllSectionsButton = findViewById(R.id.send_event_deselect_all_section_button);
        cancelButton = findViewById(R.id.send_event_info_dialog_cancel_button);
        sendButton = findViewById(R.id.send_event_info_dialog_send_button);

        employeeListView = findViewById(R.id.send_event_info_dialog_employee_list_view);
        sectionListView = findViewById(R.id.send_event_info_dialog_section_list_view);

        ccBccRadioGroup = findViewById(R.id.send_event_cc_bcc_radio_group);
        ccRadioButton = findViewById(R.id.send_event_cc_radio_button);
        bccRadioButton = findViewById(R.id.send_event_bcc_radio_button);
    }

    private void init() {
        context = this;

        toolbar = findViewById(R.id.send_event_info_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.send_event_info_activity_label);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        eventId = getIntent().getStringExtra(Constants.INTENT_EVENT_ID);
        event = EventRepository.getInstance().getAllEvents().get(eventId);
        employeesIds = EmployeeRepository.getInstance().getEmployeesIdsByEventId(eventId);
        sectionsTitles = getResources().getStringArray(R.array.sections_titles);

        employeeAdapter = new SendEventEmployeeAdapter(this, employeesIds);
        employeeListView.setAdapter(employeeAdapter);
        sectionAdapter = new SendEventSectionAdapter(this, sectionsTitles);
        sectionListView.setAdapter(sectionAdapter);

        ccRadioButton.setChecked(true);
    }

    private void addEvents() {
        selectAllEmployeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < employeeListView.getChildCount(); i++) {
                    CheckBox cb = employeeListView.getChildAt(i).findViewById(R.id.send_event_select_item_checkbox);
                    cb.setChecked(true);
                }
            }
        });

        deselectAllEmployeesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < employeeListView.getChildCount(); i++) {
                    CheckBox cb = employeeListView.getChildAt(i).findViewById(R.id.send_event_select_item_checkbox);
                    cb.setChecked(false);
                }
            }
        });

        selectAllSectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < sectionListView.getChildCount(); i++) {
                    CheckBox cb = sectionListView.getChildAt(i).findViewById(R.id.send_event_select_item_checkbox);
                    cb.setChecked(true);
                }
            }
        });

        deselectAllSectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < sectionListView.getChildCount(); i++) {
                    CheckBox cb = sectionListView.getChildAt(i).findViewById(R.id.send_event_select_item_checkbox);
                    cb.setChecked(false);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.finish();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stringBuilder = new StringBuilder();

                for (int i = 0; i < employeeListView.getChildCount(); i++) {
                    CheckBox cb = employeeListView.getChildAt(i).findViewById(R.id.send_event_select_item_checkbox);
                    if (cb.isChecked()) {
                        Employee employee = EmployeeRepository.getInstance().getAllEmployees().get(employeesIds.get(i));
                        if (employee != null) {
                            stringBuilder.append(employee.getEmail()).append(",");
                        }
                    }
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String mailto = "mailto:";
                if (ccBccRadioGroup.getCheckedRadioButtonId() == R.id.send_event_cc_radio_button) {
                    mailto += "?cc=" + stringBuilder.toString();
                } else {
                    mailto += "?bcc=" + stringBuilder.toString();
                }
                mailto += "&subject=" + Uri.encode(event.getTen() + " - " + event.getNgayBatDau());
                mailto += "&body=" + Uri.encode(prepairContent());
                emailIntent.setData(Uri.parse(mailto));
                startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng gửi Email:"));
            }
        });
    }

    private String prepairContent() {
        StringBuilder contentStringBuilder = new StringBuilder();

        CheckBox cb;

        cb = sectionListView.getChildAt(0).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            contentStringBuilder.append("Sự kiện:\n")
                    .append("\t").append(event.getTen()).append("\n")
                    .append("\n");
        }

        cb = sectionListView.getChildAt(1).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            contentStringBuilder.append("Thời gian:\n")
                    .append("\t").append(event.getNgayBatDau()).append(" - ").append(event.getGioBatDau()).append("\n")
                    .append("\t").append(event.getNgayKetThuc()).append(" - ").append(event.getGioKetThuc()).append("\n")
                    .append("\n");
        }

        cb = sectionListView.getChildAt(2).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            contentStringBuilder.append("Địa điểm:\n")
                    .append("\t").append(event.getDiaDiem()).append("\n")
                    .append("\n");
        }

        cb = sectionListView.getChildAt(3).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            contentStringBuilder.append("Nhân sự:\n");
            for (int i = 0; i < employeesIds.size(); i++) {
                Employee employee = EmployeeRepository.getInstance().getAllEmployees().get(employeesIds.get(i));
                if (employee != null) {
                    contentStringBuilder.append("\t").append("+ ").append(employee.getHoTen())
                            .append(" - ").append(employee.getChuyenMon()).append("\n");
                }
            }
            contentStringBuilder.append("\n");
        }

        cb = sectionListView.getChildAt(4).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            contentStringBuilder.append("Ghi chú:\n")
                    .append("\t").append(event.getGhiChu()).append("\n")
                    .append("\n");
        }


        cb = sectionListView.getChildAt(5).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            ArrayList<Task> tasks = TaskRepository.getInstance().getTasksInArrayListByEventId(eventId);
            TaskRepository.sortTasksByOrder(tasks);
            contentStringBuilder.append("Công việc:\n");
            for (Task t : tasks) {
                contentStringBuilder.append("\t").append("+ ").append(t.getDate());
                if (!t.getTime().isEmpty()) {
                    contentStringBuilder.append("  ").append(t.getTime());
                } else {
                    contentStringBuilder.append("                   ");
                }
                contentStringBuilder.append(": ").append(t.getContent());
                if (t.isDone()) {
                    contentStringBuilder.append(" - Đã làm")
                            .append("\n");
                } else {
                    contentStringBuilder.append(" - Chưa làm")
                            .append("\n");
                }
            }
            contentStringBuilder.append("\n");
        }

        cb = sectionListView.getChildAt(6).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            ArrayList<Schedule> schedules = ScheduleRepository.getInstance().getSchedulesInArrayListByEventId(eventId);
            ScheduleRepository.sortSchedulesByOrder(schedules);
            contentStringBuilder.append("Lịch trình:\n");
            for (Schedule s : schedules) {
                contentStringBuilder.append("\t").append("+ ").append(s.getTime()).append(": ")
                        .append(s.getContent()).append("\n");
            }
            contentStringBuilder.append("\n");
        }

        return contentStringBuilder.toString().replaceAll("\t", " ");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
