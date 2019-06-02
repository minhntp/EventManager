package com.nqm.event_manager.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nqm.event_manager.R;
import com.nqm.event_manager.interfaces.IOnSearchEventItemClicked;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.repositories.EventRepository;

import java.util.ArrayList;

public class SearchEventListAdapter extends RecyclerView.Adapter<SearchEventListAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView timeTextView;
        public TextView titleTextView;
        public TextView locationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            timeTextView = itemView.findViewById(R.id.search_event_item_time_text_view);
            titleTextView = itemView.findViewById(R.id.search_event_item_title_text_view);
            locationTextView = itemView.findViewById(R.id.search_event_item_location_text_view);

            View.OnClickListener itemClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEventItemClicked(resultEventsIds.get(position));
                        }
                    }
                }
            };

            timeTextView.setOnClickListener(itemClickListener);
            titleTextView.setOnClickListener(itemClickListener);
            locationTextView.setOnClickListener(itemClickListener);
        }
    }

    private IOnSearchEventItemClicked listener;
    private ArrayList<String> resultEventsIds;

    public SearchEventListAdapter(ArrayList<String> resultEventsIds) {
        this.resultEventsIds = resultEventsIds;
    }

    public void setListener(IOnSearchEventItemClicked listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View eventView = inflater.inflate(R.layout.list_item_search_event, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(eventView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Event e = EventRepository.getInstance().getEventByEventId(resultEventsIds.get(i));

        TextView timeTextView = viewHolder.timeTextView;
        TextView titleTextView = viewHolder.titleTextView;
        TextView locationTextView = viewHolder.locationTextView;

        String time = e.getNgayBatDau() + "  " + e.getGioBatDau() + "\n" +
                e.getNgayKetThuc() + "  " + e.getGioKetThuc();
        timeTextView.setText(time);
        titleTextView.setText(e.getTen());
        locationTextView.setText(e.getDiaDiem());
    }

    @Override
    public int getItemCount() {
        return resultEventsIds.size();
    }
}
