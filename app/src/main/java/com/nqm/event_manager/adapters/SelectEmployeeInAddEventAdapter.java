package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectEmployeeInAddEventAdapter extends BaseAdapter implements IOnDataLoadComplete {

    private final Activity context;
    private ArrayList<String> selectedEmployeesIds;
    private HashMap<String, Employee> allEmployees;
    private String[] allEmployeesIds;

    public SelectEmployeeInAddEventAdapter(Activity context, ArrayList<String> selectedEmployeesIds) {
        this.context = context;
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.allEmployees = EmployeeRepository.getInstance().getAllEmployees();
        allEmployeesIds = allEmployees.keySet().toArray(new String[allEmployees.size()]);
        EmployeeRepository.getInstance().setListener(this);
    }

    @Override
    public int getCount() {
        return allEmployees.size();
    }

    @Override
    public Object getItem(int i) {
        return allEmployees.get(i);
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
        CheckBox addEmployeeCheckBox = view.findViewById(R.id.add_employee_checkbox);

        //Fill information
        hoTenTextView.setText(allEmployees.get(allEmployeesIds[position]).getHoTen());
        chuyenMonTextView.setText(allEmployees.get(allEmployeesIds[position]).getChuyenMon());

        //--get checkbox to checked if this employees id is contained in selectedEmployeesIds
        if (selectedEmployeesIds.size() > 0 && selectedEmployeesIds.contains(allEmployees.get(allEmployeesIds[position]).getId())) {
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

    public void notifyDataSetChanged(ArrayList<String> selectedEmployeesIds) {
        this.selectedEmployeesIds = selectedEmployeesIds;
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyOnLoadComplete() {
        allEmployees = EmployeeRepository.getInstance().getAllEmployees();
        allEmployeesIds = allEmployees.keySet().toArray(new String[allEmployees.size()]);
    }

}
