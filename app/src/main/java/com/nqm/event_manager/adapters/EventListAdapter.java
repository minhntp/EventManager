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
    private HashMap<String, Event> events;
    private ArrayList<String> eventIds;

    public EventListAdapter(Activity context, Date date) {
        this.context = context;
        this.date = date;
        events = EventRepository.getInstance(null).getEventsThroughDate(CalendarUtil.sdfDayMonthYear.format(date));
        eventIds = new ArrayList<>(events.keySet());
        sortEvents();
    }

    private void sortEvents() {
        //sort by start date
        Collections.sort(eventIds, new Comparator<String>() {
            @Override
            public int compare(String id1, String id2) {
                int compareResult = 0;
                Event e1 = EventRepository.getInstance(null).getEventByEventId(id1);
                Event e2 = EventRepository.getInstance(null).getEventByEventId(id2);
                try {
                    if (!e1.getNgayBatDau().equals(e2.getNgayBatDau())) {
                        compareResult = CalendarUtil.sdfDayMonthYear.parse(e1.getNgayBatDau()).compareTo(
                                CalendarUtil.sdfDayMonthYear.parse(e2.getNgayBatDau()));
                    } else {
                        compareResult = CalendarUtil.sdfTime.parse(e1.getGioBatDau()).compareTo(
                                CalendarUtil.sdfTime.parse(e2.getGioKetThuc()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return compareResult;
            }
        });
    }

    public ArrayList<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(ArrayList<String> eventIds) {
        this.eventIds = eventIds;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Event getItem(int i) {
        return events.get(eventIds.get(i));
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_event_list_item, parent, false);
        }

        TextView timeTextView = view.findViewById(R.id.event_time_text_view);
        TextView titleTextView = view.findViewById(R.id.event_title_text_view);
        TextView locationTextView = view.findViewById(R.id.event_location_text_view);

        Event thisEvent = events.get(eventIds.get(position));

        String start = thisEvent.getGioBatDau();
        String end = thisEvent.getGioKetThuc();
        try {
            if (!thisEvent.getNgayBatDau().equals(thisEvent.getNgayKetThuc())) {
                if (CalendarUtil.sdfDayMonthYear.parse(CalendarUtil.sdfDayMonthYear.format(date)).compareTo(
                        CalendarUtil.sdfDayMonthYear.parse(thisEvent.getNgayBatDau())) > 0) {
                    start = CalendarUtil.sdfDayMonth.format(CalendarUtil.sdfDayMonthYear.parse(thisEvent.getNgayBatDau()));
                }
                if (CalendarUtil.sdfDayMonthYear.parse(CalendarUtil.sdfDayMonthYear.format(date)).compareTo(
                        CalendarUtil.sdfDayMonthYear.parse(thisEvent.getNgayKetThuc())) < 0) {
                    end = CalendarUtil.sdfDayMonth.format(CalendarUtil.sdfDayMonthYear.parse(thisEvent.getNgayKetThuc()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String time = start + "\n" + end;
        timeTextView.setText(time);
        titleTextView.setText(events.get(eventIds.get(position)).getTen());
        locationTextView.setText(events.get(eventIds.get(position)).getDiaDiem());

        return view;
    }

    public void notifyDataSetChanged(Date date) {
        this.date = date;
        events = EventRepository.getInstance(null).getEventsThroughDate(CalendarUtil.sdfDayMonthYear.format(date));
        eventIds = new ArrayList<>(events.keySet());
        sortEvents();
        super.notifyDataSetChanged();
    }
}
