package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ViewScheduleAdapter extends BaseAdapter {

    private final Activity context;
    ArrayList<Schedule> schedules;

    public ViewScheduleAdapter(Activity context, ArrayList<Schedule> schedules) {
        this.context = context;
        this.schedules = schedules;
        sortSchedule();
    }

    private void sortSchedule() {
        Collections.sort(schedules, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule schedule1, Schedule schedule2) {
                if(schedule1.getTime().isEmpty() && schedule2.getTime().isEmpty()) {
                    return 0;
                }
                if(schedule1.getTime().isEmpty() && !schedule2.getTime().isEmpty()) {
                    return -1;
                }
                if(!schedule1.getTime().isEmpty() && schedule2.getTime().isEmpty()) {
                    return 1;
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
    public Object getItem(int i) {
        return schedules.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_view_schedule_list_item, viewGroup, false);
        }

        final TextView scheduleTimeTextView = view.findViewById(R.id.view_schedule_time_text_view);
        TextView scheduleContentTextView = view.findViewById(R.id.view_schedule_content_text_view);

        //Fill information
        scheduleTimeTextView.setText(schedules.get(i).getTime());
        scheduleContentTextView.setText(schedules.get(i).getContent());

        return view;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }
}
