package com.nqm.event_manager.utils;

import java.text.SimpleDateFormat;

public class CalendarUtil {
    public static SimpleDateFormat sdfDayMonthYear = new SimpleDateFormat("dd/MM/yyyy");
    ;
    public static SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat("EEE");
    public static SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
    public static SimpleDateFormat sdfDayMonth = new SimpleDateFormat("dd/MM");
    public static SimpleDateFormat sdfMonthYear1 = new SimpleDateFormat("MM - yyyy");
    public static SimpleDateFormat sdfMonthYear2 = new SimpleDateFormat("MM/yyyy");
    public static SimpleDateFormat sdfDay = new SimpleDateFormat("d");
    public static SimpleDateFormat sdfMonth= new SimpleDateFormat("MM");
    public static SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public SimpleDateFormat getSdfDayMonthYear() {
        return sdfDayMonthYear;
    }

    public SimpleDateFormat getSdfDayOfWeek() {
        return sdfDayOfWeek;
    }

    public SimpleDateFormat getSdfTime() {
        return sdfTime;
    }
}
