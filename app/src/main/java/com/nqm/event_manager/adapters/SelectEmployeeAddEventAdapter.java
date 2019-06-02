package com.nqm.event_manager.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnSelectEmployeeViewClicked;
import com.nqm.event_manager.models.Employee;

import java.util.ArrayList;

public class SelectEmployeeAddEventAdapter extends
        RecyclerView.Adapter<SelectEmployeeAddEventAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImageView;
        public TextView nameTextView, specialityTextView;
        public CheckBox selectCheckBox;

        public ViewHolder(View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.select_employee_profile_image_view);
            nameTextView = itemView.findViewById(R.id.select_employee_name_text_view);
            specialityTextView = itemView.findViewById(R.id.select_employee_speciality_text_view);
            selectCheckBox = itemView.findViewById(R.id.select_employee_select_checkbox);

            selectCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onCheckBoxClicked(employees.get(position).getId(), selectCheckBox.isChecked());
                        }
                    }
                }
            });
        }
    }

    private ArrayList<String> selectedEmployeesIds;
    private ArrayList<Employee> employees;
    private IOnSelectEmployeeViewClicked listener;

    public SelectEmployeeAddEventAdapter(ArrayList<String> selectedEmployeesIds, ArrayList<Employee> employees) {
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.employees = employees;
    }

    public void setListener(IOnSelectEmployeeViewClicked listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SelectEmployeeAddEventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View employeeView = inflater.inflate(R.layout.list_item_select_employee, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(employeeView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Employee employee = employees.get(i);

        TextView nameTextView = viewHolder.nameTextView;
        TextView specialityTextView = viewHolder.specialityTextView;
        CheckBox selectCheckBox = viewHolder.selectCheckBox;

        nameTextView.setText(employee.getHoTen());
        specialityTextView.setText(employee.getChuyenMon());

        selectCheckBox.setChecked(selectedEmployeesIds.contains(employee.getId()));
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }
}