package com.nqm.event_manager.adapters;


import android.app.Activity;
import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnCustomViewClicked;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class AddScheduleAdapter extends BaseAdapter {

    private final Activity context;
    ArrayList<Schedule> schedules;
    Calendar calendar = Calendar.getInstance();

    EditText scheduleTimeEditText;
    EditText scheduleContentEditText;
    ImageButton scheduleDeleteButton;

    IOnCustomViewClicked listener;

    public AddScheduleAdapter(Activity context, ArrayList<Schedule> schedules, IOnCustomViewClicked listener) {
        this.context = context;
        this.schedules = schedules;
        this.listener = listener;
    }

    public void sort() {
        Collections.sort(schedules, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule schedule1, Schedule schedule2) {
                if(schedule1.getTime().isEmpty() && schedule2.getTime().isEmpty()) {
                    return 0;
                }
                if(schedule1.getTime().isEmpty() && !schedule2.getTime().isEmpty()) {
                    return 1;
                }
                if(!schedule1.getTime().isEmpty() && schedule2.getTime().isEmpty()) {
                    return -1;
                }
                int compareResult = 0;
                try {
                    compareResult = CalendarUtil.sdfTime.parse(schedule1.getTime()).compareTo(
                            CalendarUtil.sdfTime.parse(schedule2.getTime()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return compareResult;
            }
        });
    }

    @Override
    public int getCount() {
        return schedules.size();
    }

    @Override
    public Schedule getItem(int i) {
        return schedules.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_add_schedule_list_item, viewGroup, false);
        }

        scheduleTimeEditText = view.findViewById(R.id.add_schedule_time_edit_text);
        scheduleContentEditText = view.findViewById(R.id.add_schedule_content_edit_text);

        //Fill information
        scheduleTimeEditText.setText(schedules.get(position).getTime());
        scheduleContentEditText.setText(schedules.get(position).getContent());

        //Add events
        scheduleDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onDeleteButtonClicked(position);
            }
        });

        scheduleTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar = Calendar.getInstance();
                if (!getItem(position).getTime().isEmpty()) {
                    try {
                        calendar.setTime(CalendarUtil.sdfTime.parse(getItem(position).getTime()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                int HH = calendar.get(Calendar.HOUR_OF_DAY);
//                int mm = calendar.get(Calendar.MINUTE);
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        listener.onTimeEditTextSet(position, CalendarUtil.sdfTime.format(calendar.getTime()));
                    }
                }, 18, 0, false).show();
            }
        });

        return view;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
