package com.nqm.event_manager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.AddEventActivity;
import com.nqm.event_manager.activities.SearchEventActivity;
import com.nqm.event_manager.activities.ViewEventActivity;
import com.nqm.event_manager.adapters.EventListAdapter;
import com.nqm.event_manager.custom_views.CustomCalendar;
import com.nqm.event_manager.interfaces.IOnCustomCalendarItemClicked;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;
import com.nqm.event_manager.utils.DatabaseAccess;

import java.util.Calendar;
import java.util.Date;

public class ManageEventFragment extends Fragment implements IOnDataLoadComplete,
        IOnCustomCalendarItemClicked {

    public static IOnDataLoadComplete thisListener;

    ListView eventsListView;
    TextView dayTitleTextView;
    CustomCalendar calendarView;
    EventListAdapter mainViewEventAdapter;
    Date selectedDate;

    String listTitle = "";

    public ManageEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        thisListener = this;
//        DatabaseAccess.setDatabaseListener(this);

        connectViews(view);

        selectedDate = calendarView.getSelectedDate();
        listTitle = String.format(getResources().getString(R.string.event_fragment_list_title),
                CalendarUtil.sdfDayMonthYear.format(selectedDate));
        dayTitleTextView.setText(listTitle);
        mainViewEventAdapter = new EventListAdapter(getActivity(), selectedDate);
        eventsListView.setAdapter(mainViewEventAdapter);

        addEvents();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manage_event_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_event) {
            Intent intent = new Intent(getActivity(), AddEventActivity.class);
            intent.putExtra(Constants.INTENT_SELECTED_DATE, CalendarUtil.sdfDayMonthYear.format(selectedDate));
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_today) {
            calendarView.setViewDate(Calendar.getInstance().getTime());
            calendarView.updateView();
        }

        if (id == R.id.action_selected_day) {
            calendarView.setViewDate(selectedDate);
            calendarView.updateView();
        }

        if (id == R.id.action_search) {
            Intent intent = new Intent(getActivity(), SearchEventActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectViews(View v) {
        eventsListView = v.findViewById(R.id.events_list_view);
//        calendarView = v.findViewById(R.id.calendar_view);
        calendarView = v.findViewById(R.id.event_fragment_calendar_view);
        dayTitleTextView = v.findViewById(R.id.day_title_text_view);
    }

    private void addEvents() {
        calendarView.setListener(this);

        //Xem chi tiết sự kiện
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent viewEventIntent = new Intent(getActivity(), ViewEventActivity.class);
                viewEventIntent.putExtra(Constants.INTENT_EVENT_ID, mainViewEventAdapter.getEventIds().get(position));
                startActivity(viewEventIntent);
            }
        });
    }


    @Override
    public void notifyOnLoadComplete() {
        Date date = calendarView.getSelectedDate();
        mainViewEventAdapter.notifyDataSetChanged(date);
        calendarView.updateView();
    }

    @Override
    public void onResume() {
        DatabaseAccess.setDatabaseListener(this, getContext());

        mainViewEventAdapter.notifyDataSetChanged(selectedDate);
        calendarView.updateView();

        super.onResume();
    }

    @Override
    public void onCustomCalendarCellClicked(Date selectedDate) {
        this.selectedDate = selectedDate;
        listTitle = String.format(getResources().getString(R.string.event_fragment_list_title),
                CalendarUtil.sdfDayMonthYear.format(selectedDate));
        dayTitleTextView.setText(listTitle);
        mainViewEventAdapter.notifyDataSetChanged(selectedDate);
    }
}
