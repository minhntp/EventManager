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
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class EditSalaryAdapter extends BaseAdapter {
    private final Activity context;
    private HashMap<String, Salary> allSalaries;
    private ArrayList<Salary> salaries;
    private HashMap<String, Employee> allEmployees;

    public EditSalaryAdapter(Activity context, ArrayList<Salary> salaries) {
        this.context = context;
        this.salaries = salaries;
        allSalaries = SalaryRepository.getInstance(null).getAllSalaries();
        allEmployees = EmployeeRepository.getInstance(null).getAllEmployees();
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
        final EditText salaryEditText = view.findViewById(R.id.edit_salary_salary_edit_text);
        final CheckBox paidCheckBox = view.findViewById(R.id.edit_salary_paid_checkbox);

        //Fill information
        hoTenTextView.setText(allEmployees.get(getItem(position).getEmployeeId()).getHoTen());
        chuyenMonTextView.setText(allEmployees.get(getItem(position).getEmployeeId()).getChuyenMon());
        salaryEditText.setText("" + getItem(position).getSalary());
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
