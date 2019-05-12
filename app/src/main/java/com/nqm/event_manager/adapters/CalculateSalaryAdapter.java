package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;

public class CalculateSalaryAdapter extends BaseAdapter {
    private final Activity context;
    int sum = 0, paid = 0, unpaid = 0;
    private ArrayList<String> resultSalariesIds;

    public CalculateSalaryAdapter(Activity context, ArrayList<String> resultSalariesIds) {
        this.context = context;
        this.resultSalariesIds = resultSalariesIds;
    }

    public int getSum() {
        return sum;
    }

    public int getPaid() {
        return paid;
    }

    public int getUnpaid() {
        return unpaid;
    }

    @Override
    public int getCount() {
        return resultSalariesIds.size();
    }

    @Override
    public Object getItem(int i) {
        return SalaryRepository.getInstance(null).getAllSalaries().get(resultSalariesIds.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_calculate_salary_list_item, parent, false);
        }
        TextView startDateTextView = view.findViewById(R.id.calculate_salaries_list_item_date_text_view);
        TextView titleTextView = view.findViewById(R.id.calculate_salaries_list_item_event_title_text_view);
        TextView locationTextView = view.findViewById(R.id.calculate_salaries_list_item_event_location_text_view);
        EditText salaryEditText = view.findViewById(R.id.calculate_salaries_list_item_salary_edit_text);
        CheckBox paidCheckBox = view.findViewById(R.id.calculate_salaries_list_item_paid_checkbox);

        //Fill information
        Salary salary = SalaryRepository.getInstance(null).getAllSalaries()
                .get(resultSalariesIds.get(position));
//        Employee employee = EmployeeRepository.getInstance(null).getAllEmployees()
//                .get(salary.getEmployeeId());
        Event event = EventRepository.getInstance(null).getAllEvents().get(salary.getEventId());

        try {
            startDateTextView.setText(CalendarUtil.sdfDayMonth.format(CalendarUtil.sdfDayMonthYear
                    .parse(event.getNgayBatDau())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        titleTextView.setText(event.getTen());
        locationTextView.setText(event.getDiaDiem());
        salaryEditText.setText("" + salary.getSalary());
        paidCheckBox.setChecked(salary.isPaid());

        if (paidCheckBox.isChecked()) {
            paidCheckBox.setEnabled(false);
            salaryEditText.setEnabled(false);
        } else {
            paidCheckBox.setEnabled(true);
            salaryEditText.setEnabled(true);
        }

        return view;
    }

    public ArrayList<String> getSalariesIds() {
        return resultSalariesIds;
    }

    public void setSalariesIds(ArrayList<String> salariesIds) {
        this.resultSalariesIds = salariesIds;
    }

    public void notifyDataSetChanged(ArrayList<String> resultSalariesIds) {
        this.resultSalariesIds = resultSalariesIds;
        super.notifyDataSetChanged();
    }
}
