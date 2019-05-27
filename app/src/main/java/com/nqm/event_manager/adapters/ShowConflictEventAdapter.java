package com.nqm.event_manager.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.repositories.EventRepository;

import java.util.ArrayList;

public class ShowConflictEventAdapter extends RecyclerView.Adapter<ShowConflictEventAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView timeTextView;
        public TextView titleTextView;
        public TextView locationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.conflict_event_list_item_time_text_view);
            titleTextView = itemView.findViewById(R.id.conflict_event_list_item_title_text_view);
            locationTextView = itemView.findViewById(R.id.conflict_event_list_item_location_text_view);
        }
    }

    private ArrayList<String> conflictEventsIds;

    public ShowConflictEventAdapter(ArrayList<String> conflictEventsIds) {
        this.conflictEventsIds = conflictEventsIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View eventView = inflater.inflate(R.layout.list_item_conflict_event, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(eventView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Event event = EventRepository.getInstance().getAllEvents().get(conflictEventsIds.get(i));

        TextView timeTextView = viewHolder.timeTextView;
        TextView titleTextView = viewHolder.titleTextView;
        TextView locationTextView = viewHolder.locationTextView;

        String time = event.getNgayBatDau() + " - " + event.getGioBatDau() + "\n" +
                event.getNgayKetThuc() + " - " + event.getGioKetThuc();
        timeTextView.setText(time);
        titleTextView.setText(event.getTen());
        locationTextView.setText(event.getDiaDiem());
    }

    @Override
    public int getItemCount() {
        return conflictEventsIds.size();
    }
}
