package com.nqm.event_manager.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.utils.ScheduleUtil;

import java.util.ArrayList;

public class SendEventInfo extends AppCompatActivity {

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

        eventId = getIntent().getStringExtra("eventId");
        event = EventRepository.getInstance(null).getAllEvents().get(eventId);
        employeesIds = EmployeeRepository.getInstance(null).getEmployeesIdsByEventId(eventId);
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
                String emailAddressesString = "";
                for (int i = 0; i < employeeListView.getChildCount(); i++) {
                    CheckBox cb = employeeListView.getChildAt(i).findViewById(R.id.send_event_select_item_checkbox);
                    if (cb.isChecked()) {
                        emailAddressesString += EmployeeRepository.getInstance(null).getAllEmployees()
                                .get(employeesIds.get(i)).getEmail() + ",";
                    }
                }
                if (emailAddressesString.length() > 0) {
                    emailAddressesString = emailAddressesString.substring(0, emailAddressesString.length() - 1);
                }

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String mailto = "mailto:";
                if (ccBccRadioGroup.getCheckedRadioButtonId() == R.id.send_event_cc_radio_button) {
                    mailto += "?cc=" + emailAddressesString;
                } else {
                    mailto += "?bcc=" + emailAddressesString;
                }
                mailto += "&subject=" + Uri.encode(event.getTen() + " - " + event.getNgayBatDau());
                mailto += "&body=" + Uri.encode(prepairContent());
                emailIntent.setData(Uri.parse(mailto));
                startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng gửi Email:"));
            }
        });
    }

    private String prepairContent() {
        String content = "";
        CheckBox cb;

        cb = sectionListView.getChildAt(0).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            content += "Sự kiện: " + "\n";
            content += "\t" + event.getTen() + "\n";
            content += "\n";
        }

        cb = sectionListView.getChildAt(1).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            content += "Thời gian:" + "\n";
            content += "\t" + event.getNgayBatDau() + " - " + event.getGioBatDau() + "\n";
            content += "\t" + event.getNgayKetThuc() + " - " + event.getGioKetThuc() + "\n";
            content += "\n";
        }

        cb = sectionListView.getChildAt(2).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            content += "Địa điểm:" + "\n";
            content += "\t" + event.getDiaDiem() + "\n";
            content += "\n";
        }

        cb = sectionListView.getChildAt(3).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            content += "Nhân sự: " + "\n";
            for (int i = 0; i < employeesIds.size(); i++) {
                Employee e = EmployeeRepository.getInstance(null).getAllEmployees().get(employeesIds.get(i));
                content += "\t" + e.getHoTen() + " - " + e.getChuyenMon() + "\n";
            }
            content += "\n";
        }

        cb = sectionListView.getChildAt(4).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            content += "Ghi chú: " + "\n";
            content += "\t" + event.getGhiChu() + "\n";
            content += "\n";
        }

        cb = sectionListView.getChildAt(5).findViewById(R.id.send_event_select_item_checkbox);
        if (cb.isChecked()) {
            ArrayList<Schedule> schedules = ScheduleRepository.getInstance(null).getSchedulesInArrayListByEventId(eventId);
            ScheduleUtil.sortSchedulesByOrder(schedules);
            content += "Lịch trình: " + "\n";
            for (Schedule s : schedules) {
                content += "\t" + s.getTime() + ": " + s.getContent() + "\n";
            }
            content += "\n";
        }
//        content.replaceAll("\t", getString(R.string.tab));
        return content;
    }

}
