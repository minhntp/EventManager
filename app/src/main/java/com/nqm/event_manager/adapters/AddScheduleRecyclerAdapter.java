package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.nqm.event_manager.R;
import com.nqm.event_manager.custom_views.AddScheduleItemViewHolder;
import com.nqm.event_manager.custom_views.AddScheduleSwipeAndDragCallback;
import com.nqm.event_manager.interfaces.IOnAddScheduleViewClicked;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.ScheduleUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class AddScheduleRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements AddScheduleSwipeAndDragCallback.ActionCompletionContract {

    IOnAddScheduleViewClicked listener;
    ArrayList<Schedule> schedules;
    ItemTouchHelper itemTouchHelper;
    Calendar calendar = Calendar.getInstance();
    Activity context;

    public void setContext(Activity context) {
        this.context = context;
    }

    public void setListener(IOnAddScheduleViewClicked listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_add_schedule_list_item, parent, false);
        return new AddScheduleItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        ((AddScheduleItemViewHolder) viewHolder).getTimeEditText().setText(schedules.get(position).getTime());
        ((AddScheduleItemViewHolder) viewHolder).getContentEditText().setText(schedules.get(position).getContent());

        ((AddScheduleItemViewHolder) viewHolder).getTimeEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hourOfDay = 18;
                int minute = 0;
                calendar = Calendar.getInstance();
                if (!(schedules.get(position).getTime().isEmpty())) {
                    try {
                        calendar.setTime(CalendarUtil.sdfTime.parse(schedules.get(position).getTime()));
                        hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                        minute = calendar.get(Calendar.MINUTE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hour);
                        calendar.set(Calendar.MINUTE, minute);
                        listener.onTimeEditTextSet(position, CalendarUtil.sdfTime.format(calendar.getTime()));
                    }
                }, hourOfDay, minute, false).show();
            }
        });

        ((AddScheduleItemViewHolder) viewHolder).getReorderImageView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(viewHolder);
                }
                return false;
            }
        });
    }

    public void setSchedules(ArrayList<Schedule> schedules) {
//        ScheduleUtil.sortSchedulesByOrder(schedules);
        this.schedules = schedules;
        notifyDataSetChanged();
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        Schedule targetSchedule = schedules.get(oldPosition);
        Schedule schedule = new Schedule(targetSchedule);
        schedules.remove(oldPosition);
        schedules.add(newPosition, schedule);
        notifyItemMoved(oldPosition, newPosition);
        listener.onAddScheduleItemMoved();
    }

    @Override
    public void onViewSwiped(int position) {
        schedules.remove(position);
        notifyItemRemoved(position);
        listener.onAddScheduleItemRemoved();
    }
}