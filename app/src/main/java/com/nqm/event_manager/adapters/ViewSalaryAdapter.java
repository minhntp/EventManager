package com.nqm.event_manager.adapters;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnViewSalaryItemClicked;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class ViewSalaryAdapter extends BaseAdapter {

    private final Activity context;
    private HashMap<String, Salary> salaries;
    private String[] salariesIds;

    IOnViewSalaryItemClicked listener;

    public ViewSalaryAdapter(Activity context, HashMap<String, Salary> salaries) {
        this.context = context;
        this.salaries = salaries;
        salariesIds = salaries.keySet().toArray(new String[salaries.size()]);
    }

    @Override
    public int getCount() {
        return salaries.size();
    }

    @Override
    public Salary getItem(int i) {
        return salaries.get(salariesIds[i]);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_view_salary_list_item, parent, false);
        }

        TextView hoTenTextView = view.findViewById(R.id.view_salary_employee_name_text_view);
        TextView chuyenMonTextView = view.findViewById(R.id.view_salary_employee_speciality_text_view);
        TextView luongTextView = view.findViewById(R.id.view_salary_salary_text_view);
        CheckBox daThanhToanCheckBox = view.findViewById(R.id.view_salary_paid_checkbox);

        //SHOW DATA
        final String employeeId = getItem(position).getEmployeeId();
        hoTenTextView.setText(EmployeeRepository.getInstance(null).getAllEmployees().get(employeeId).getHoTen());
        chuyenMonTextView.setText(EmployeeRepository.getInstance(null).getAllEmployees().get(employeeId).getChuyenMon());
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

    public void notifyDataSetChanged(HashMap<String, Salary> salaries) {
        this.salaries = salaries;
        salariesIds = salaries.keySet().toArray(new String[salaries.size()]);
        super.notifyDataSetChanged();
    }
}
