package com.nqm.event_manager.adapters;

import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnCalculateSalaryItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.time.LocalDate;
import java.util.List;

public class CalculateSalaryAdapter extends RecyclerView.Adapter<CalculateSalaryAdapter.ViewHolder> {

    IOnCalculateSalaryItemClicked listener;
    List<Salary> salaries;
    int editedSum;

    public CalculateSalaryAdapter(List<Salary> salaries) {
        this.salaries = salaries;
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
        Salary salary = salaries.get(position);
        Event event = EventRepository.getInstance().getAllEvents().get(salary.getEventId());
        if (event != null) {
            holder.dowTextView.setText(CalendarUtil.dayOfWeekInVietnamese(event.getNgayBatDau()));
            String shortDate = LocalDate.parse(event.getNgayBatDau(), CalendarUtil.dtfDayMonthYear)
                    .format(CalendarUtil.dtfDayMonthYearShort);
            holder.startDateTextView.setText(shortDate);
            holder.titleTextView.setText(event.getTen());
            holder.locationTextView.setText(event.getDiaDiem());

            if (salary.isPaid()) {
                holder.salaryEditText.setText(String.valueOf(salary.getSalary()));
                holder.paidCheckBox.setChecked(true);
                holder.paidCheckBox.setEnabled(false);
                holder.salaryEditText.setEnabled(false);
            } else {
                holder.salaryEditText.setText(String.valueOf(salary.getEditedSalary()));
                holder.paidCheckBox.setChecked(salary.isChecked());
                holder.paidCheckBox.setEnabled(true);
                holder.salaryEditText.setEnabled(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return salaries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView dowTextView;
        TextView startDateTextView;
        TextView titleTextView;
        TextView locationTextView;
        EditText salaryEditText;
        CheckBox paidCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dowTextView = itemView.findViewById(R.id.calculate_salaries_list_item_dow_text_view);
            startDateTextView = itemView.findViewById(R.id.calculate_salaries_list_item_date_text_view);
            titleTextView = itemView.findViewById(R.id.calculate_salaries_list_item_event_title_text_view);
            locationTextView = itemView.findViewById(R.id.calculate_salaries_list_item_event_location_text_view);
            salaryEditText = itemView.findViewById(R.id.calculate_salaries_list_item_salary_edit_text);
            paidCheckBox = itemView.findViewById(R.id.calculate_salaries_list_item_paid_checkbox);

            //Add events
            View.OnClickListener onClickListener = v -> listener.onCalculateSalaryItemClicked(salaries.get(getLayoutPosition()).getEventId());

            dowTextView.setOnClickListener(onClickListener);
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
                    salaries.get(getLayoutPosition()).setEditedSalary(editedNumber);
                }
            });

            paidCheckBox.setOnClickListener(view -> salaries.get(getLayoutPosition()).setChecked(paidCheckBox.isChecked()));

            View.OnLongClickListener onLongClickListener = view -> {
                if (!paidCheckBox.isEnabled()) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Bạn có chắc chắn muốn sửa thành \"Chưa trả\"?")
                            .setIcon(R.drawable.ic_error)
                            .setPositiveButton("Sửa", (dialog, whichButton) ->
                                    listener.onCalculateSalaryInputLayoutLongClicked(
                                            salaries.get(getLayoutPosition()).getId()))
                            .setNegativeButton("Hủy", null).show();
                }
                return false;
            };

            dowTextView.setOnLongClickListener(onLongClickListener);
            startDateTextView.setOnLongClickListener(onLongClickListener);
            titleTextView.setOnLongClickListener(onLongClickListener);
            locationTextView.setOnLongClickListener(onLongClickListener);

        }
    }
}