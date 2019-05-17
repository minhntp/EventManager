package com.nqm.event_manager.interfaces;

public interface IOnCustomViewClicked {
    void onDeleteButtonClicked(int position);
    void onTimeEditTextSet(int position, String timeText);
    void onEmployeeListItemClicked(String employeeId);
    void onAddScheduleItemMoved();
    void onAddScheduleItemRemoved();
}
