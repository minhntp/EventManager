package com.nqm.event_manager.interfaces;

public interface IOnCalculateSalaryItemClicked {
    void onCalculateSalaryItemClicked(String eventId);
    void onCalculateSalaryItemChecked(int amount);
    void onCalculateSalaryItemSelectedAmountChanged(int changedAmount);
    void renderedAllElements();
}
