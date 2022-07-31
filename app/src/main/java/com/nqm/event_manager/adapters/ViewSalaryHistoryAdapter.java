package com.nqm.event_manager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.History;
import com.nqm.event_manager.repositories.HistoryRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;

import java.util.List;

public class ViewSalaryHistoryAdapter extends
        RecyclerView.Adapter<ViewSalaryHistoryAdapter.ViewHolder>
        implements IOnDataLoadComplete {

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayOfWeekTextView, dateTextView, eventNameTextView, eventLocationTextView,
                employeeNameTextView, employeeSpecialityTextView, amountTextView, isPaidTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dayOfWeekTextView = itemView.findViewById(R.id.salary_history_list_item_edit_date_text_view);
            dateTextView = itemView.findViewById(R.id.salary_history_list_item_date_text_view);
            eventNameTextView = itemView.findViewById(R.id.salary_history_event_name_text_view);
            eventLocationTextView = itemView.findViewById(R.id.salary_history_event_location_text_view);
            employeeNameTextView = itemView.findViewById(R.id.salary_history_employee_name_text_view);
            employeeSpecialityTextView = itemView.findViewById(R.id.salary_history_employee_speciality_text_view);
            amountTextView = itemView.findViewById(R.id.salary_history_amount_text_view);
            isPaidTextView = itemView.findViewById(R.id.salary_history_is_paid_text_view);
        }
    }

    private List<History> histories;

    public ViewSalaryHistoryAdapter() {
        HistoryRepository.getInstance().setListener(this);
        this.histories = HistoryRepository.getInstance().getAllHistoriesSortedByDateTime();
    }

    public boolean isHistoryEmpty() {
        return (histories == null) || histories.isEmpty();
    }

    @NonNull
    @Override
    public ViewSalaryHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View employeeView = inflater.inflate(R.layout.list_item_view_salary_history, viewGroup, false);

        return new ViewHolder(employeeView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        History history = histories.get(i);
        viewHolder.dayOfWeekTextView.setText(CalendarUtil.getFormattedDate(history.getEditedDateTimeInMillis()));
        viewHolder.dateTextView.setText(CalendarUtil.getFormattedDate(history.getDateTimeInMillis()));
        viewHolder.eventNameTextView.setText(history.getEventName());
        viewHolder.eventLocationTextView.setText(history.getEventLocation());
        viewHolder.employeeNameTextView.setText(history.getEmployeeName());
        viewHolder.employeeSpecialityTextView.setText(history.getEmployeeSpeciality());

        HtmlText htmlText = getTransitionHtmlText(history, true);
        viewHolder.amountTextView.setText(htmlText.htmlText);
        if (htmlText.isChanged) {
            viewHolder.amountTextView.setTextColor(Color.parseColor(Constants.SALARY_HISTORY_TEXT_COLOR));
        } else {
            viewHolder.amountTextView.setTextColor(Color.parseColor(Constants.DEFAULT_TEXT_COLOR));
        }

        htmlText = getTransitionHtmlText(history, false);
        viewHolder.isPaidTextView.setText(htmlText.htmlText);
        if (htmlText.isChanged) {
            viewHolder.isPaidTextView.setTextColor(Color.parseColor(Constants.SALARY_HISTORY_IS_PAID_TEXT_COLOR));
        } else {
            viewHolder.isPaidTextView.setTextColor(Color.parseColor(Constants.DEFAULT_TEXT_COLOR));
        }
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }

    private static class HtmlText {
        boolean isChanged;
        Spanned htmlText;

        public HtmlText(boolean isChanged, Spanned htmlText) {
            this.isChanged = isChanged;
            this.htmlText = htmlText;
        }
    }

    private HtmlText getTransitionHtmlText(History history, boolean isAmount) {
        // isAmount: true -> get text for amount; false -> get text for isPaid
        History.HistorySalary oldSalary = history.getOldSalary();
        History.HistorySalary newSalary = history.getNewSalary();

        String text = "";
        boolean changed = false;
        if (isAmount) {
            text += oldSalary.getAmount();
            if (oldSalary.getAmount() != newSalary.getAmount()) {
                text += Constants.RIGHT_ARROW + newSalary.getAmount();
                changed = true;
            }
        } else {
            String oppositeText;
            if (oldSalary.isPaid()) {
                text += Constants.IS_PAID;
                oppositeText = Constants.IS_NOT_PAID;
            } else {
                text += Constants.IS_NOT_PAID;
                oppositeText = Constants.IS_PAID;
            }
            if (oldSalary.isPaid() != newSalary.isPaid()) {
                text += Constants.RIGHT_ARROW + oppositeText;
                changed = true;
            }
        }
        return new HtmlText(changed, Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
    }

    @Override
    public void notifyOnLoadComplete() {
        this.histories = HistoryRepository.getInstance().getAllHistoriesSortedByDateTime();
        notifyDataSetChanged();
    }

}
