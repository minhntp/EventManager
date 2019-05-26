package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class EditEmployeeFromEditEventAdapter extends BaseAdapter {
    private Activity context;
    private ArrayList<String> selectedEmployeesIds;
    private HashMap<String, Employee> allEmployees;
    private String eventId;

    public EditEmployeeFromEditEventAdapter(Activity context, String eventId,
                                            ArrayList<String> selectedEmployeesIds) {
        this.context = context;
        this.eventId = eventId;
        this.selectedEmployeesIds = selectedEmployeesIds;
        allEmployees = EmployeeRepository.getInstance().getAllEmployees();
    }

    @Override
    public int getCount() {
        return selectedEmployeesIds.size();
    }

    @Override
    public Object getItem(int i) {
        return allEmployees.get(selectedEmployeesIds.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_edit_employee, parent, false);
        }

        //Connect views
//        TextView hoTenTextView = view.findViewById(R.id.delete_employee_list_item_employee_name);
//        TextView chuyenMonTextView = view.findViewById(R.id.delete_employee_list_item_employee_speciality);
//        ImageButton deleteEmployeeButton = view.findViewById(R.id.delete_employee_list_item_delete_button);

        //Fill information
//        hoTenTextView.setText(allEmployees.get(selectedEmployeesIds.get(position)).getHoTen());
//        chuyenMonTextView.setText(allEmployees.get(selectedEmployeesIds.get(position)).getChuyenMon());

        //Delete
//        deleteEmployeeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                selectedEmployeesIds.remove(position);
//                notifyDataSetChanged();
//            }
//        });

        //If salary is paid -> hide deleteButton
        Salary salaryOfThisLine = SalaryRepository.getInstance(null).getAllSalaries()
                .get(SalaryRepository.getInstance(null).getSalaryIdByEventIdAndEmployeeId(eventId,
                        selectedEmployeesIds.get(position)));
//        if (salaryOfThisLine != null && salaryOfThisLine.isPaid()) {
//            deleteEmployeeButton.setVisibility(View.INVISIBLE);
//        } else {
//            deleteEmployeeButton.setVisibility(View.VISIBLE);
//        }

        //Check for conflict -> show conflict and set on click listener -> conflict activity


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
