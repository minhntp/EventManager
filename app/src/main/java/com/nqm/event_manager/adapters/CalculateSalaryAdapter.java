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
import com.nqm.event_manager.interfaces.IOnCalculateSalaryItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CalculateSalaryAdapter extends BaseAdapter {
    private final Activity context;
    int sum = 0, paid = 0, unpaid = 0;
    private ArrayList<String> resultSalariesIds;
    IOnCalculateSalaryItemClicked listener;

    public CalculateSalaryAdapter(Activity context, ArrayList<String> resultSalariesIds) {
        this.context = context;
        this.resultSalariesIds = resultSalariesIds;
        sortResultSalariesIds();
    }

    private void sortResultSalariesIds() {
        Collections.sort(resultSalariesIds, new Comparator<String>() {
            @Override
            public int compare(String salaryId1, String salaryId2) {
                int compareResult = 0;

                Salary salary1 = SalaryRepository.getInstance(null).getAllSalaries().get(salaryId1);
                Salary salary2 = SalaryRepository.getInstance(null).getAllSalaries().get(salaryId2);

                Event e1 = EventRepository.getInstance(null).getAllEvents().get(salary1.getEventId());
                Event e2 = EventRepository.getInstance(null).getAllEvents().get(salary2.getEventId());

                try {
                    if (!e1.getNgayBatDau().equals(e2.getNgayBatDau())) {
                        compareResult = CalendarUtil.sdfDayMonthYear.parse(e1.getNgayBatDau()).compareTo(
                                CalendarUtil.sdfDayMonthYear.parse(e2.getNgayBatDau()));
                    } else {
                        compareResult = CalendarUtil.sdfTime.parse(e1.getGioBatDau()).compareTo(
                                CalendarUtil.sdfTime.parse(e2.getGioKetThuc()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return compareResult;
            }
        });
    }

    public void setListener(IOnCalculateSalaryItemClicked listener) {
        this.listener = listener;
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
    public Salary getItem(int i) {
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
        final Salary salary = getItem(position);
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

        //Add events
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCalculateSalaryItemClicked(salary.getEventId());
            }
        });

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
        sortResultSalariesIds();
        super.notifyDataSetChanged();
    }
}
