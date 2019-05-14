package com.nqm.event_manager.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.tibolte.agendacalendarview.AgendaCalendarView;
import com.github.tibolte.agendacalendarview.CalendarPickerController;
import com.github.tibolte.agendacalendarview.models.CalendarEvent;
import com.github.tibolte.agendacalendarview.models.DayItem;
import com.nqm.event_manager.R;
import com.nqm.event_manager.activities.AddEventActivity;
import com.nqm.event_manager.activities.RootActivity;
import com.nqm.event_manager.activities.ViewEventActivity;
import com.nqm.event_manager.interfaces.IOnDataLoadComplete;
import com.nqm.event_manager.models.DrawableCalendarEvent;
import com.nqm.event_manager.models.Event;
import com.nqm.event_manager.repositories.EventRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventListFragment extends Fragment implements IOnDataLoadComplete {

    private static final int RESULT_FROM_DELETE_EVENT_INTENT = 1;
    private static final int RESULT_FROM_ADD_EVENT_INTENT = 2;
    AgendaCalendarView agendaCalendarView;

    public EventListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectViews(view);
        EventRepository.getInstance(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Mở cửa sổ thêm sự kiện
        if (id == R.id.action_add_event) {
            Intent intent = new Intent(getActivity(), AddEventActivity.class);
            startActivity(intent);
            //  startActivityForResult(intent, RESULT_FROM_ADD_EVENT_INTENT);
            return true;
        }
        //Xem sự kiện theo danh sách dọc
        if (id == R.id.action_list_view) {
            RootActivity activity = (RootActivity) getActivity();
            activity.openManageEventFragment();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void initCalendarData(List<CalendarEvent> eventList) {
        for (String e : EventRepository.getInstance(null).getAllEvents().keySet()) {
            try {
                Event event = EventRepository.getInstance(null).getAllEvents().get(e);
                String day1[] = event.getNgayBatDau().split("/");
                String day2[] = event.getNgayKetThuc().split("/");
                Calendar startTime1 = Calendar.getInstance();
                startTime1.set(Integer.parseInt(day1[2]), Integer.parseInt(day1[1]), Integer.parseInt(day1[0]));
                Calendar endTime1 = Calendar.getInstance();
                endTime1.set(Integer.parseInt(day2[2]), Integer.parseInt(day2[1]), Integer.parseInt(day2[0]));
                DrawableCalendarEvent event1 = new DrawableCalendarEvent(event.getTen(), event.getGhiChu(), event.getDiaDiem(),
                        ContextCompat.getColor(getContext(), R.color.blue_selected), startTime1, endTime1, false, e);
                eventList.add(event1);
            } catch (Exception ex) {
                Log.d("EventList parse", ex.getMessage());
            }


        }
        ;
    }

    private void intCalendar() {
        Calendar minDate = Calendar.getInstance();
        Calendar maxDate = Calendar.getInstance();
        minDate.add(Calendar.MONTH, -2);
        minDate.set(Calendar.DAY_OF_MONTH, 1);
        maxDate.add(Calendar.YEAR, 1);
        List<CalendarEvent> eventList = new ArrayList<>();
        initCalendarData(eventList);

        agendaCalendarView.init(eventList, minDate, maxDate, Locale.getDefault(), new CalendarPickerController() {
            @Override
            public void onDaySelected(DayItem dayItem) {

            }

            @Override
            public void onEventSelected(CalendarEvent event) {
                Intent eventDetailsIntent = new Intent(getContext(), ViewEventActivity.class);
                eventDetailsIntent.putExtra("eventId", ((DrawableCalendarEvent) event).getEventID());
                getActivity().startActivityForResult(eventDetailsIntent, RESULT_FROM_DELETE_EVENT_INTENT);
            }

            @Override
            public void onScrollToDate(Calendar calendar) {

            }
        });
    }

    private void connectViews(View v) {
        agendaCalendarView = v.findViewById(R.id.agenda_calendar_view);
        intCalendar();
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
                            intCalendar();
                        } else {
                            Toast.makeText(getActivity(), "Xóa sự kiện thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else if (requestCode == RESULT_FROM_ADD_EVENT_INTENT && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra("added?", false)) {
                intCalendar();
            }
        }
    }

    private void addEvents() {
        /*
        //Cập nhật danh sách sự kiện theo ngày đã chọn
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                CalendarUtil.getInstance().getCalendar().set(Calendar.YEAR, year);
                CalendarUtil.getInstance().getCalendar().set(Calendar.MONTH, month);
                CalendarUtil.getInstance().getCalendar().set(Calendar.DAY_OF_MONTH, dayOfMonth);

                currentDate = CalendarUtil.getInstance().getSdfDayMonthYear()
                        .format(CalendarUtil.getInstance().getCalendar().getTime());

                dayTitleTextView.setText(Constants.DAY_TITLE_MAIN_FRAGMENT + currentDate);
                mainViewEventAdapter.notifyDataSetChanged(currentDate);
            }
        });

        //Xem chi tiết sự kiện
        eventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent eventDetailsIntent = new Intent(getActivity(), EventDetailsActivity.class);
                eventDetailsIntent.putExtra("eventId", mainViewEventAdapter.getEventIds()[position]);
                startActivityForResult(eventDetailsIntent, RESULT_FROM_DELETE_EVENT_INTENT);
            }
        });
        */
    }

    //Cập nhật danh sách sự kiện của ngày hiện tại khi mở ứng dụng
    @Override
    public void notifyOnLoadComplete() {
    }
}
