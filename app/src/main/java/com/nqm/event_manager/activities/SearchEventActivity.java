package com.nqm.event_manager.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.SearchEventListAdapter;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnSearchEventViewClicked;
import com.nqm.event_manager.models.Employee;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.Constants;

import java.util.ArrayList;

public class SearchEventActivity extends AppCompatActivity implements IOnSearchEventViewClicked,
        IOnDataLoadComplete {

    Activity context;
    Toolbar toolbar;

    RecyclerView eventRecyclerView;
    SearchEventListAdapter eventAdapter;
    SearchView searchView;

    ArrayList<String> resultEventsIds;

    String searchString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event);

        connectViews();
        init();
        addEvents();
    }

    private void connectViews() {
        searchView = findViewById(R.id.search_event_search_view);
        eventRecyclerView = findViewById(R.id.search_event_recycler_view);
    }

    private void init() {
        toolbar = findViewById(R.id.search_event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.search_event_activity_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        EventRepository.getInstance().setListener(this);

        resultEventsIds = new ArrayList<>();
        eventAdapter = new SearchEventListAdapter(resultEventsIds);
        eventAdapter.setListener(this);
        eventRecyclerView.setAdapter(eventAdapter);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchString = newText;
                resultEventsIds.clear();
                resultEventsIds.addAll(EventRepository.getInstance().getEventsIdsBySearchString(searchString));
                eventAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void addEvents() {

    }

    @Override
    public void onEventItemClicked(String eventId) {
        Intent viewEventIntent = new Intent(this, ViewEventActivity.class);
        viewEventIntent.putExtra(Constants.INTENT_EVENT_ID, eventId);
        startActivity(viewEventIntent);
    }

    @Override
    protected void onResume() {
        EventRepository.getInstance().setListener(this);
        resultEventsIds.clear();
        resultEventsIds.addAll(EventRepository.getInstance().getEventsIdsBySearchString(searchString));
        eventAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void notifyOnLoadComplete() {
        resultEventsIds.clear();
        resultEventsIds.addAll(EventRepository.getInstance().getEventsIdsBySearchString(searchString));
        eventAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
