package com.nqm.event_manager.adapters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnEditTaskItemClicked;
import com.nqm.event_manager.interfaces.IOnItemDraggedOrSwiped;
import com.nqm.event_manager.models.EventTask;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class EditTaskAdapter extends RecyclerView.Adapter<EditTaskAdapter.ViewHolder>
        implements IOnItemDraggedOrSwiped {

    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText dateEditText;
        TextView dowTextView;
        EditText timeEditText;
        EditText contentEditText;
        TextView timeLeftTextView;
        CheckBox checkBox;
        ImageView reorderImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            dateEditText = itemView.findViewById(R.id.edit_task_date_edit_text);
            dowTextView = itemView.findViewById(R.id.edit_task_dow_text_view);
            timeEditText = itemView.findViewById(R.id.edit_task_time_edit_text);
            contentEditText = itemView.findViewById(R.id.edit_task_content_edit_text);
            timeLeftTextView = itemView.findViewById(R.id.edit_task_time_left_text_view);
            checkBox = itemView.findViewById(R.id.edit_task_check_box);
            reorderImageView = itemView.findViewById(R.id.edit_task_reorder_image_view);

            contentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    eventTasks.get(getAdapterPosition()).setContent(s.toString());
                }
            });

            dateEditText.setOnClickListener(v -> {
                try {
                    final int position = getAdapterPosition();
                    calendarOfTask.setTime(CalendarUtil.sdfDayMonthYear.parse(eventTasks.get(position).getDate()));
                    int d = calendarOfTask.get(Calendar.DAY_OF_MONTH);
                    int m = calendarOfTask.get(Calendar.MONTH);
                    int y = calendarOfTask.get(Calendar.YEAR);
                    DatePickerDialog dpd = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                        calendarOfTask.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        calendarOfTask.set(Calendar.MONTH, month);
                        calendarOfTask.set(Calendar.YEAR, year);
                        eventTasks.get(position).setDate(CalendarUtil.sdfDayMonthYear.format(calendarOfTask.getTime()));
                        notifyItemChanged(position);
                    }, y, m, d);
                    dpd.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
                    dpd.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            timeEditText.setOnClickListener(v -> {
                final int position = getAdapterPosition();
                int hourOfDay = 12;
                int minute = 0;
                calendarOfTask = Calendar.getInstance();
                if (!(eventTasks.get(position).getTime().isEmpty())) {
                    try {
                        calendarOfTask.setTime(CalendarUtil.sdfTime.parse(eventTasks.get(position).getTime()));
                        hourOfDay = calendarOfTask.get(Calendar.HOUR_OF_DAY);
                        minute = calendarOfTask.get(Calendar.MINUTE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                new TimePickerDialog(context, (timePicker, hour, minute1) -> {
                    calendarOfTask.set(Calendar.HOUR_OF_DAY, hour);
                    calendarOfTask.set(Calendar.MINUTE, minute1);
                    eventTasks.get(position).setTime(CalendarUtil.sdfTime.format(calendarOfTask.getTime()));
                    notifyItemChanged(position);
                }, hourOfDay, minute, false).show();
            });

            checkBox.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    eventTasks.get(position).setDone(checkBox.isChecked());
                    listener.onEditTaskItemCheckBoxClicked(checkBox.isChecked());
                    notifyItemChanged(position);
                }
            });
        }
    }

    private ArrayList<EventTask> eventTasks;
    private ItemTouchHelper itemTouchHelper;
    private IOnEditTaskItemClicked listener;
    private Calendar calendarOfTask = Calendar.getInstance();
    private Calendar calendarOfCurrentTime = Calendar.getInstance();
    private Context context;

    public EditTaskAdapter(ArrayList<EventTask> eventTasks) {
        this.eventTasks = eventTasks;
    }

    public void setListener(IOnEditTaskItemClicked listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null) {
            context = viewGroup.getContext();
        }
        View taskView = LayoutInflater.from(context).inflate(R.layout.list_item_edit_task, viewGroup, false);
        return new ViewHolder(taskView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        EventTask t = eventTasks.get(i);

        EditText dateEditText = viewHolder.dateEditText;
        TextView dowTextView = viewHolder.dowTextView;
        EditText timeEditText = viewHolder.timeEditText;
        EditText contentEditText = viewHolder.contentEditText;
        TextView timeLeftTextView = viewHolder.timeLeftTextView;
        CheckBox checkBox = viewHolder.checkBox;
        ImageView reorderImageView = viewHolder.reorderImageView;

        dateEditText.setText(t.getDate());
        dowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(t.getDate()));
        timeEditText.setText(t.getTime());
        contentEditText.setText(t.getContent());

        if (!t.isDone()) {
            try {
                calendarOfTask.setTime(CalendarUtil.sdfDayMonthYear.parse(t.getDate()));
                calendarOfCurrentTime.setTime(CalendarUtil.sdfDayMonthYear.parse(
                        CalendarUtil.sdfDayMonthYear.format(Calendar.getInstance().getTime())));
                long days = (calendarOfTask.getTime().getTime() - calendarOfCurrentTime.getTime().getTime()) / (1000 * 60 * 60 * 24);
//            int compareResult = calendar1.getTime().compareTo(calendar2.getTime());
                Resources res = context.getResources();
                if (days < 0) {
                    timeLeftTextView.setText(String.format(res.getString(R.string.task_overdue), -days));
                    timeLeftTextView.setTextColor(Color.RED);
                } else if (days == 0) {
                    timeLeftTextView.setText(res.getString(R.string.task_today));
                    timeLeftTextView.setTextColor(Color.rgb(255, 102, 0));
                } else {
                    timeLeftTextView.setText(String.format(res.getString(R.string.task_still_valid), days));
                    timeLeftTextView.setTextColor(Color.GREEN);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            timeLeftTextView.setText("");
        }

        dateEditText.setEnabled(!t.isDone());
        timeEditText.setEnabled(!t.isDone());
        contentEditText.setEnabled(!t.isDone());
        checkBox.setChecked(t.isDone());

        reorderImageView.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(viewHolder);
            }
            return false;
        });
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    @Override
    public int getItemCount() {
        return eventTasks.size();
    }

    //ITEM DRAGGED OT SWIPED CALLBACK
    @Override
    public void onViewDragged(int oldPosition, int newPosition) {
        EventTask tempEventTask = new EventTask(eventTasks.get(oldPosition));
        eventTasks.remove(oldPosition);
        eventTasks.add(newPosition, tempEventTask);
        notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void onViewSwiped(int position) {
        eventTasks.remove(position);
        listener.onEditTaskItemRemoved();
        notifyItemRemoved(position);
    }

}