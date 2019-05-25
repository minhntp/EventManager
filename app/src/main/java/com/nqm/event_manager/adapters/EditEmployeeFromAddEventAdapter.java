package com.nqm.event_manager.adapters;


import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.application.EventManager;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class EditEmployeeFromAddEventAdapter extends BaseAdapter {
    Activity context;
    private ArrayList<String> selectedEmployeesIds;
    private HashMap<String, Employee> allEmployees;

    public EditEmployeeFromAddEventAdapter(Activity context, ArrayList<String> selectedEmployeesIds) {
        this.context = context;
        this.selectedEmployeesIds = selectedEmployeesIds;
        allEmployees = EmployeeRepository.getInstance().getAllEmployees();
    }

    @Override
    public int getCount() {
        return selectedEmployeesIds.size();
    }

    @Override
    public Employee getItem(int i) {
        return allEmployees.get(selectedEmployeesIds.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_add_employee, parent, false);
        }

        //Connect views
        TextView hoTenTextView = view.findViewById(R.id.delete_employee_list_item_employee_name);
        TextView chuyenMonTextView = view.findViewById(R.id.delete_employee_list_item_employee_speciality);
        ImageButton deleteEmployeeButton = view.findViewById(R.id.delete_employee_list_item_delete_button);

        //Fill information
        hoTenTextView.setText(allEmployees.get(selectedEmployeesIds.get(position)).getHoTen());
        chuyenMonTextView.setText(allEmployees.get(selectedEmployeesIds.get(position)).getChuyenMon());

        //Delete
        deleteEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedEmployeesIds.remove(position);
                notifyDataSetChanged();
            }
        });

        return view;
    }

    public ArrayList<String> getSelectedEmployeesIds() {
        return selectedEmployeesIds;
    }

    public void setSelectedEmployeesIds(ArrayList<String> selectedEmployeesIds) {
        this.selectedEmployeesIds = selectedEmployeesIds;
    }

    public void notifyDataSetChanged(ArrayList<String> selectedEmployeesIds) {
        this.selectedEmployeesIds = selectedEmployeesIds;
        super.notifyDataSetChanged();
    }
}
