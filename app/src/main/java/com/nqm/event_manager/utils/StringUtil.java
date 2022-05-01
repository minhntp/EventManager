package com.nqm.event_manager.utils;

import android.util.Log;

import java.text.Collator;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtil {

    public static Collator collatorVN = Collator.getInstance(new Locale("vi","VN"));

    static public String normalizeString(String s) {
        try {
            String tempS = Normalizer.normalize(s, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(tempS).replaceAll("").toLowerCase().replaceAll("đ", "d");
        } catch (Exception e) {
            System.out.println( Log.getStackTraceString(e));
        }
        return "";
    }

    static public String startDateEditText = "start date";
    static public String endDateEditText = "end date";

}
