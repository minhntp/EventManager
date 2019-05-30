package com.nqm.event_manager.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.CalendarUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class EventListAdapter extends BaseAdapter {

    private final Activity context;
    Date date;
    private ArrayList<Event> events;

    public EventListAdapter(Activity context, Date date) {
        this.context = context;
        this.date = date;
        events = EventRepository.getInstance().getEventsArrayListThroughDate(CalendarUtil.sdfDayMonthYear.format(date));
        EventRepository.getInstance().sortEventsByStartDateTime(events);
    }

    public ArrayList<String> getEventIds() {
        ArrayList<String> eventsIds = new ArrayList<>();
        for (Event e : events) {
            eventsIds.add(e.getId());
        }
        return  eventsIds;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Event getItem(int i) {
        return events.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_event, parent, false);
        }

        TextView timeTextView = view.findViewById(R.id.event_time_text_view);
        TextView titleTextView = view.findViewById(R.id.event_title_text_view);
        TextView locationTextView = view.findViewById(R.id.event_location_text_view);

        String startTime = getItem(position).getGioBatDau();
        String endTime = getItem(position).getGioKetThuc();
        try {
            if (!getItem(position).getNgayBatDau().equals(getItem(position).getNgayKetThuc())) {
                if (CalendarUtil.sdfDayMonthYear.parse(CalendarUtil.sdfDayMonthYear.format(date)).compareTo(
                        CalendarUtil.sdfDayMonthYear.parse(getItem(position).getNgayBatDau())) > 0) {
                    startTime = CalendarUtil.sdfDayMonth.format(CalendarUtil.sdfDayMonthYear
                            .parse(getItem(position).getNgayBatDau()));
                }
                if (CalendarUtil.sdfDayMonthYear.parse(CalendarUtil.sdfDayMonthYear.format(date)).compareTo(
                        CalendarUtil.sdfDayMonthYear.parse(getItem(position).getNgayKetThuc())) < 0) {
                    endTime = CalendarUtil.sdfDayMonth.format(CalendarUtil.sdfDayMonthYear
                            .parse(getItem(position).getNgayKetThuc()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String time = startTime + "\n" + endTime;
        timeTextView.setText(time);
        titleTextView.setText(getItem(position).getTen());
        locationTextView.setText(getItem(position).getDiaDiem());

        return view;
    }

    public void notifyDataSetChanged(Date date) {
        this.date = date;
        events = EventRepository.getInstance().getEventsArrayListThroughDate(CalendarUtil.sdfDayMonthYear.format(date));
        EventRepository.getInstance().sortEventsByStartDateTime(events);
        super.notifyDataSetChanged();
    }
}
