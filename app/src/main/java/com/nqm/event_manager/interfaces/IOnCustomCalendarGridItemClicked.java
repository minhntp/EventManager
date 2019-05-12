package com.nqm.event_manager.interfaces;

import java.util.Date;

public interface IOnCustomCalendarGridItemClicked {
    void onGridItemClickedFromCalendarView(Date selectedDate);
    void onGridItemClickedFromCalendarAdapter(Date selectedDate);
}
