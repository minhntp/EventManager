package com.nqm.event_manager.adapters;

import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnCalculateSalaryItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CalculateSalaryAdapter extends RecyclerView.Adapter<CalculateSalaryAdapter.ViewHolder> {

    IOnCalculateSalaryItemClicked listener;
    ArrayList<Salary> editedSalaries;

    int editedSum;

    public CalculateSalaryAdapter(ArrayList<Salary> editedSalaries) {
        this.editedSalaries = editedSalaries;
        editedSum = 0;
    }

    public void setListener(IOnCalculateSalaryItemClicked listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_calculate_salary, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Salary salary = editedSalaries.get(position);
        Event event = EventRepository.getInstance().getAllEvents().get(salary.getEventId());
        if (event != null) {
            try {
                Date startDate = CalendarUtil.sdfDayMonthYear.parse(event.getNgayBatDau());
                String toText = CalendarUtil.sdfDayMonth.format(startDate) + "\n" +
                        CalendarUtil.dayOfWeekInVietnamese(event.getNgayBatDau());
                holder.startDateTextView.setText(toText);
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.titleTextView.setText(event.getTen());
            holder.locationTextView.setText(event.getDiaDiem());

            if (salary.isPaid()) {
                holder.paidCheckBox.setEnabled(false);
                holder.salaryEditText.setEnabled(false);

                holder.paidCheckBox.setChecked(true);
                holder.salaryEditText.setText(String.valueOf(salary.getSalary()));

            } else {
                holder.paidCheckBox.setEnabled(true);
                holder.salaryEditText.setEnabled(true);

                holder.salaryEditText.setText(String.valueOf(salary.getSalary()));

                holder.paidCheckBox.setChecked(salary.isPaid());
            }
        }
    }

    @Override
    public int getItemCount() {
        return editedSalaries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView startDateTextView;
        TextView titleTextView;
        TextView locationTextView;
        EditText salaryEditText;
        CheckBox paidCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            startDateTextView = itemView.findViewById(R.id.calculate_salaries_list_item_date_text_view);
            titleTextView = itemView.findViewById(R.id.calculate_salaries_list_item_event_title_text_view);
            locationTextView = itemView.findViewById(R.id.calculate_salaries_list_item_event_location_text_view);
            salaryEditText = itemView.findViewById(R.id.calculate_salaries_list_item_salary_edit_text);
            paidCheckBox = itemView.findViewById(R.id.calculate_salaries_list_item_paid_checkbox);

            //Add events
            View.OnClickListener onClickListener = v -> {
                listener.onCalculateSalaryItemClicked(editedSalaries.get(getLayoutPosition()).getEventId());
            };

            startDateTextView.setOnClickListener(onClickListener);
            titleTextView.setOnClickListener(onClickListener);
            locationTextView.setOnClickListener(onClickListener);

            salaryEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    int editedNumber = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                    editedSalaries.get(getLayoutPosition()).setSalary(editedNumber);
                }
            });

            paidCheckBox.setOnClickListener(view -> editedSalaries.get(getLayoutPosition()).setPaid(paidCheckBox.isChecked()));

            View.OnLongClickListener onLongClickListener = view -> {
                if (!paidCheckBox.isEnabled()) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Bạn có chắc chắn muốn sửa thành \"Chưa trả\"?")
                            .setIcon(R.drawable.ic_error)
                            .setPositiveButton("Sửa", (dialog, whichButton) ->
                                    listener.onCalculateSalaryInputLayoutLongClicked(
                                            editedSalaries.get(getLayoutPosition()).getSalaryId()))
                            .setNegativeButton("Hủy", null).show();
                }
                return false;
            };

            startDateTextView.setOnLongClickListener(onLongClickListener);
            titleTextView.setOnLongClickListener(onLongClickListener);
            locationTextView.setOnLongClickListener(onLongClickListener);

        }
    }
}