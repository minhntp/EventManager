package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnViewSalaryItemClicked;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;

public class ViewSalaryAdapter extends BaseAdapter {

    private final Activity context;
    IOnViewSalaryItemClicked listener;
    private ArrayList<Salary> salaries;

    public ViewSalaryAdapter(Activity context, ArrayList<Salary> salaries) {
        this.context = context;
        this.salaries = salaries;
    }

    @Override
    public int getCount() {
        return salaries.size();
    }

    @Override
    public Salary getItem(int i) {
        return salaries.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_view_salary, parent, false);
        }

        TextView hoTenTextView = view.findViewById(R.id.view_salary_employee_name_text_view);
        TextView chuyenMonTextView = view.findViewById(R.id.view_salary_employee_speciality_text_view);
        TextView luongTextView = view.findViewById(R.id.view_salary_salary_text_view);
        CheckBox daThanhToanCheckBox = view.findViewById(R.id.view_salary_paid_checkbox);

        //SHOW DATA
        final String employeeId = getItem(position).getEmployeeId();
        hoTenTextView.setText(EmployeeRepository.getInstance().getAllEmployees().get(employeeId).getHoTen());
        chuyenMonTextView.setText(EmployeeRepository.getInstance().getAllEmployees().get(employeeId).getChuyenMon());
        luongTextView.setText("" + getItem(position).getSalary());
        daThanhToanCheckBox.setChecked(getItem(position).isPaid());

        //ADD EVENTS
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onViewSalaryItemClicked(employeeId);
            }
        });

        return view;
    }

    public void setListener(IOnViewSalaryItemClicked listener) {
        this.listener = listener;
    }

    public void notifyDataSetChanged(ArrayList<Salary> salaries) {
        this.salaries = salaries;
        super.notifyDataSetChanged();
    }
}
