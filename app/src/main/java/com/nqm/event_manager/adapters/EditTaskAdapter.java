package com.nqm.event_manager.adapters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnEditTaskItemClicked;
import com.nqm.event_manager.interfaces.IOnItemDraggedOrSwiped;
import com.nqm.event_manager.models.Task;
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
                    tasks.get(getAdapterPosition()).setContent(s.toString());
                }
            });

            dateEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final int position = getAdapterPosition();
                        calendarOfTask.setTime(CalendarUtil.sdfDayMonthYear.parse(tasks.get(position).getDate()));
                        int d = calendarOfTask.get(Calendar.DAY_OF_MONTH);
                        int m = calendarOfTask.get(Calendar.MONTH);
                        int y = calendarOfTask.get(Calendar.YEAR);
                        DatePickerDialog dpd = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                calendarOfTask.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                calendarOfTask.set(Calendar.MONTH, month);
                                calendarOfTask.set(Calendar.YEAR, year);
                                tasks.get(position).setDate(CalendarUtil.sdfDayMonthYear.format(calendarOfTask.getTime()));
                                notifyItemChanged(position);
                            }
                        }, y, m, d);
                        dpd.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
                        dpd.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            timeEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getAdapterPosition();
                    int hourOfDay = 12;
                    int minute = 0;
                    calendarOfTask = Calendar.getInstance();
                    if (!(tasks.get(position).getTime().isEmpty())) {
                        try {
                            calendarOfTask.setTime(CalendarUtil.sdfTime.parse(tasks.get(position).getTime()));
                            hourOfDay = calendarOfTask.get(Calendar.HOUR_OF_DAY);
                            minute = calendarOfTask.get(Calendar.MINUTE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                            calendarOfTask.set(Calendar.HOUR_OF_DAY, hour);
                            calendarOfTask.set(Calendar.MINUTE, minute);
                            tasks.get(position).setTime(CalendarUtil.sdfTime.format(calendarOfTask.getTime()));
                            notifyItemChanged(position);
                        }
                    }, hourOfDay, minute, false).show();
                }
            });

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        tasks.get(position).setDone(checkBox.isChecked());
                        listener.onEditTaskItemCheckBoxClicked(checkBox.isChecked());
                        notifyItemChanged(position);
                    }
                }
            });
        }
    }

    private ArrayList<Task> tasks;
    private ItemTouchHelper itemTouchHelper;
    private IOnEditTaskItemClicked listener;
    private Calendar calendarOfTask = Calendar.getInstance();
    private Calendar calendarOfCurrentTime = Calendar.getInstance();
    private Context context;

    public EditTaskAdapter(ArrayList<Task> tasks) {
        this.tasks = tasks;
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
        View taskView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_edit_task, viewGroup, false);
        return new ViewHolder(taskView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        Task t = tasks.get(i);

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
                if (days < 0) {
                    timeLeftTextView.setText("Quá hạn " + (-days) + " ngày");
                    timeLeftTextView.setTextColor(Color.RED);
                } else if (days == 0) {
                    timeLeftTextView.setText("Hôm nay");
                    timeLeftTextView.setTextColor(Color.rgb(255, 102, 0));
                } else {
                    timeLeftTextView.setText("Còn " + (days) + " ngày");
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

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    //ITEM DRAGGED OT SWIPED CALLBACK
    @Override
    public void onViewDragged(int oldPosition, int newPosition) {
        Task tempTask = new Task(tasks.get(oldPosition));
        tasks.remove(oldPosition);
        tasks.add(newPosition, tempTask);
        notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void onViewSwiped(int position) {
        tasks.remove(position);
        listener.onEditTaskItemRemoved();
        notifyItemRemoved(position);
    }

}