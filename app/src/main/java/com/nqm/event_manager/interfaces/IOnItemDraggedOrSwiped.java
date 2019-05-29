package com.nqm.event_manager.interfaces;

public interface IOnItemDraggedOrSwiped {
    void onViewDragged(int oldPosition, int newPosition);
    void onViewSwiped(int position);
}
