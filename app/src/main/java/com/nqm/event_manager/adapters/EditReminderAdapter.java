package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnEditReminderItemClicked;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.repositories.ReminderRepository;

import java.util.ArrayList;

public class EditReminderAdapter extends BaseAdapter {

    private Activity context;
    private ArrayList<Reminder> selectedReminders;
    IOnEditReminderItemClicked listener;

    public EditReminderAdapter(Activity context, ArrayList<Reminder> selectedReminders) {
        this.context = context;
        this.selectedReminders = selectedReminders;
        ReminderRepository.sortReminder(selectedReminders);
    }

    public void setListener(IOnEditReminderItemClicked listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return selectedReminders.size();
    }

    @Override
    public Reminder getItem(int position) {
        return selectedReminders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_edit_reminder, parent, false);
        }

        TextView timeTextView = view.findViewById(R.id.edit_reminder_item_time_text_view);
        ImageButton clearButton = view.findViewById(R.id.edit_reminder_item_delete_image_button);

        timeTextView.setText(ReminderRepository.defaultRemindersMap.get(getItem(position).getMinute()));

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReminderClearButtonClicked(getItem(position).getMinute());
            }
        });

        return view;
    }

    public void notifyDataSetChanged() {
        ReminderRepository.sortReminder(selectedReminders);
        super.notifyDataSetChanged();
        listener.remindersChanged();
    }
}
