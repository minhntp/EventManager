package com.nqm.event_manager.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnItemDraggedOrSwiped;
import com.nqm.event_manager.models.Schedule;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class EditScheduleAdapter extends RecyclerView.Adapter<EditScheduleAdapter.ViewHolder>
        implements IOnItemDraggedOrSwiped {

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText timeEditText;
        EditText contentEditText;
        ImageView reorderImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            timeEditText = itemView.findViewById(R.id.edit_schedule_time_edit_text);
            contentEditText = itemView.findViewById(R.id.edit_schedule_content_edit_text);
            reorderImageView = itemView.findViewById(R.id.edit_schedule_item_reorder);

            contentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    schedules.get(getAdapterPosition()).setContent(s.toString());
                }
            });

            timeEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int hourOfDay = 18;
                    int minute = 0;
                    calendar = Calendar.getInstance();
                    if (!(schedules.get(getAdapterPosition()).getTime().isEmpty())) {
                        try {
                            calendar.setTime(CalendarUtil.sdfTime.parse(schedules.get(getAdapterPosition()).getTime()));
                            hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                            minute = calendar.get(Calendar.MINUTE);
                        } catch (Exception e) {
                            System.out.println( Log.getStackTraceString(e));
                        }
                    }
                    new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                            schedules.get(getAdapterPosition()).setTime(CalendarUtil.sdfTime.format(calendar.getTime()));
                            notifyItemChanged(getAdapterPosition());
                        }
                    }, hourOfDay, minute, false).show();
                }
            });
        }
    }

    private ArrayList<Schedule> schedules;
    private ItemTouchHelper itemTouchHelper;
    private Calendar calendar = Calendar.getInstance();
    private Context context;

    public EditScheduleAdapter(ArrayList<Schedule> schedules) {
        this.schedules = schedules;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }

        View scheduleView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_edit_schedule, parent, false);
        return new ViewHolder(scheduleView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        Schedule s = schedules.get(i);

        EditText timeEditText = viewHolder.timeEditText;
        EditText contentEditText = viewHolder.contentEditText;
        ImageView reorderImageView = viewHolder.reorderImageView;

        timeEditText.setText(s.getTime());
        contentEditText.setText(s.getContent());

        reorderImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(viewHolder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    @Override
    public void onViewDragged(int oldPosition, int newPosition) {
        Schedule tempSchedule = schedules.get(oldPosition);
        schedules.remove(oldPosition);
        schedules.add(newPosition, tempSchedule);
        notifyItemMoved(oldPosition, newPosition);

    }

    @Override
    public void onViewSwiped(int position) {
        schedules.remove(position);
        notifyItemRemoved(position);
    }
}