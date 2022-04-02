package com.nqm.event_manager.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.nqm.event_manager.R;
import com.nqm.event_manager.adapters.SearchEventListAdapter;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.interfaces.IOnSearchEventItemClicked;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.utils.Constants;

import java.util.ArrayList;

public class SearchEventActivity extends BaseActivity implements IOnSearchEventItemClicked,
        IOnDataLoadComplete {


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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.search_event_activity_label);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        EventRepository.getInstance().addListener(this);

        resultEventsIds = new ArrayList<>();
        eventAdapter = new SearchEventListAdapter(resultEventsIds);
        eventAdapter.setListener(this);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventRecyclerView.setAdapter(eventAdapter);
        eventRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

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
        EventRepository.getInstance().addListener(this);
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
