package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nqm.event_manager.R;

public class SendEventSectionAdapter extends BaseAdapter {
    Activity context;
    String[] sectionsTitles;

    public SendEventSectionAdapter(Activity context, String[] sectionsTitles) {
        this.context = context;
        this.sectionsTitles = sectionsTitles;
    }

    @Override
    public int getCount() {
        return sectionsTitles.length;
    }

    @Override
    public String getItem(int position) {
        return sectionsTitles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_send_event, parent, false);
        }

        //Connect views
        TextView nameTextView = view.findViewById(R.id.send_event_item_name_text_view);
        CheckBox selectCheckBox = view.findViewById(R.id.send_event_select_item_checkbox);

        //Fill info
        nameTextView.setText(getItem(position));
        selectCheckBox.setChecked(true);

        return view;
    }
}
