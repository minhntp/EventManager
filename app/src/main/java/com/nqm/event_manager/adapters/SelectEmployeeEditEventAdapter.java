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
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;

public class SelectEmployeeEditEventAdapter extends
        RecyclerView.Adapter<SelectEmployeeEditEventAdapter.ViewHolder> {

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
    private String eventId;

    public SelectEmployeeEditEventAdapter(ArrayList<String> selectedEmployeesIds, ArrayList<Employee> employees,
                                          String eventId) {
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.employees = employees;
        this.eventId = eventId;
    }

    public void setListener(IOnSelectEmployeeViewClicked listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SelectEmployeeEditEventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View employeeView = inflater.inflate(R.layout.list_item_select_employee, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(employeeView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Employee employee = employees.get(i);

        TextView nameTextView = viewHolder.nameTextView;
        TextView specialityTextView = viewHolder.specialityTextView;
        final CheckBox selectCheckBox = viewHolder.selectCheckBox;

        nameTextView.setText(employee.getHoTen());
        specialityTextView.setText(employee.getChuyenMon());

        SalaryRepository.getInstance().isSalaryPaid(employee.getId(), eventId, new SalaryRepository.MyIsPaidSalaryCallback() {
            @Override
            public void onCallback(boolean isPaid) {
                if (isPaid) {
                    selectCheckBox.setVisibility(View.INVISIBLE);
                } else {
                    selectCheckBox.setVisibility(View.VISIBLE);
                    selectCheckBox.setChecked(selectedEmployeesIds.contains(employee.getId()));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return employees.size();
    }
}

