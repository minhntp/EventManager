package com.nqm.event_manager.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnConflictEventItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.repositories.EventRepository;

import java.util.ArrayList;

public class ShowConflictEventAdapter extends RecyclerView.Adapter<ShowConflictEventAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView titleTextView;
        TextView locationTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.conflict_event_list_item_time_text_view);
            titleTextView = itemView.findViewById(R.id.conflict_event_list_item_title_text_view);
            locationTextView = itemView.findViewById(R.id.conflict_event_list_item_location_text_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onConflictEventItemClicked(conflictEventsIds.get(getAdapterPosition()));
                }
            });
        }
    }

    private ArrayList<String> conflictEventsIds;
    private IOnConflictEventItemClicked listener;

    public ShowConflictEventAdapter(ArrayList<String> conflictEventsIds) {
        this.conflictEventsIds = conflictEventsIds;
    }

    public void setListener(IOnConflictEventItemClicked listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View eventView = inflater.inflate(R.layout.list_item_conflict_event, viewGroup, false);
        return new ViewHolder(eventView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Event event = EventRepository.getInstance().getAllEvents().get(conflictEventsIds.get(i));

        if (event != null) {
            String time = event.getNgayBatDau() + " - " + event.getGioBatDau() + "\n" +
                    event.getNgayKetThuc() + " - " + event.getGioKetThuc();
            viewHolder.timeTextView.setText(time);
            viewHolder.titleTextView.setText(event.getTen());
            viewHolder.locationTextView.setText(event.getDiaDiem());
        }
    }

    @Override
    public int getItemCount() {
        return conflictEventsIds.size();
    }
}
