package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.application.EventManager;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.repositories.EventRepository;

import java.util.ArrayList;

public class ShowConflictEventAdapter extends BaseAdapter {

    private Activity context;
    private ArrayList<Event> events;

    public ShowConflictEventAdapter(Activity context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
        EventRepository.getInstance().sortEventsByStartDateTime(this.events);
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Event getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_conflict_event, parent, false);
        }

        TextView timeTextView = view.findViewById(R.id.conflit_event_list_item_time_text_view);
        TextView titleTextView = view.findViewById(R.id.conflit_event_list_item_title_text_view);
        TextView locationTextView = view.findViewById(R.id.conflit_event_list_item_location_text_view);

        String time = getItem(position).getNgayBatDau() + " - " + getItem(position).getGioBatDau() + "\n" +
                getItem(position).getNgayKetThuc() + " - " + getItem(position).getGioKetThuc();
        timeTextView.setText(time);
        titleTextView.setText(getItem(position).getTen());
        locationTextView.setText(getItem(position).getDiaDiem());

        return view;
    }

    public void notifyDataSetChanged(ArrayList<Event> events) {
        this.events = events;
        EventRepository.getInstance().sortEventsByStartDateTime(events);
        super.notifyDataSetChanged();
    }
}
