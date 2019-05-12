package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectEmployeeInEditEventAdapter extends BaseAdapter {
    private final Activity context;
    private HashMap<String, Employee> allEmployees;
    private String[] allEmployeesIds;
    private ArrayList<String> selectedEmployeesIds;
    private HashMap<String, Salary> allSalaries;
    private String eventId;

    public SelectEmployeeInEditEventAdapter(Activity context, String eventId, ArrayList<String> selectedEmployeesIds) {
        this.context = context;
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.eventId = eventId;
        allEmployees = EmployeeRepository.getInstance(null).getAllEmployees();
        allEmployeesIds = allEmployees.keySet().toArray(new String[allEmployees.size()]);
        allSalaries = SalaryRepository.getInstance(null).getAllSalaries();
    }

    @Override
    public int getCount() {
        return allEmployees.size();
    }

    @Override
    public Object getItem(int i) {
        return allEmployees.get(allEmployeesIds[i]);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_select_employee_list_item, parent, false);
        }

        TextView hoTenTextView = view.findViewById(R.id.select_ho_ten_text_view);
        TextView chuyenMonTextView = view.findViewById(R.id.select_chuyen_mon_text_view);
        CheckBox addEmployeeCheckBox = view.findViewById(R.id.add_employee_checkbox);

        hoTenTextView.setText(allEmployees.get(allEmployeesIds[position]).getHoTen());
        chuyenMonTextView.setText(allEmployees.get(allEmployeesIds[position]).getChuyenMon());

        //If salary is paid -> hide checkbox
        Salary salaryOfThisLine = allSalaries.get(SalaryRepository.getInstance(null)
                .getSalaryIdByEventIdAndEmployeeId(eventId, allEmployeesIds[position]));
        if (salaryOfThisLine != null && salaryOfThisLine.isPaid()) {
            addEmployeeCheckBox.setVisibility(View.INVISIBLE);
        } else {
            addEmployeeCheckBox.setVisibility(View.VISIBLE);
        }

        //If employee is selected -> set checkbox = checked
        if (selectedEmployeesIds.contains(allEmployeesIds[position])) {
            addEmployeeCheckBox.setChecked(true);
        } else {
            addEmployeeCheckBox.setChecked(false);
        }

        return view;
    }

    public String[] getAllEmployeesIds() {
        return allEmployeesIds;
    }

    public void setAllEmployeesIds(String[] allEmployeesIds) {
        this.allEmployeesIds = allEmployeesIds;
    }

    public HashMap<String, Employee> getAllEmployees() {
        return allEmployees;
    }

    public void notifyDataSetChanged(ArrayList<String> selectedEmployeesIds) {
        this.selectedEmployeesIds = selectedEmployeesIds;
        super.notifyDataSetChanged();
    }
}
