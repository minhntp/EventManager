package com.nqm.event_manager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.utils.EmployeeUtil;

import java.util.ArrayList;

public class EditSalaryAdapter extends RecyclerView.Adapter<EditSalaryAdapter.ViewHolder> {

    private ArrayList<Salary> salaries;

    public EditSalaryAdapter(ArrayList<Salary> salaries) {
        EmployeeUtil.sortSalariesByEmployeesNames(salaries);
        this.salaries = salaries;
    }

    public void customNotifyDataSetChanged() {
        EmployeeUtil.sortSalariesByEmployeesNames(salaries);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_edit_salary,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Employee employee = EmployeeRepository.getInstance().getAllEmployees().get(salaries.get(position).getEmployeeId());
        if (employee != null) {
            //Fill information
            holder.hoTenTextView.setText(employee.getHoTen());
            holder.chuyenMonTextView.setText(employee.getChuyenMon());
        }
        holder.salaryEditText.setText(String.valueOf(salaries.get(position).getSalary()));
        holder.paidCheckBox.setChecked(salaries.get(position).isPaid());

        if (holder.paidCheckBox.isChecked()) {
            holder.paidCheckBox.setEnabled(false);
            holder.salaryEditText.setEnabled(false);
        } else {
            holder.paidCheckBox.setEnabled(true);
            holder.salaryEditText.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return salaries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView hoTenTextView;
        TextView chuyenMonTextView;
        EditText salaryEditText;
        CheckBox paidCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            hoTenTextView = itemView.findViewById(R.id.edit_salary_employee_name_text_view);
            chuyenMonTextView = itemView.findViewById(R.id.edit_salary_employee_speciality_text_view);
            salaryEditText = itemView.findViewById(R.id.edit_salary_salary_edit_text);
            paidCheckBox = itemView.findViewById(R.id.edit_salary_paid_checkbox);
        }
    }
}

//public class EditSalaryAdapter extends BaseAdapter {
//
//
//    public EditSalaryAdapter(Activity context, ArrayList<Salary> salaries) {
//        this.context = context;
//        res = context.getResources();
//        this.salaries = salaries;
//    }
//
//    @Override
//    public int getCount() {
//        return salaries.size();
//    }
//
//    @Override
//    public Salary getItem(int i) {
//        return salaries.get(i);
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return i;
//    }
//
//    @Override
//    public View getView(final int position, View view, ViewGroup parent) {
//        if (view == null) {
//            view = LayoutInflater.from(context).inflate(R.layout.list_item_edit_salary, parent, false);
//        }
//
//        TextView hoTenTextView = view.findViewById(R.id.edit_salary_employee_name_text_view);
//        TextView chuyenMonTextView = view.findViewById(R.id.edit_salary_employee_speciality_text_view);
//        EditText salaryEditText = view.findViewById(R.id.edit_salary_salary_edit_text);
//        CheckBox paidCheckBox = view.findViewById(R.id.edit_salary_paid_checkbox);
//
//        Employee employee = EmployeeRepository.getInstance().getAllEmployees().get(getItem(position).getEmployeeId());
//        if (employee != null) {
//            //Fill information
//            hoTenTextView.setText(employee.getHoTen());
//            chuyenMonTextView.setText(employee.getChuyenMon());
//        }
//        salaryEditText.setText(String.valueOf(getItem(position).getSalary()));
//        paidCheckBox.setChecked(getItem(position).isPaid());
//
//        if (paidCheckBox.isChecked()) {
//            paidCheckBox.setEnabled(false);
//            salaryEditText.setEnabled(false);
//        } else {
//            paidCheckBox.setEnabled(true);
//            salaryEditText.setEnabled(true);
//        }
//
//        return view;
//    }
//
//    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
//    }
//}
