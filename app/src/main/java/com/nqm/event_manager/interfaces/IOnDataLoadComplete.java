package com.nqm.event_manager.interfaces;

import android.content.Context;

public interface IOnDataLoadComplete {
    void notifyOnLoadCompleteWithContext(Context context);
    void notifyOnLoadComplete();
}

