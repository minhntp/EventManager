package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.CalculateSalaryForSingleEmployeeActivity;
import com.nqm.event_manager.interfaces.IOnAddScheduleViewClicked;
import com.nqm.event_manager.interfaces.IOnManageEmployeeViewClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;

import java.util.ArrayList;

public class ViewEmployeeListAdapter extends BaseAdapter {
    Activity context;
    ArrayList<String> employeesIds;
    IOnManageEmployeeViewClicked listener;

    public ViewEmployeeListAdapter(Activity context, ArrayList<String> employeesIds) {
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
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_view_employee_list_item, parent, false);
        }

        //connect views
        ImageView profileImageView = view.findViewById(R.id.view_employee_profile_image_view);
        TextView nameTextView = view.findViewById(R.id.view_employee_name_text_view);
        TextView specialityTextView = view.findViewById(R.id.view_employee_speciality_text_view);
        ImageButton callImageButton = view.findViewById(R.id.view_employee_call_button);
        ImageButton messageImageButton = view.findViewById(R.id.view_employee_message_button);
        ImageButton emailImageButton = view.findViewById(R.id.view_employee_email_button);
        ImageButton calculateSalaryImageButton = view.findViewById(R.id.view_employee_calculate_salary_button);

        //fill info
        nameTextView.setText(getItem(position).getHoTen());
        specialityTextView.setText(getItem(position).getChuyenMon());

        //add events
        callImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall(getItem(position).getSdt());
            }
        });

        messageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(getItem(position).getSdt());
            }
        });

        emailImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(getItem(position).getEmail());
            }
        });

        calculateSalaryImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateSalary(getItem(position).getId());
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEmployeeListItemClicked(getItem(position).getId());
            }
        });

        nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEmployeeListItemClicked(getItem(position).getId());
            }
        });

        specialityTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEmployeeListItemClicked(getItem(position).getId());
            }
        });

        return view;
    }

    public void setListener(IOnManageEmployeeViewClicked listener) {
        this.listener = listener;
    }

    private void makePhoneCall(String phoneNumber) {
        if(!phoneNumber.isEmpty()) {
            context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
        } else {
            Toast.makeText(context, "Không có số điện thoại", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage(String phoneNumber) {
        if(!phoneNumber.isEmpty()) {
            context.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", phoneNumber, null)));
        } else {
            Toast.makeText(context, "Không có số điện thoại", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String emailAddress) {
        if(!emailAddress.isEmpty()) {
            String[] TO = {emailAddress};
            String[] CC = {""};
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setDataAndType(Uri.parse("mailto"), "text/plain");

            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
            emailIntent.putExtra(Intent.EXTRA_CC, CC);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");

            try {
                context.startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng gửi Email..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "Không tìm thấy ứng dụng gửi Email", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Không có địa chỉ Email", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateSalary(String employeeId) {
        Intent intent = new Intent(context, CalculateSalaryForSingleEmployeeActivity.class);
        intent.putExtra("employeeId", employeeId);
        context.startActivity(intent);
    }

    public void notifyDataSetChanged(ArrayList<String> employeesIds) {
        this.employeesIds = employeesIds;
        super.notifyDataSetChanged();
    }
}
