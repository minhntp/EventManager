package com.nqm.event_manager.interfaces;

public interface IOnAddScheduleViewClicked {
    void onTimeEditTextSet(int position, String timeText);

    void onAddScheduleItemMoved();

    void onAddScheduleItemRemoved();
}
