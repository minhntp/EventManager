package com.nqm.event_manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnEditEmployeeItemClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.utils.EmployeeUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class EditEmployeeEditEventAdapter extends
        RecyclerView.Adapter<EditEmployeeEditEventAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImageView;
        TextView nameTextView;
        TextView specialityTextView;
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.edit_employee_list_item_profile_image_view);
            nameTextView = itemView.findViewById(R.id.edit_employee_list_item_name_text_view);
            specialityTextView = itemView.findViewById(R.id.edit_employee_list_item_speciality_text_view);
            deleteButton = itemView.findViewById(R.id.edit_employee_list_item_delete_button);

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteButtonClicked(selectedEmployeesIds.get(position));
                    }
                }
            });

            View.OnClickListener itemClickListener = v -> {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onListItemClicked(selectedEmployeesIds.get(position));
                    }
                }
            };

            profileImageView.setOnClickListener(itemClickListener);
            nameTextView.setOnClickListener(itemClickListener);
            specialityTextView.setOnClickListener(itemClickListener);
        }
    }

    IOnEditEmployeeItemClicked listener;
    private ArrayList<String> selectedEmployeesIds;
    private HashMap<String, ArrayList<String>> conflictsMap;
    private String eventId;

    public EditEmployeeEditEventAdapter(String eventId, ArrayList<String> selectedEmployeesIds,
                                        HashMap<String, ArrayList<String>> conflictsMap) {
        this.eventId = eventId;
        EmployeeUtil.sortEmployeesIdsByNameNew(selectedEmployeesIds);
        this.selectedEmployeesIds = selectedEmployeesIds;
        this.conflictsMap = conflictsMap;
    }

    public void setListener(IOnEditEmployeeItemClicked listener) {
        this.listener = listener;
    }

    public void customNotifyDataSetChanged() {
        EmployeeUtil.sortEmployeesIdsByNameNew(selectedEmployeesIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View employeeView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_edit_employee, viewGroup, false);

        return new ViewHolder(employeeView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        Employee employee = EmployeeRepository.getInstance().getAllEmployees().get(selectedEmployeesIds.get(i));

        if (employee != null) {
            ArrayList<String> conflictEventsIds = conflictsMap.get(employee.getId());
            if (conflictEventsIds != null && conflictEventsIds.size() > 0) {
//                int color = EventManager.getAppContext().getColor(R.color.conflictBackground);
                int color = viewHolder.nameTextView.getContext().getColor(R.color.conflictBackground);
                viewHolder.profileImageView.setBackgroundColor(color);
                viewHolder.nameTextView.setBackgroundColor(color);
                viewHolder.specialityTextView.setBackgroundColor(color);
            } else {
                viewHolder.profileImageView.setBackgroundColor(0);
                viewHolder.nameTextView.setBackgroundColor(0);
                viewHolder.specialityTextView.setBackgroundColor(0);
            }

            viewHolder.nameTextView.setText(employee.getHoTen());
            viewHolder.specialityTextView.setText(employee.getChuyenMon());

            boolean isPaid = SalaryRepository.getInstance().isSalaryPaid(employee.getId(), eventId);
            if (isPaid) {
                viewHolder.deleteButton.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.deleteButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return selectedEmployeesIds.size();
    }

}
