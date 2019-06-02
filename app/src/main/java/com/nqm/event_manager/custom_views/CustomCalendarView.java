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
import com.nqm.event_manager.interfaces.IOnCustomCalendarViewClicked;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.Calendar;
import java.util.Date;

public class CustomCalendarView extends LinearLayout implements IOnCustomCalendarViewClicked {

    ImageView prevButton;
    ImageView nextButton;

    TextView monthYearTextView;

    GridView gridView;
    CustomCalendarGridAdapter gridAdapter;

    IOnCustomCalendarViewClicked listener;

    Context context;

    Calendar calendar = Calendar.getInstance();

    boolean isInit = false;

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        connectViews();
        init();
        addEvents();
    }

    private void connectViews() {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);

        View rootView = inflate(getContext(), R.layout.view_custom_calendar, this);

        prevButton = rootView.findViewById(R.id.calendar_view_prev_button);
        nextButton = rootView.findViewById(R.id.calendar_view_next_button);
        monthYearTextView = rootView.findViewById(R.id.calendar_view_month_year_text_view);
        gridView = rootView.findViewById(R.id.calendar_view_grid);

    }

    private void init() {
        gridAdapter = new CustomCalendarGridAdapter(context, calendar.getTime(), calendar.getTime());
        gridAdapter.setListener(this);
        gridView.setAdapter(gridAdapter);

        monthYearTextView.setText(CalendarUtil.sdfMonthYear2.format(gridAdapter.getViewDate()));
    }

    private void addEvents() {
        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.setTime(gridAdapter.getViewDate());
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
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
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                gridAdapter.setViewDate(calendar.getTime());
                gridAdapter.notifyDataSetChanged();
                monthYearTextView.setText(CalendarUtil.sdfMonthYear2.format(gridAdapter.getViewDate()));
            }
        });
        monthYearTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.setTime(gridAdapter.getViewDate());
                int d = 1;
                int m = calendar.get(Calendar.MONTH);
                int y = calendar.get(Calendar.YEAR);
                try {
                    if (CalendarUtil.sdfMonthYear2.format(calendar.getTime())
                            .equals(CalendarUtil.sdfMonthYear2.format(gridAdapter.getSelectedDate()))) {
                        calendar.setTime(gridAdapter.getSelectedDate());
                        d = calendar.get(Calendar.DAY_OF_MONTH);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
//                int d = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(context,     new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        gridAdapter.setViewDate(calendar.getTime());
                        gridAdapter.setSelectedDate(calendar.getTime());
                        gridAdapter.notifyDataSetChanged();
                        listener.onCustomCalendarCellClicked(gridAdapter.getSelectedDate());
                        monthYearTextView.setText(CalendarUtil.sdfMonthYear2.format(calendar.getTime()));
                    }
                }, y, m, d);
                datePickerDialog.getDatePicker().setFirstDayOfWeek(Calendar.MONDAY);
                datePickerDialog.show();
            }
        });
    }



    public void setListener(IOnCustomCalendarViewClicked listener) {
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
    public void onCustomCalendarCellClicked(Date selectedDate) {
        listener.onCustomCalendarCellClicked(this.gridAdapter.getSelectedDate());
    }

    public void updateView() {
        gridAdapter.updateData();
        gridAdapter.notifyDataSetChanged();
        monthYearTextView.setText(CalendarUtil.sdfMonthYear2.format(gridAdapter.getViewDate()));
    }
}
