package com.nqm.event_manager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.application.EventManager;
import com.nqm.event_manager.interfaces.IOnEditEmployeeViewClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class EditEmployeeEditEventAdapter extends
        RecyclerView.Adapter<EditEmployeeEditEventAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView profileImageView;
        public TextView nameTextView;
        public TextView specialityTextView;
        public ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.edit_employee_list_item_profile_image_view);
            nameTextView = itemView.findViewById(R.id.edit_employee_list_item_name_text_view);
            specialityTextView = itemView.findViewById(R.id.edit_employee_list_item_speciality_text_view);
            deleteButton = itemView.findViewById(R.id.edit_employee_list_item_delete_button);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteButtonClicked(selectedEmployeesIds.get(position));
                        }
                    }
                }
            });

            View.OnClickListener itemClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onListItemClicked(selectedEmployeesIds.get(position));
                        }
                    }
                }
            };

            profileImageView.setOnClickListener(itemClickListener);
            nameTextView.setOnClickListener(itemClickListener);
            specialityTextView.setOnClickListener(itemClickListener);
        }
    }

    IOnEditEmployeeViewClicked listener;
    private ArrayList<String> selectedEmployeesIds;
    private HashMap<String, ArrayList<String>> conflictsMap;
    private String eventId;

    public EditEmployeeEditEventAdapter(String eventId, ArrayList<String> selectedEmployeesIds,
                                       HashMap<String, ArrayList<String>> conflictsMap) {
        this.eventId = eventId;
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.conflictsMap = conflictsMap;
    }

    public void setListener(IOnEditEmployeeViewClicked listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View employeeView = inflater.inflate(R.layout.list_item_edit_employee, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(employeeView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Employee employee = EmployeeRepository.getInstance().getAllEmployees().get(selectedEmployeesIds.get(i));

        ImageView profileImageView = viewHolder.profileImageView;
        TextView nameTextView = viewHolder.nameTextView;
        TextView specialityTextView = viewHolder.specialityTextView;
        final ImageButton deleteButton = viewHolder.deleteButton;

        if (conflictsMap.get(employee.getId()) != null && conflictsMap.get(employee.getId()).size() > 0) {
            profileImageView.setBackgroundColor(EventManager.getAppContext().getColor(R.color.conflictBackground));
            nameTextView.setBackgroundColor(EventManager.getAppContext().getColor(R.color.conflictBackground));
            specialityTextView.setBackgroundColor(EventManager.getAppContext().getColor(R.color.conflictBackground));
        } else {
            profileImageView.setBackgroundColor(Color.TRANSPARENT);
            nameTextView.setBackgroundColor(Color.TRANSPARENT);
            specialityTextView.setBackgroundColor(Color.TRANSPARENT);
        }

        nameTextView.setText(employee.getHoTen());
        specialityTextView.setText(employee.getChuyenMon());

        SalaryRepository.getInstance().isSalaryPaid(employee.getId(), eventId, new SalaryRepository.MyIsPaidSalaryCallback() {
            @Override
            public void onCallback(boolean isPaid) {
                if (isPaid) {
                    deleteButton.setVisibility(View.INVISIBLE);
                } else {
                    deleteButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedEmployeesIds.size();
    }

}
