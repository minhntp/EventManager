package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnSelectReminderViewClicked;
import com.nqm.event_manager.models.Reminder;
import com.nqm.event_manager.repositories.ReminderRepository;

import java.util.ArrayList;

public class SelectReminderAdapter extends BaseAdapter {

    private Activity context;
    private ArrayList<Reminder> selectedReminders;
    private ArrayList<Integer> selectedRemindersMinutes;
    IOnSelectReminderViewClicked listener;

    public SelectReminderAdapter(Activity context, ArrayList<Reminder> selectedReminders) {
        this.context = context;
        this.selectedReminders = selectedReminders;
        selectedRemindersMinutes = new ArrayList<>();
        for (Reminder r : selectedReminders) {
            selectedRemindersMinutes.add(r.getMinute());
        }
    }


    public void setListener(IOnSelectReminderViewClicked listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return ReminderRepository.defaultReminders.size();
    }

    @Override
    public Reminder getItem(int position) {
        return ReminderRepository.defaultReminders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_select_reminder, parent, false);
        }

        TextView timeTextView = view.findViewById(R.id.select_reminder_item_time_text_view);
        final CheckBox selectCheckBox = view.findViewById(R.id.select_reminder_item_select_check_box);

        timeTextView.setText(ReminderRepository.defaultRemindersMap.get(getItem(position).getMinute()));

        if (selectedRemindersMinutes.contains(getItem(position).getMinute())) {
            selectCheckBox.setChecked(true);
        } else {
            selectCheckBox.setChecked(false);
        }

        selectCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSelectReminderCheckBoxClicked(getItem(position).getMinute(), selectCheckBox.isChecked());
            }
        });

        return view;
    }

    public void notifyDataSetChanged() {
        selectedRemindersMinutes.clear();
        for (Reminder r : selectedReminders) {
            selectedRemindersMinutes.add(r.getMinute());
        }
        Log.d("debug", "selectedRemindersMinute size = " + selectedRemindersMinutes.size());
        super.notifyDataSetChanged();
    }
}
