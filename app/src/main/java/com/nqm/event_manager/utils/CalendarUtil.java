package com.nqm.event_manager.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CalendarUtil {

    private static String formatDayMonthYear = "dd/MM/yyyy";
    private static String formatDayMonthYearTime = "dd/MM/yyyy HH:mm:ss:ss";
    private static String formatDayOfWeek = "EEE";
    private static String formatTime = "hh:mm a";
    private static String formatDayMonth = "dd/MM";
    private static String formatDay = "d";

    public static DateTimeFormatter dtfDayMonthYear = DateTimeFormatter.ofPattern(formatDayMonthYear);
    public static DateTimeFormatter dtfDayMonthYearTime = DateTimeFormatter.ofPattern(formatDayMonthYearTime);
    public static DateTimeFormatter dtfDayOfWeek = DateTimeFormatter.ofPattern(formatDayOfWeek);
    public static DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern(formatTime);
    public static DateTimeFormatter dtfDayMonth = DateTimeFormatter.ofPattern(formatDayMonth);
    public static DateTimeFormatter dtfDay = DateTimeFormatter.ofPattern(formatDay);

    public static SimpleDateFormat sdfDayMonthYear = new SimpleDateFormat(formatDayMonthYear);
    public static SimpleDateFormat sdfDayMonthYearTime = new SimpleDateFormat(formatDayMonthYearTime);
    public static SimpleDateFormat sdfDayOfWeek = new SimpleDateFormat(formatDayOfWeek);
    public static SimpleDateFormat sdfTime = new SimpleDateFormat(formatTime);
    public static SimpleDateFormat sdfDayMonth = new SimpleDateFormat(formatDayMonth);
    public static SimpleDateFormat sdfMonthYear1 = new SimpleDateFormat("MM - yyyy");
    public static SimpleDateFormat sdfMonthYear2 = new SimpleDateFormat("MM/yyyy");
    public static SimpleDateFormat sdfDay = new SimpleDateFormat(formatDay);
    public static SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
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
                default:
                    return dayOfWeekEng;
            }
        } catch (Exception e) {
            System.out.println(Log.getStackTraceString(e));
        }
        return "";
    }

    public static LocalDateTime getLocalDateTime(String date, String time) {
        return LocalDateTime.of(LocalDate.parse(date, CalendarUtil.dtfDayMonthYear), LocalTime.parse(time, CalendarUtil.dtfTime));
    }
}
