package com.nqm.event_manager.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.nqm.event_manager.interfaces.IOnCalculateSalaryItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CalculateSalaryAdapter extends RecyclerView.Adapter<CalculateSalaryAdapter.ViewHolder> {

    IOnCalculateSalaryItemClicked listener;
    ArrayList<Salary> salaries;

    // <SalaryId, editedAmount>
    HashMap<String, Integer> editedAmountArray;

    // <SalaryId, checked>
    HashMap<String, Boolean> checkedArray;

    int editedSum;

    public CalculateSalaryAdapter(ArrayList<Salary> salaries) {
        this.salaries = salaries;
        editedAmountArray = new HashMap<>();
        checkedArray = new HashMap<>();
        editedSum = 0;
    }

    public void setListener(IOnCalculateSalaryItemClicked listener) {
        this.listener = listener;
    }

//    public void customNotifyDataSetChanged(ArrayList<Salary> resultSalaries) {
//        this.resultSalaries = resultSalaries;
//        notifyDataSetChanged();
//    }
    public void customNotifyDataSetChanged() {
        Log.d("dbg", "customNotifyDataSetChanged: new size = " + salaries.size());
        notifyDataSetChanged();
        editedAmountArray.clear();
        checkedArray.clear();
    }

    public HashMap<String, Boolean> getCheckedArray() {
        return checkedArray;
    }

    public HashMap<String, Integer> getEditedAmountArray() {
        return editedAmountArray;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_calculate_salary, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Salary salary = salaries.get(position);
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

                if(editedAmountArray.get(salary.getSalaryId()) != null) {
                    holder.salaryEditText.setText(String.valueOf(
                            editedAmountArray.get(salaries.get(position).getSalaryId())));
                } else {
                    holder.salaryEditText.setText(String.valueOf(salary.getSalary()));
                }

                if(checkedArray.get(salary.getSalaryId()) != null) {
                    holder.paidCheckBox.setChecked(checkedArray.get(salary.getSalaryId()));
                } else {
                    holder.paidCheckBox.setChecked(salary.isPaid());
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return salaries.size();
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
                listener.onCalculateSalaryItemClicked(salaries.get(getLayoutPosition()).getEventId());
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
                    int editedNumber = s.toString().equals("") ? 0 : Integer.parseInt(s.toString());
                    editedAmountArray.put(salaries.get(getLayoutPosition()).getSalaryId(), editedNumber);
                }
            });

            paidCheckBox.setOnClickListener(v -> {
                checkedArray.put(salaries.get(getLayoutPosition()).getSalaryId(), paidCheckBox.isChecked());
            });

//            paidCheckBox.setOnClickListener(v -> {
//                if (paidCheckBox.isChecked()) {
//                    salaryEditText.addTextChangedListener(textWatcher);
//                    String text = salaryEditText.getText().toString();
//                    listener.onCalculateSalaryItemCheckboxTouched(text.equals("") ? 0 : Integer.parseInt(text));
//                } else {
//                    salaryEditText.removeTextChangedListener(textWatcher);
//                    listener.onCalculateSalaryItemCheckboxTouched(-1 * Integer.parseInt(salaryEditText.getText().toString()));
//                }
//            });

//            Log.d("dbg", "renderedCount++: "+renderedCount);
//            renderedCount++;
//            if(renderedCount == getItemCount()) {
//                listener.dataSetChanged();
//            }
        }

//        TextWatcher
//                textWatcher = new TextWatcher() {
//            int beforeAmount = 0;
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                if(paidCheckBox.isChecked()) {
//                    beforeAmount = s.toString().equals("") ? 0 : Integer.parseInt(s.toString());
//                }
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(paidCheckBox.isChecked()) {
//                    int afterAmount = s.toString().equals("") ? 0 : Integer.parseInt(s.toString());
//                    int increasedAmount = afterAmount - beforeAmount;
////                Log.d("dbg", "before amount =  " + beforeAmount);
////                Log.d("dbg", "after amount =  " + afterAmount);
////                Log.d("dbg", "increased amount =  " + increasedAmount);
//                    listener.onCalculateSalaryItemSelectedAmountChanged(increasedAmount);
//                }
//            }
//        };

    }
}