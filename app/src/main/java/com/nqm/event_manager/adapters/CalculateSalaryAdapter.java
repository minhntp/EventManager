package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnCalculateSalaryItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Date;

public class CalculateSalaryAdapter extends BaseAdapter {
    private final Activity context;
    IOnCalculateSalaryItemClicked listener;
    private ArrayList<Salary> resultSalaries;
    private Resources res;

    public CalculateSalaryAdapter(Activity context, ArrayList<Salary> resultSalaries) {
        this.context = context;
        this.resultSalaries = resultSalaries;
        res = context.getResources();
    }


    public void setListener(IOnCalculateSalaryItemClicked listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return resultSalaries.size();
    }

    @Override
    public Salary getItem(int i) {
        return resultSalaries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_calculate_salary, parent, false);
        }

        //Connect views
        TextView startDateTextView = view.findViewById(R.id.calculate_salaries_list_item_date_text_view);
        TextView titleTextView = view.findViewById(R.id.calculate_salaries_list_item_event_title_text_view);
        TextView locationTextView = view.findViewById(R.id.calculate_salaries_list_item_event_location_text_view);
        EditText salaryEditText = view.findViewById(R.id.calculate_salaries_list_item_salary_edit_text);
        CheckBox paidCheckBox = view.findViewById(R.id.calculate_salaries_list_item_paid_checkbox);

        //Fill information
        final Salary salary = getItem(position);
        Event event = EventRepository.getInstance().getAllEvents().get(salary.getEventId());
        if (event != null) {
            try {
                Date startDate = CalendarUtil.sdfDayMonthYear.parse(event.getNgayBatDau());
                String toText = CalendarUtil.sdfDayMonth.format(startDate) + "\n" +
                        CalendarUtil.dayOfWeekInVietnamese(event.getNgayBatDau());
                startDateTextView.setText(toText);
//                startDateTextView.setText(CalendarUtil.sdfDayMonth.format(CalendarUtil.sdfDayMonthYear
//                        .parse(event.getNgayBatDau())));
            } catch (Exception e) {
                e.printStackTrace();
            }
            titleTextView.setText(event.getTen());
            locationTextView.setText(event.getDiaDiem());
            salaryEditText.setText(String.valueOf(salary.getSalary()));
            paidCheckBox.setChecked(salary.isPaid());

            if (salary.isPaid()) {
                paidCheckBox.setEnabled(false);
                salaryEditText.setEnabled(false);
            } else {
                paidCheckBox.setEnabled(true);
                salaryEditText.setEnabled(true);
            }

            //Add events
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCalculateSalaryItemClicked(salary.getEventId());
                }
            });
        }

        return view;
    }

    public void notifyDataSetChanged(ArrayList<Salary> resultSalaries) {
        this.resultSalaries = resultSalaries;
        super.notifyDataSetChanged();
    }
}
