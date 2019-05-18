package com.nqm.event_manager.utils;

import com.nqm.event_manager.models.Schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScheduleUtil {
    static public void sortSchedulesByOrder(ArrayList<Schedule> schedules) {
        Collections.sort(schedules, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule schedule1, Schedule schedule2) {
                return schedule1.getOrder() - schedule2.getOrder();
            }
        });
    }

    public static void sortSchedulesByStartTime(ArrayList<Schedule> schedules) {
        Collections.sort(schedules, new Comparator<Schedule>() {
            @Override
            public int compare(Schedule schedule1, Schedule schedule2) {
                if (schedule1.getTime().isEmpty() && schedule2.getTime().isEmpty()) {
                    if (schedule1.getContent().isEmpty() && schedule2.getContent().isEmpty()) {
                        return 0;
                    } else if (schedule1.getContent().isEmpty() && !schedule2.getContent().isEmpty()) {
                        return 1;
                    } else if (!schedule1.getContent().isEmpty() && schedule2.getContent().isEmpty()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
                if (schedule1.getTime().isEmpty() && !schedule2.getTime().isEmpty()) {
                    return 1;
                }
                if (!schedule1.getTime().isEmpty() && schedule2.getTime().isEmpty()) {
                    return -1;
                }
                int compareResult = 0;
                try {
                    compareResult = CalendarUtil.sdfTime.parse(schedule1.getTime()).compareTo(
                            CalendarUtil.sdfTime.parse(schedule2.getTime()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return compareResult;
            }
        });
    }
}
