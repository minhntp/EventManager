package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnViewSalaryItemClicked;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;

public class ViewSalaryAdapter extends RecyclerView.Adapter<ViewSalaryAdapter.ViewHolder> {

    IOnViewSalaryItemClicked listener;
    private ArrayList<Salary> salaries;

    public ViewSalaryAdapter(ArrayList<Salary> salaries) {
        this.salaries = salaries;
    }

    public void setListener(IOnViewSalaryItemClicked listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view_salary, parent, false);
        ViewHolder vh = new ViewHolder(view, position -> {
            listener.onViewSalaryItemClicked(salaries.get(position).getEmployeeId());
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String employeeId = salaries.get(position).getEmployeeId();
        holder.hoTenTextView.setText(EmployeeRepository.getInstance().getAllEmployees().get(employeeId).getHoTen());
        holder.chuyenMonTextView.setText(EmployeeRepository.getInstance().getAllEmployees().get(employeeId).getChuyenMon());
        holder.luongTextView.setText(String.valueOf(salaries.get(position).getSalary()));
        holder.daThanhToanCheckBox.setChecked(salaries.get(position).isPaid());
    }

    @Override
    public int getItemCount() {
        return salaries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView hoTenTextView;
        TextView chuyenMonTextView;
        TextView luongTextView;
        CheckBox daThanhToanCheckBox;
        IOnClick innerListener;

        public ViewHolder(@NonNull View itemView, IOnClick innerListener) {
            super(itemView);
            this.innerListener = innerListener;

            hoTenTextView = itemView.findViewById(R.id.view_salary_employee_name_text_view);
            chuyenMonTextView = itemView.findViewById(R.id.view_salary_employee_speciality_text_view);
            luongTextView = itemView.findViewById(R.id.view_salary_salary_text_view);
            daThanhToanCheckBox = itemView.findViewById(R.id.view_salary_paid_checkbox);

            itemView.setOnClickListener(v -> {
                innerListener.onClick(getLayoutPosition());
            });
        }
    }
}

interface IOnClick {
    void onClick(int position);
}

//public class ViewSalaryAdapter extends BaseAdapter {
//
//    private final Activity context;
//    IOnViewSalaryItemClicked listener;
//    private ArrayList<Salary> salaries;
//
//    public ViewSalaryAdapter(Activity context, ArrayList<Salary> salaries) {
//        this.context = context;
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
//            view = LayoutInflater.from(context).inflate(R.layout.list_item_view_salary, parent, false);
//        }
//
//        TextView hoTenTextView = view.findViewById(R.id.view_salary_employee_name_text_view);
//        TextView chuyenMonTextView = view.findViewById(R.id.view_salary_employee_speciality_text_view);
//        TextView luongTextView = view.findViewById(R.id.view_salary_salary_text_view);
//        CheckBox daThanhToanCheckBox = view.findViewById(R.id.view_salary_paid_checkbox);
//
//        //SHOW DATA
//        final String employeeId = getItem(position).getEmployeeId();
//        hoTenTextView.setText(EmployeeRepository.getInstance().getAllEmployees().get(employeeId).getHoTen());
//        chuyenMonTextView.setText(EmployeeRepository.getInstance().getAllEmployees().get(employeeId).getChuyenMon());
//        luongTextView.setText(String.valueOf(getItem(position).getSalary()));
//        daThanhToanCheckBox.setChecked(getItem(position).isPaid());
//
//        //ADD EVENTS
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listener.onViewSalaryItemClicked(employeeId);
//            }
//        });
//
//        return view;
//    }
//
//    public void setListener(IOnViewSalaryItemClicked listener) {
//        this.listener = listener;
//    }
//
//    public void notifyDataSetChanged(ArrayList<Salary> salaries) {
//        this.salaries = salaries;
//        super.notifyDataSetChanged();
//    }
//}
