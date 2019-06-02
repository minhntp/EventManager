package com.nqm.event_manager.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Task;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class ViewTaskAdapter extends RecyclerView.Adapter<ViewTaskAdapter.ViewHolder> {

    public class ViewHolder extends  RecyclerView.ViewHolder{
        TextView dateTextView, dowTextView, timeTextView, contentTextView, timeLeftTextView;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.view_task_date_text_view);
            dowTextView = itemView.findViewById(R.id.view_task_dow_text_view);
            timeTextView = itemView.findViewById(R.id.view_task_time_text_view);
            contentTextView = itemView.findViewById(R.id.view_task_content_text_view);
            timeLeftTextView = itemView.findViewById(R.id.view_task_time_left_text_view);
            checkBox = itemView.findViewById(R.id.view_task_check_box);
        }
    }

    private ArrayList<Task> tasks;
    Calendar calendarOfTask = Calendar.getInstance();
    Calendar calendarOfCurrentTime = Calendar.getInstance();

    public ViewTaskAdapter(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View scheduleView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_view_task, viewGroup, false);
        return new ViewHolder(scheduleView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Task t = tasks.get(i);

        viewHolder.dateTextView.setText(t.getDate());
        viewHolder.dowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(t.getDate()));
        viewHolder.timeTextView.setText(t.getTime());
        viewHolder.contentTextView.setText(t.getContent());

        if (!t.isDone()) {
            try {
                calendarOfTask.setTime(CalendarUtil.sdfDayMonthYear.parse(t.getDate()));
                calendarOfCurrentTime.setTime(CalendarUtil.sdfDayMonthYear.parse(
                        CalendarUtil.sdfDayMonthYear.format(Calendar.getInstance().getTime())));
                long days = (calendarOfTask.getTime().getTime() - calendarOfCurrentTime.getTime().getTime()) / (1000 * 60 * 60 * 24);
//            int compareResult = calendarOfTask.getTime().compareTo(calendar2.getTime());
                if (days < 0) {
                    viewHolder.timeLeftTextView.setText("Quá hạn " + (-days) + " ngày");
                    viewHolder.timeLeftTextView.setTextColor(Color.RED);
                } else if (days == 0) {
                    viewHolder.timeLeftTextView.setText("Hôm nay");
                    viewHolder.timeLeftTextView.setTextColor(Color.rgb(255, 102, 0));
                } else {
                    viewHolder.timeLeftTextView.setText("Còn " + days + " ngày");
                    viewHolder.timeLeftTextView.setTextColor(Color.GREEN);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            viewHolder.timeLeftTextView.setText("");
        }

        viewHolder.checkBox.setChecked(t.isDone());

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

}
