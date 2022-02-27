package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnViewSalaryItemClicked;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.utils.EmployeeUtil;

import java.util.ArrayList;

public class ViewSalaryAdapter extends RecyclerView.Adapter<ViewSalaryAdapter.ViewHolder> {

    IOnViewSalaryItemClicked listener;
    private ArrayList<Salary> salaries;

    public ViewSalaryAdapter(ArrayList<Salary> salaries) {
        EmployeeUtil.sortSalariesByEmployeesNames(salaries);
        this.salaries = salaries;
    }

    public void setListener(IOnViewSalaryItemClicked listener) {
        this.listener = listener;
    }

    public void customNotifyDataSetChanged() {
        EmployeeUtil.sortSalariesByEmployeesNames(salaries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view_salary, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String employeeId = salaries.get(position).getEmployeeId();
        holder.hoTenTextView.setText(EmployeeRepository.getInstance().getAllEmployees().get(employeeId).getHoTen());
        holder.chuyenMonTextView.setText(EmployeeRepository.getInstance().getAllEmployees().get(employeeId).getChuyenMon());
        holder.luongEditText.setText(String.valueOf(salaries.get(position).getSalary()));
        holder.daThanhToanCheckBox.setChecked(salaries.get(position).isPaid());
    }

    @Override
    public int getItemCount() {
        return salaries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView hoTenTextView;
        TextView chuyenMonTextView;
        EditText luongEditText;
        CheckBox daThanhToanCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            hoTenTextView = itemView.findViewById(R.id.view_salary_employee_name_text_view);
            chuyenMonTextView = itemView.findViewById(R.id.view_salary_employee_speciality_text_view);
            luongEditText = itemView.findViewById(R.id.view_salary_salary_edit_text);
            daThanhToanCheckBox = itemView.findViewById(R.id.view_salary_paid_checkbox);

            itemView.setOnClickListener(v -> {
                listener.onViewSalaryItemClicked(salaries.get(getLayoutPosition()).getEmployeeId());
            });
        }
    }
}