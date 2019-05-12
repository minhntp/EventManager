package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.SalaryRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class EditSalaryAdapter extends BaseAdapter {
    private final Activity context;
    private HashMap<String, Salary> allSalaries;
    private ArrayList<String> salariesIds;
    private HashMap<String, Employee> allEmployees;

    public EditSalaryAdapter(Activity context, ArrayList<String> salariesIds) {
        this.context = context;
        this.salariesIds = salariesIds;
        allSalaries = SalaryRepository.getInstance(null).getAllSalaries();
        allEmployees = EmployeeRepository.getInstance(null).getAllEmployees();
    }

    @Override
    public int getCount() {
        return salariesIds.size();
    }

    @Override
    public Object getItem(int i) {
        return allSalaries.get(salariesIds.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_edit_salary_list_item, parent, false);
        }
        TextView hoTenTextView = view.findViewById(R.id.edit_salary_employee_name_text_view);
        TextView chuyenMonTextView = view.findViewById(R.id.edit_salary_employee_speciality_text_view);
        final EditText salaryEditText = view.findViewById(R.id.edit_salary_salary_edit_text);
        final CheckBox paidCheckBox = view.findViewById(R.id.edit_salary_paid_checkbox);

        //Fill information
        hoTenTextView.setText(allEmployees.get(allSalaries.get(salariesIds.get(position)).getEmployeeId()).getHoTen());
        chuyenMonTextView.setText(allEmployees.get(allSalaries.get(salariesIds.get(position)).getEmployeeId()).getChuyenMon());
        salaryEditText.setText("" + allSalaries.get(salariesIds.get(position)).getSalary());
        paidCheckBox.setChecked(allSalaries.get(salariesIds.get(position)).isPaid());

        if (paidCheckBox.isChecked()) {
            paidCheckBox.setEnabled(false);
            salaryEditText.setEnabled(false);
        } else {
            paidCheckBox.setEnabled(true);
            salaryEditText.setEnabled(true);
        }


//        paidCheckBox.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    new AlertDialog.Builder(context)
//                            .setTitle("Xác nhận trả lương")
//                            .setIcon(android.R.drawable.ic_dialog_alert)
//                            .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int whichButton) {
//                                    if (paidCheckBox.isChecked()) {
//                                        paidCheckBox.setEnabled(false);
//                                        salaryEditText.setEnabled(false);
//                                    } else {
//                                        paidCheckBox.setEnabled(true);
//                                        salaryEditText.setEnabled(true);
//                                    }
//                                }
//                            })
//                            .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    paidCheckBox.setChecked(false);
//                                }
//                            }).show();
//            }
//        });

        return view;
    }

    private void setPaidCheckBox() {

    }

    public ArrayList<String> getSalariesIds() {
        return salariesIds;
    }

    public void setSalariesIds(ArrayList<String> salariesIds) {
        this.salariesIds = salariesIds;
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
