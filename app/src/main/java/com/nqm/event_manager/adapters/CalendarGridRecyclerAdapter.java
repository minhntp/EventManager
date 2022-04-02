package com.nqm.event_manager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnCustomCalendarItemClicked;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class CalendarGridRecyclerAdapter extends RecyclerView.Adapter<CalendarGridRecyclerAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;
        TextView numberOfEventsTextView;
        LinearLayout cellLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.custom_calendar_grid_cell_day_text_view);
            numberOfEventsTextView = itemView.findViewById(R.id.custom_calendar_grid_cell_number_of_events_text_view);
            cellLayout = itemView.findViewById(R.id.custom_calendar_cell_layout);
        }
    }

    private Calendar calendar = Calendar.getInstance();
    private IOnCustomCalendarItemClicked listener;
    private Date currentDate;
    private Date selectedDate;
    private Date viewDate;
    private ArrayList<CellData> cellDataArrayList;
    private Context context;

    public CalendarGridRecyclerAdapter(Date selectedDate, Date viewDate) {
        currentDate = calendar.getTime();
        this.selectedDate = selectedDate;
        this.viewDate = viewDate;

        cellDataArrayList = new ArrayList<>();
        updateData();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null) {
            context = viewGroup.getContext();
        }
        View cellView = LayoutInflater.from(context).inflate(R.layout.grid_item_custom_calendar, viewGroup, false);
        return new ViewHolder(cellView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        TextView dayTextView = viewHolder.dayTextView;
        TextView numberOfEventsTextView = viewHolder.numberOfEventsTextView;
        LinearLayout cellLayout = viewHolder.cellLayout;

        CellData cellData = cellDataArrayList.get(i);
        if (cellData.getDay() > 0) {
            cellLayout.setOnClickListener(v -> {
                calendar.setTime(viewDate);
                calendar.set(Calendar.DAY_OF_MONTH, cellData.getDay());
                selectedDate = calendar.getTime();
                notifyDataSetChanged();
                listener.onCustomCalendarCellClicked(selectedDate);
            });

            dayTextView.setText(String.valueOf(cellData.getDay()));
            if (cellData.getNumberOfEvents() > 0) {
                numberOfEventsTextView.setText(String.valueOf(cellData.getNumberOfEvents()));
            } else {
                numberOfEventsTextView.setText("");
            }

            if (isCurrentDay(cellData.getDay())) {
                if (isSelectedDay(cellData.getDay())) {
                    dayTextView.setTextColor(context.getColor(R.color.colorPrimaryLight));
                    numberOfEventsTextView.setTextColor(context.getColor(R.color.colorPrimaryLight));
                    cellLayout.setBackground(context.getDrawable(R.drawable.custom_calendar_grid_item_background));
                } else {
                    dayTextView.setTextColor(context.getColor(R.color.colorAccent));
                    numberOfEventsTextView.setTextColor(context.getColor(R.color.colorPrimaryLight));
                    cellLayout.setBackground(null);
                }
            } else {
                if (isSelectedDay(cellData.getDay())) {
                    dayTextView.setTextColor(context.getColor(R.color.textPrimaryColor));
                    numberOfEventsTextView.setTextColor(context.getColor(R.color.textPrimaryColor));
                    cellLayout.setBackground(context.getDrawable(R.drawable.custom_calendar_grid_item_background));
                } else {
                    dayTextView.setTextColor(context.getColor(R.color.textBlack));
                    numberOfEventsTextView.setTextColor(context.getColor(R.color.colorPrimaryLight));
                    cellLayout.setBackground(null);
                }
            }
        } else {
            cellLayout.setOnClickListener(null);
            dayTextView.setText("");
            numberOfEventsTextView.setText("");
            cellLayout.setBackground(null);
        }

    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public int getItemCount() {
        return cellDataArrayList.size();
    }

    public void updateData() {
        cellDataArrayList.clear();

        calendar.setTime(viewDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1); //dayOfMonth starts from 1

        // dayOfWeek = 1 (sunday) -> offSet = 6 -> offSet = dayOfWeek + 5 (only Sunday)
        // dayOfWeek = 2 (monday) -> offSet = 0 -> offSet = dayOfWeek - 2 (same for other days)
        int offSet;
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY) {
            offSet = dayOfWeek + 5;
        } else {
            offSet = dayOfWeek - 2;
        }
        HashMap<String, ArrayList<String>> numberOfEventsMap = EventRepository.getInstance().getNumberOfEventsMap();
        for (int i = 0; i < 37; i++) {
            int day = i - offSet + 1;
            int numberOfEvents = 0;
            if (day > 0 && day <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                String dateString = CalendarUtil.sdfDayMonthYear.format(calendar.getTime());
                ArrayList<String> arr = numberOfEventsMap.get(dateString);
                if (arr != null) {
                    numberOfEvents = arr.size();
                }
            } else {
                day = 0;
            }
            cellDataArrayList.add(new CellData(day, numberOfEvents));
        }
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    public Date getViewDate() {
        return viewDate;
    }

    public void setViewDate(Date viewDate) {
        this.viewDate = viewDate;
        updateData();
        notifyDataSetChanged();
    }

    private boolean isSelectedDay(int day) {
        calendar.setTime(viewDate);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return CalendarUtil.sdfDayMonthYear.format(calendar.getTime()).equals(
                CalendarUtil.sdfDayMonthYear.format(selectedDate));
    }

    private boolean isCurrentDay(int day) {
        calendar.setTime(viewDate);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return CalendarUtil.sdfDayMonthYear.format(calendar.getTime()).equals(
                CalendarUtil.sdfDayMonthYear.format(currentDate));
    }

    public void setListener(IOnCustomCalendarItemClicked listener) {
        this.listener = listener;
    }

    private class CellData {
        private int day;
        private int numberOfEvents;

        private CellData(int day, int numberOfEvents) {
            this.day = day;
            this.numberOfEvents = numberOfEvents;
        }

        private int getDay() {
            return day;
        }

        private int getNumberOfEvents() {
            return numberOfEvents;
        }
    }
}
