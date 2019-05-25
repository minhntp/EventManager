package com.nqm.event_manager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnCustomCalendarViewClicked;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CustomCalendarGridAdapter extends BaseAdapter {
    private final Context context;
    LinearLayout cellLayout;
    int offSet;
    IOnCustomCalendarViewClicked listener;
    private LayoutInflater layoutInflater;
    private TextView numberOfEventsTextView, dayTextView;
    private Date currentDate;
    private Date selectedDate;
    private Date viewDate;
    private ArrayList<CellData> cellDataArrayList;
    private Calendar calendar = Calendar.getInstance();

    // Days in Current Month
    public CustomCalendarGridAdapter(Context context, Date selectedDate, Date viewDate) {
        super();
        this.context = context;
        layoutInflater = LayoutInflater.from(context);

        currentDate = calendar.getTime();
        this.selectedDate = selectedDate;
        this.viewDate = viewDate;

        cellDataArrayList = new ArrayList<>();
        updateData();
    }

    @Override
    public int getCount() {
        return cellDataArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public CellData getItem(int position) {
        return cellDataArrayList.get(position);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_item_custom_calendar, parent, false);
        }

        //Connect views
        dayTextView = view.findViewById(R.id.custom_calendar_grid_cell_day_text_view);
        numberOfEventsTextView = view.findViewById(R.id.custom_calendar_grid_cell_number_of_events_text_view);
        cellLayout = view.findViewById(R.id.custom_calendar_cell_layout);

        //Fill information
        //Set colors and background
        //if item.day > 0 -> set 2 textviews = item.day & item.numOfEvents, 2 textviews' color = black, background = null
        //--if item.day.toDayMonthYear == current date -> set 2 textviews' color = accent, background = null
        //--else if item.day.toDayMonthYeat == selected date -> set 2 textviews' color = white, background = background
        //else set 2 textviews = "", 2 textviews' color = black, background = null
        CellData cellData = getItem(position);
        if (cellData.getDay() > 0) {
            //set text
            dayTextView.setText("" + cellData.getDay());
            if (cellData.getNumberOfEvents() > 0) {
                numberOfEventsTextView.setText("" + cellData.getNumberOfEvents());
            } else {
                numberOfEventsTextView.setText("");
            }

            //set style
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
            dayTextView.setText("");
            numberOfEventsTextView.setText("");
            cellLayout.setBackground(null);
        }

        //Add events
        cellLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItem(position).getDay() > 0) {
                    calendar.setTime(viewDate);
                    calendar.set(Calendar.DAY_OF_MONTH, getItem(position).getDay());
                    selectedDate = calendar.getTime();
                    notifyDataSetChanged();
                    listener.onCustomCalendarCellClicked(selectedDate);
                }
            }
        });

        return view;
    }

    public void updateData() {
        cellDataArrayList.clear();

        calendar.setTime(viewDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1); //dayOfMonth starts from 1

        // dayOfWeek = 1 (sunday) -> offSet = 6 -> offSet = dayOfWeek + 5 (only Sunday)
        // dayOfWeek = 2 (monday) -> offSet = 0 -> offSet = dayOfWeek - 2 (same for other days)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY) {
            offSet = dayOfWeek + 5;
        } else {
            offSet = dayOfWeek - 2;
        }
        for (int i = 0; i < 37; i++) {
            int day = i - offSet + 1;
            int numberOfEvents = 0;
            if (day > 0 && day <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                numberOfEvents = EventRepository.getInstance()
                        .getNumberOfEventsThroughDate(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
            } else {
                day = 0;
            }
            cellDataArrayList.add(new CellData(day, numberOfEvents));
        }
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


    public void setListener(IOnCustomCalendarViewClicked listener) {
        this.listener = listener;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
        updateData();
    }

    public Date getViewDate() {
        return viewDate;
    }

    public void setViewDate(Date viewDate) {
        this.viewDate = viewDate;
        updateData();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    //------------------------------------------------------------------

    class CellData {
        private int day;
        private int numberOfEvents;

        public CellData(int day, int numberOfEvents) {
            this.day = day;
            this.numberOfEvents = numberOfEvents;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public int getNumberOfEvents() {
            return numberOfEvents;
        }

        public void setNumberOfEvents(int numberOfEvents) {
            this.numberOfEvents = numberOfEvents;
        }
    }
}
