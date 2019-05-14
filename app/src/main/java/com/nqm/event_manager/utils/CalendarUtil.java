package com.nqm.event_manager.utils;

import java.text.SimpleDateFormat;

public class CalendarUtil {
    public static SimpleDateFormat sdfDayMonthYear = new SimpleDateFormat("dd/MM/yyyy");
    public static SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat("EEE");
    public static SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a");
    public static SimpleDateFormat sdfDayMonth = new SimpleDateFormat("dd/MM");
    public static SimpleDateFormat sdfMonthYear1 = new SimpleDateFormat("MM - yyyy");
    public static SimpleDateFormat sdfMonthYear2 = new SimpleDateFormat("MM/yyyy");
    public static SimpleDateFormat sdfDay = new SimpleDateFormat("d");
    public static SimpleDateFormat sdfMonth= new SimpleDateFormat("MM");
    public static SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

    public static String dayOfWeekInVietnamese(String dayMonthYear) {
        try {
            String dayOfWeekEng = sdfDayOfWeek.format(sdfDayMonthYear.parse(dayMonthYear));
            switch (dayOfWeekEng) {
                case "Mon":
                    return "T2";
                case "Tue":
                    return "T3";
                case "Wed":
                    return "T4";
                case "Thu":
                    return "T5";
                case "Fri":
                    return "T6";
                case "Sat":
                    return "T7";
                case "Sun":
                    return "CN";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
