package com.nqm.event_manager.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.AddEventActivity;
import com.nqm.event_manager.activities.RootActivity;
import com.nqm.event_manager.activities.ViewEventActivity;
import com.nqm.event_manager.adapters.EventListAdapter;
import com.nqm.event_manager.custom_views.CustomCalendarView;
import com.nqm.event_manager.custom_views.CustomListView;
import com.nqm.event_manager.interfaces.IOnCustomCalendarGridItemClicked;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.repositories.EmployeeRepository;
import com.nqm.event_manager.repositories.EventRepository;
import com.nqm.event_manager.repositories.SalaryRepository;
import com.nqm.event_manager.repositories.ScheduleRepository;
import com.nqm.event_manager.utils.CalendarUtil;
import com.nqm.event_manager.utils.Constants;

import java.util.Calendar;
import java.util.Date;

public class ManageEventFragment extends Fragment implements IOnDataLoadComplete, IOnCustomCalendarGridItemClicked {

    private static final int RESULT_FROM_DELETE_EVENT_INTENT = 1;
    private static final int RESULT_FROM_ADD_EVENT_INTENT = 2;

    CustomListView eventsListView;
    TextView dayTitleTextView;
    //    CalendarView calendarView;
    CustomCalendarView calendarView;
    EventListAdapter mainViewEventAdapter;

    boolean isFirstLoad = true;

    Date selectedDate;

    public ManageEventFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectViews(view);
        addEvents();

        //Update eventList at EventRepository & employeeList at EmployeeRepository
        EventRepository.getInstance(this);
        EmployeeRepository.getInstance(this);
        SalaryRepository.getInstance(this);
        ScheduleRepository.getInstance(this);

//        selectedDate = CalendarUtil.sdfDayMonthYear.format(calendarView.getDate());
        selectedDate = calendarView.getSelectedDate();
        dayTitleTextView.setText(Constants.DAY_TITLE_MAIN_FRAGMENT + CalendarUtil.sdfDayMonthYear.format(selectedDate));

        mainViewEventAdapter = new EventListAdapter(getActivity(), selectedDate);
        eventsListView.setAdapter(mainViewEventAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
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

        //Mở cửa sổ thêm sự kiện
        if (id == R.id.action_add_e) {
            Intent intent = new Intent(getActivity(), AddEventActivity.class);
            intent.putExtra("selectedDate", CalendarUtil.sdfDayMonthYear.format(selectedDate));
            startActivityForResult(intent, RESULT_FROM_ADD_EVENT_INTENT);
            return true;
        }
        //Xem sự kiện theo danh sách dọc
        if (id == R.id.action_list_view) {
            RootActivity activity = (RootActivity) getActivity();
            activity.openEventListFragment();
            return true;
        }

        //Đi đến ngày hôm nay
        if (id == R.id.action_today) {
            calendarView.setViewDate(Calendar.getInstance().getTime());
            calendarView.updateView();
        }

        if (id == R.id.action_selected_day) {
            calendarView.setViewDate(selectedDate);
            calendarView.updateView();
        }

        return super.onOptionsItemSelected(item);
    }

    private void connectViews(View v) {
        eventsListView = v.findViewById(R.id.events_listview);
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
                Intent eventDetailsIntent = new Intent(getActivity(), ViewEventActivity.class);
                eventDetailsIntent.putExtra("eventId", mainViewEventAdapter.getEventIds().get(position));
                startActivityForResult(eventDetailsIntent, RESULT_FROM_DELETE_EVENT_INTENT);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_FROM_DELETE_EVENT_INTENT && resultCode == Activity.RESULT_OK) {
            Log.d("debug", "delete? from EventDetails to EventManagement: " + data.getBooleanExtra("delete?", false));
            if (data.getBooleanExtra("delete?", false)) {
                EventRepository.getInstance(null).deleteEventFromDatabase(data.getStringExtra("eventId"), new EventRepository.MyDeleteEventCallback() {
                    @Override
                    public void onCallback(boolean deleteEventSucceed) {
                        if (deleteEventSucceed) {
                            Toast.makeText(getActivity(), "Xóa sự kiện thành công", Toast.LENGTH_SHORT).show();
                            calendarView.updateView();
                            mainViewEventAdapter.notifyDataSetChanged(selectedDate);
                        } else {
                            Toast.makeText(getActivity(), "Xóa sự kiện thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else if (requestCode == RESULT_FROM_ADD_EVENT_INTENT && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra("added?", false)) {
                Toast.makeText(getContext(), "Thêm sự kiện thành công", Toast.LENGTH_SHORT).show();
                calendarView.updateView();
                mainViewEventAdapter.notifyDataSetChanged(selectedDate);
            }
        }
    }

    //Cập nhật danh sách sự kiện của ngày hiện tại khi mở ứng dụng
    @Override
    public void notifyOnLoadComplete() {
        if (isFirstLoad) {
//            String date = CalendarUtil.sdfDayMonthYear.format(calendarView.getDate());
            Date date = calendarView.getSelectedDate();
            mainViewEventAdapter.notifyDataSetChanged(date);
            calendarView.updateView();
            isFirstLoad = false;
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        mainViewEventAdapter.notifyDataSetChanged(selectedDate);
        calendarView.updateView();
    }

    @Override
    public void onGridItemClickedFromCalendarView(Date selectedDate) {
        this.selectedDate = selectedDate;
        dayTitleTextView.setText(Constants.DAY_TITLE_MAIN_FRAGMENT + CalendarUtil.sdfDayMonthYear.format(selectedDate));
        mainViewEventAdapter.notifyDataSetChanged(selectedDate);
    }

    @Override
    public void onGridItemClickedFromCalendarAdapter(Date selectedDate) {

    }
}