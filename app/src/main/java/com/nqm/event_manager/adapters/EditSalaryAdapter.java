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
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;

public class EditSalaryAdapter extends BaseAdapter {
    private final Activity context;
    private ArrayList<Salary> salaries;
    private Resources res;

    public EditSalaryAdapter(Activity context, ArrayList<Salary> salaries) {
        this.context = context;
        res = context.getResources();
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
            view = LayoutInflater.from(context).inflate(R.layout.list_item_edit_salary, parent, false);
        }

        TextView hoTenTextView = view.findViewById(R.id.edit_salary_employee_name_text_view);
        TextView chuyenMonTextView = view.findViewById(R.id.edit_salary_employee_speciality_text_view);
        EditText salaryEditText = view.findViewById(R.id.edit_salary_salary_edit_text);
        CheckBox paidCheckBox = view.findViewById(R.id.edit_salary_paid_checkbox);

        Employee employee = EmployeeRepository.getInstance().getAllEmployees().get(getItem(position).getEmployeeId());
        if (employee != null) {
            //Fill information
            hoTenTextView.setText(employee.getHoTen());
            chuyenMonTextView.setText(employee.getChuyenMon());
        }
        salaryEditText.setText(String.valueOf(getItem(position).getSalary()));
        paidCheckBox.setChecked(getItem(position).isPaid());

        if (paidCheckBox.isChecked()) {
            paidCheckBox.setEnabled(false);
            salaryEditText.setEnabled(false);
        } else {
            paidCheckBox.setEnabled(true);
            salaryEditText.setEnabled(true);
        }

        return view;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
