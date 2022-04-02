package com.nqm.event_manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Schedule;

import java.util.ArrayList;

public class ViewScheduleAdapter extends RecyclerView.Adapter<ViewScheduleAdapter.ViewHolder> {

    public class ViewHolder extends  RecyclerView.ViewHolder{
        TextView timeTextView;
        TextView contentTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            timeTextView = itemView.findViewById(R.id.view_schedule_time_text_view);
            contentTextView = itemView.findViewById(R.id.view_schedule_content_text_view);
        }
    }

    private ArrayList<Schedule> schedules;

    public ViewScheduleAdapter(ArrayList<Schedule> schedules) {
        this.schedules = schedules;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View scheduleView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_view_schedule, viewGroup, false);
        return new ViewHolder(scheduleView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Schedule s = schedules.get(i);

        viewHolder.timeTextView.setText(s.getTime());
        viewHolder.contentTextView.setText(s.getContent());

    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

}
