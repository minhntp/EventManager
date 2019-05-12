package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Schedule;

import java.util.ArrayList;

public class ViewScheduleAdapter extends BaseAdapter {

    private final Activity context;
    ArrayList<Schedule> schedules;

    public ViewScheduleAdapter(Activity context, ArrayList<Schedule> schedules) {
        this.context = context;
        this.schedules = schedules;
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

        final TextView scheduleTimeEditText = view.findViewById(R.id.view_schedule_time_text_view);
        TextView scheduleContentEditText = view.findViewById(R.id.view_schedule_content_text_view);

        //Fill information
        scheduleTimeEditText.setText(schedules.get(i).getTime());
        scheduleContentEditText.setText(schedules.get(i).getContent());

        return view;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }
}
