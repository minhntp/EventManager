package com.nqm.event_manager.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.nqm.event_manager.R;

import java.util.Calendar;
import java.util.Date;

public class CustomScrollDatePicker extends LinearLayout {
    //    NumberPicker dayPicker;
    NumberPicker monthPicker, yearPicker;
    Context context;
    Calendar calendar = Calendar.getInstance();

    public CustomScrollDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        connectViews();
        init();
        addEvents();
    }

    private void connectViews() {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);

        View rootView = inflate(getContext(), R.layout.view_custom_scroll_date_picker, this);

//        dayPicker = rootView.findViewById(R.id.scroll_date_picker_day_number_picker);
        monthPicker = rootView.findViewById(R.id.scroll_date_picker_month_number_picker);
        yearPicker = rootView.findViewById(R.id.scroll_date_picker_year_number_picker);
    }

    private void init() {
//        dayPicker.setMinValue(1);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        yearPicker.setMinValue(1970);
        yearPicker.setMaxValue(2100);
    }

    private void addEvents() {
        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                calendar.set(Calendar.YEAR, yearPicker.getValue());
                calendar.set(Calendar.MONTH, newVal - 1);
//                dayPicker.setMaxValue(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
        });

        yearPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                calendar.set(Calendar.YEAR, newVal);
                calendar.set(Calendar.MONTH, monthPicker.getValue() - 1);
//                dayPicker.setMaxValue(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
        });
    }

    public void setViewDate(Date viewDate) {
        try {
            calendar.setTime(viewDate);
            yearPicker.setValue(calendar.get(Calendar.YEAR));
            monthPicker.setValue(calendar.get(Calendar.MONTH) + 1);
//            dayPicker.setMaxValue(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
//            dayPicker.setValue(calendar.get(Calendar.DAY_OF_MONTH));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Date getViewDate() {
        int y = yearPicker.getValue();
        int m = monthPicker.getValue();
//        int d = dayPicker.getValue();
        calendar.set(Calendar.YEAR, y);
        calendar.set(Calendar.MONTH, m - 1);
//        calendar.set(Calendar.DAY_OF_MONTH, d);
        return calendar.getTime();
    }
}
