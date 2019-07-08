package com.nqm.event_manager.custom_views;

import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.CustomCalendarGridAdapter;
import com.nqm.event_manager.interfaces.IOnCustomCalendarItemClicked;

import java.util.Calendar;
import java.util.Date;

public class CustomCalendar extends LinearLayout implements IOnCustomCalendarItemClicked {

    ImageView prevButton;
    ImageView nextButton;
    TextView monthYearTextView;
        GridView gridView;
    CustomCalendarGridAdapter gridAdapter;
//    RecyclerView recyclerView;
//    CalendarGridRecyclerAdapter gridRecyclerAdapter;
    Dialog scrollDatePickerDialog;
    CustomScrollDatePicker scrollDatePicker;
    Button scrollDatePickerDialogOkButton, scrollDatePickerDialogCancelButton;

    IOnCustomCalendarItemClicked listener;

    Context context;

    Calendar calendar = Calendar.getInstance();

//    boolean isInit = false;

    public CustomCalendar(Context context, AttributeSet attrs) {
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
//        recyclerView = rootView.findViewById(R.id.calendar_recycler_view);
    }

    private void init() {
        gridAdapter = new CustomCalendarGridAdapter(context, calendar.getTime(), calendar.getTime());
        gridAdapter.setListener(this);
        gridView.setAdapter(gridAdapter);
//        gridRecyclerAdapter = new CalendarGridRecyclerAdapter(calendar.getTime(), calendar.getTime());
//        gridRecyclerAdapter.setListener(this);
//        recyclerView.setLayoutManager(new GridLayoutManager(context, 7));
//        recyclerView.setAdapter(gridRecyclerAdapter);

//        calendar.setTime(gridRecyclerAdapter.getViewDate());
        calendar.setTime(gridAdapter.getViewDate());
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        String monthYear = String.format(context.getResources().getString(R.string.calendar_month_year),
                month, year);
        monthYearTextView.setText(monthYear);

        initScrollDatePickerDialog();
    }

    private void initScrollDatePickerDialog() {
        scrollDatePickerDialog = new Dialog(context);
        scrollDatePickerDialog.setContentView(R.layout.dialog_custom_scroll_date_picker);

        scrollDatePicker = scrollDatePickerDialog.findViewById(R.id.dialog_custom_scroll_date_picker_main);
        scrollDatePickerDialogCancelButton = scrollDatePickerDialog.findViewById(R.id.dialog_custom_scroll_date_picker_cancel_button);
        scrollDatePickerDialogOkButton = scrollDatePickerDialog.findViewById(R.id.dialog_custom_scroll_date_picker_ok_button);

        scrollDatePickerDialogCancelButton.setOnClickListener(v -> scrollDatePickerDialog.dismiss());

        scrollDatePickerDialogOkButton.setOnClickListener(v -> {
            Date selectedViewDate = scrollDatePicker.getViewDate();
//            gridAdapter.setViewDate(selectedViewDate);
            gridAdapter.setViewDate(selectedViewDate);
            calendar.setTime(selectedViewDate);
            String monthYear = String.format(context.getResources().getString(R.string.calendar_month_year),
                    calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
            monthYearTextView.setText(monthYear);
            scrollDatePickerDialog.dismiss();
        });
    }


    private void addEvents() {
        prevButton.setOnClickListener(v -> {
//            calendar.setTime(gridAdapter.getViewDate());
            calendar.setTime(gridAdapter.getViewDate());
            calendar.add(Calendar.MONTH, -1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
//            gridAdapter.setViewDate(calendar.getTime());
            gridAdapter.setViewDate(calendar.getTime());
//            calendar.setTime(gridAdapter.getViewDate());
            calendar.setTime(gridAdapter.getViewDate());
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            String monthYear = String.format(context.getResources().getString(R.string.calendar_month_year),
                    month, year);
            monthYearTextView.setText(monthYear);
        });

        nextButton.setOnClickListener(v -> {
            calendar.setTime(gridAdapter.getViewDate());
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            gridAdapter.setViewDate(calendar.getTime());

            calendar.setTime(gridAdapter.getViewDate());
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            String monthYear = String.format(context.getResources().getString(R.string.calendar_month_year),
                    month, year);
            monthYearTextView.setText(monthYear);
        });

        monthYearTextView.setOnClickListener(v -> {
            scrollDatePicker.setViewDate(gridAdapter.getViewDate());
            scrollDatePickerDialog.show();
            if (scrollDatePickerDialog.getWindow() != null) {
                scrollDatePickerDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });

    }


    public void setListener(IOnCustomCalendarItemClicked listener) {
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
//        Log.d("debug", "custom calendar update data and notify of gridAdapter");
        gridAdapter.updateData();
        gridAdapter.notifyDataSetChanged();

        calendar.setTime(gridAdapter.getViewDate());
        String monthYear = String.format(context.getResources().getString(R.string.calendar_month_year),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
        monthYearTextView.setText(monthYear);
    }
}
