package com.nqm.event_manager.custom_views;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.CustomCalendarGridAdapter;
import com.nqm.event_manager.interfaces.IOnCustomCalendarGridItemClicked;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.Calendar;
import java.util.Date;

public class CustomCalendarView extends LinearLayout implements IOnCustomCalendarGridItemClicked {

    static int DAYS_COUNT = 42;

    ImageView prevButton;
    ImageView nextButton;

    TextView monthYearTextView;

    GridView gridView;
    CustomCalendarGridAdapter gridAdapter;

    IOnCustomCalendarGridItemClicked listener;

    Context context;

    Calendar calendar = Calendar.getInstance();

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        connectViews();
        addEvents();
        init();
    }

    private void connectViews() {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);

        View rootView = inflate(getContext(), R.layout.layout_custom_calendar, this);

        prevButton = rootView.findViewById(R.id.calendar_view_prev_button);
        nextButton = rootView.findViewById(R.id.calendar_view_next_button);

        monthYearTextView = rootView.findViewById(R.id.calendar_view_month_year_text_view);

        gridView = rootView.findViewById(R.id.calendar_view_grid);

    }

    private void addEvents() {
        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.setTime(gridAdapter.getViewDate());
                calendar.add(Calendar.MONTH, -1);
                gridAdapter.setViewDate(calendar.getTime());
                gridAdapter.notifyDataSetChanged();
                monthYearTextView.setText(CalendarUtil.sdfMonthYear2.format(gridAdapter.getViewDate()));
            }
        });
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.setTime(gridAdapter.getViewDate());
                calendar.add(Calendar.MONTH, 1);
                gridAdapter.setViewDate(calendar.getTime());
                gridAdapter.notifyDataSetChanged();
                monthYearTextView.setText(CalendarUtil.sdfMonthYear2.format(gridAdapter.getViewDate()));
            }
        });
        monthYearTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.setTime(gridAdapter.getViewDate());
                int d = calendar.get(Calendar.DAY_OF_MONTH);
                int m = calendar.get(Calendar.MONTH);
                int y = calendar.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        gridAdapter.setViewDate(calendar.getTime());
                        gridAdapter.setSelectedDate(calendar.getTime());
                        gridAdapter.notifyDataSetChanged();
                        listener.onGridItemClickedFromCalendarView(gridAdapter.getSelectedDate());
                        monthYearTextView.setText(CalendarUtil.sdfMonthYear2.format(calendar.getTime()));
                    }
                }, y, m, d);
                datePickerDialog.show();
            }
        });
    }

    private void init() {
        gridAdapter = new CustomCalendarGridAdapter(context, calendar.getTime(), calendar.getTime());
        gridAdapter.setListener(this);
        gridView.setAdapter(gridAdapter);

        monthYearTextView.setText(CalendarUtil.sdfMonthYear2.format(gridAdapter.getViewDate()));
    }

    public void setListener(IOnCustomCalendarGridItemClicked listener) {
        this.listener = listener;
    }

    public Date getSelectedDate() {
        return gridAdapter.getSelectedDate();
    }

    public void setSelectedDate(Date selectedDate) {
        gridAdapter.setSelectedDate(selectedDate);
    }

    public void setViewDate(Date viewDate) {
        gridAdapter.setViewDate(viewDate);
    }

    @Override
    public void onGridItemClickedFromCalendarView(Date selectedDate) {

    }

    @Override
    public void onGridItemClickedFromCalendarAdapter(Date selectedDate) {
        listener.onGridItemClickedFromCalendarView(this.gridAdapter.getSelectedDate());
    }

    public void updateView() {
        gridAdapter.updateData();
        gridAdapter.notifyDataSetChanged();
        monthYearTextView.setText(CalendarUtil.sdfMonthYear2.format(gridAdapter.getViewDate()));
    }
}
