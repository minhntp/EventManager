package com.nqm.event_manager.utils;

import com.nqm.event_manager.models.Salary;
import com.nqm.event_manager.repositories.EventRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class SalaryUtil {

    public static void sortSalariesByEventStartDate(ArrayList<Salary> salaries) {
        Collections.sort(salaries, new Comparator<Salary>() {
            @Override
            public int compare(Salary s1, Salary s2) {
                Date d1 = Calendar.getInstance().getTime();
                Date d2 = Calendar.getInstance().getTime();
                try {
                    d1 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getAllEvents().get(s1.getEventId()).getNgayBatDau());
                    d2 = CalendarUtil.sdfDayMonthYear.parse(EventRepository.getInstance(null).getAllEvents().get(s2.getEventId()).getNgayBatDau());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return d1.compareTo(d2);
            }
        });
    }

}
