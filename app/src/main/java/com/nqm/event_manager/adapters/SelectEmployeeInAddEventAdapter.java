package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.application.EventManager;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnSelectEmployeeViewClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectEmployeeInAddEventAdapter extends BaseAdapter {

    Activity context;
    private ArrayList<String> selectedEmployeesIds;
    private ArrayList<Employee> employees;
    private IOnSelectEmployeeViewClicked listener;

    public SelectEmployeeInAddEventAdapter(Activity context, ArrayList<String> selectedEmployeesIds,
                                           ArrayList<Employee> employees) {
        this.context = context;
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.employees = employees;
    }

    public void setListener(IOnSelectEmployeeViewClicked listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return employees.size();
    }

    @Override
    public Employee getItem(int position) {
        return employees.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_select_employee, parent, false);
        }

        //Connect views
        TextView hoTenTextView = view.findViewById(R.id.select_ho_ten_text_view);
        TextView chuyenMonTextView = view.findViewById(R.id.select_chuyen_mon_text_view);
        final CheckBox addEmployeeCheckBox = view.findViewById(R.id.add_employee_checkbox);

        //Fill information
        hoTenTextView.setText(getItem(position).getHoTen());
        chuyenMonTextView.setText(getItem(position).getChuyenMon());

        //if employee is selected -> set checkbox to checked
        if (selectedEmployeesIds.contains(getItem(position).getId())) {
            addEmployeeCheckBox.setChecked(true);
        } else {
            addEmployeeCheckBox.setChecked(false);
        }

        //update selectedEmployees when user click on checkbox
        addEmployeeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCheckBoxClicked(getItem(position).getId(), addEmployeeCheckBox.isChecked());
            }
        });

        return view;
    }

    public void setSelectedEmployeesIds(ArrayList<String> selectedEmployeesIds) {
        this.selectedEmployeesIds = selectedEmployeesIds;
    }

    public ArrayList<String> getSelectedEmployeesIds() {
        return selectedEmployeesIds;
    }

    public void notifyDataSetChanged(ArrayList<String> selectedEmployeesIds, ArrayList<Employee> employees) {
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.employees = employees;
        super.notifyDataSetChanged();
    }


}
