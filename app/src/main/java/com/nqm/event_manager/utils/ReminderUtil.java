package com.nqm.event_manager.utils;

import com.nqm.event_manager.models.Reminder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ReminderUtil {
    public static void sortReminder(ArrayList<Reminder> reminders) {
        Collections.sort(reminders, new Comparator<Reminder>() {
            @Override
            public int compare(Reminder r1, Reminder r2) {
                return r1.getMinute() - r2.getMinute();
            }
        });
    }
}
