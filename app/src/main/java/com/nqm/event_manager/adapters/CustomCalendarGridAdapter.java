package com.nqm.event_manager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnCustomCalendarGridItemClicked;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CustomCalendarGridAdapter extends BaseAdapter {
    private final Context context;
    private LayoutInflater layoutInflater;

    private TextView numberOfEventsTextView, dayTextView;
    LinearLayout cellLayout;

    private Date currentDate;
    private Date selectedDate;
    private Date viewDate;
    int offSet;
    private ArrayList<CellData> cellDataArrayList;
    private Calendar calendar = Calendar.getInstance();

    IOnCustomCalendarGridItemClicked listener;

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
            view = LayoutInflater.from(context).inflate(R.layout.layout_custom_calendar_grid_item, parent, false);
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
        String currentDateText = CalendarUtil.sdfDayMonthYear.format(currentDate);
        String selectedDateText = CalendarUtil.sdfDayMonthYear.format(selectedDate);
        if (cellData.getDay() > 0) {
            //set text
            dayTextView.setText("" + cellData.getDay());
            if (cellData.getNumberOfEvents() > 0) {
                numberOfEventsTextView.setText("" + cellData.getNumberOfEvents());
            } else {
                numberOfEventsTextView.setText("");
            }
            dayTextView.setTextColor(Color.BLACK);
            numberOfEventsTextView.setTextColor(context.getColor(R.color.colorPrimaryLight));
            cellLayout.setBackground(null);

            calendar.setTime(viewDate);
            calendar.set(Calendar.DAY_OF_MONTH, cellData.getDay());
            String thisDateText = CalendarUtil.sdfDayMonthYear.format(calendar.getTime());

            //current date
            if (currentDateText.equals(thisDateText)) {
                dayTextView.setTextColor(context.getColor(R.color.colorAccent));
//                dayTextView.setTextColor(Color.BLUE);
                numberOfEventsTextView.setTextColor(context.getColor(R.color.colorPrimaryLight));
            }
            //selected Date
            if (selectedDateText.equals(thisDateText)) {
                dayTextView.setTextColor(Color.WHITE);
                numberOfEventsTextView.setTextColor(Color.WHITE);
                cellLayout.setBackground(context.getDrawable(R.drawable.custom_calendar_grid_item_background));
            }
        } else {
            dayTextView.setText("");
            numberOfEventsTextView.setText("");
            dayTextView.setTextColor(Color.BLACK);
            numberOfEventsTextView.setTextColor(Color.BLACK);
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
                    listener.onGridItemClickedFromCalendarAdapter(selectedDate);
                }
            }
        });

        return view;
    }

    public void updateData() {
        cellDataArrayList.clear();

        calendar.setTime(viewDate);
        calendar.set(Calendar.DAY_OF_MONTH, 1); //dayOfMonth starts from 1

        //dayOfWeek starts from 1 (sunday) -> dayOfWeek = 2 (monday) - offSet = 0
        offSet = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        for (int i = 0; i < 37; i++) {
            int day = i - offSet + 1;
            int numberOfEvents = 0;
            if (day > 0 && day <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                calendar.set(Calendar.DAY_OF_MONTH, day);
                numberOfEvents = EventRepository.getInstance(null)
                        .getNumberOfEventsThroughDate(CalendarUtil.sdfDayMonthYear.format(calendar.getTime()));
            } else {
                day = 0;
            }
            cellDataArrayList.add(new CellData(day, numberOfEvents));
        }
    }

    public void setListener(IOnCustomCalendarGridItemClicked listener) {
        this.listener = listener;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
        updateData();
    }

    public void setViewDate(Date viewDate) {
        this.viewDate = viewDate;
        updateData();
    }

    public Date getViewDate() {
        return viewDate;
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
