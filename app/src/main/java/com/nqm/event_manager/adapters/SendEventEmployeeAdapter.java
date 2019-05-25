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
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;

public class SendEventEmployeeAdapter extends BaseAdapter {

    Activity context;
    ArrayList<String> employeesIds;

    public SendEventEmployeeAdapter(Activity context, ArrayList<String> employeesIds) {
        this.context = context;
        this.employeesIds = employeesIds;
    }

    @Override
    public int getCount() {
        return employeesIds.size();
    }

    @Override
    public Employee getItem(int position) {
        return EmployeeRepository.getInstance(null).getAllEmployees().get(employeesIds.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_send_event, parent, false);
        }

        //Connect views
        TextView nameTextView = view.findViewById(R.id.send_event_item_name_text_view);
        CheckBox selectCheckBox = view.findViewById(R.id.send_event_select_item_checkbox);

        //Fill info
        nameTextView.setText(getItem(position).getHoTen());
        selectCheckBox.setChecked(true);

        return view;
    }
}
