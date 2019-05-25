package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.application.EventManager;
import com.nqm.event_manager.interfaces.IOnSelectEmployeeViewClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectEmployeeInEditEventAdapter extends BaseAdapter {
    private ArrayList<Employee> employees;
    private ArrayList<String> selectedEmployeesIds;
    private String eventId;
    private Activity context;

    private IOnSelectEmployeeViewClicked listener;

    public SelectEmployeeInEditEventAdapter(Activity context, String eventId, ArrayList<String> selectedEmployeesIds,
                                            ArrayList<Employee> employees) {
        this.context = context;
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.eventId = eventId;
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
    public Employee getItem(int i) {
        return employees.get(i);
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

        TextView hoTenTextView = view.findViewById(R.id.select_ho_ten_text_view);
        TextView chuyenMonTextView = view.findViewById(R.id.select_chuyen_mon_text_view);
        final CheckBox addEmployeeCheckBox = view.findViewById(R.id.add_employee_checkbox);

        hoTenTextView.setText(getItem(position).getHoTen());
        chuyenMonTextView.setText(getItem(position).getChuyenMon());

        //If salary is paid -> hide checkbox
        String salaryId = SalaryRepository.getInstance()
                .getSalaryIdByEventIdAndEmployeeId(eventId, getItem(position).getId());
        if (!salaryId.isEmpty() && SalaryRepository.getInstance().getAllSalaries().get(salaryId).isPaid()) {
            addEmployeeCheckBox.setVisibility(View.INVISIBLE);
        } else {
            addEmployeeCheckBox.setVisibility(View.VISIBLE);
        }

        //If employee is selected -> set checkbox = checked
        if (selectedEmployeesIds.contains(getItem(position).getId())) {
            addEmployeeCheckBox.setChecked(true);
        } else {
            addEmployeeCheckBox.setChecked(false);
        }

        addEmployeeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCheckBoxClicked(getItem(position).getId(), addEmployeeCheckBox.isChecked());
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

    public void notifyDataSetChanged(ArrayList<String> selectedEmployeesIds, ArrayList<Employee> employees) {
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.employees = employees;
        super.notifyDataSetChanged();
    }
}
