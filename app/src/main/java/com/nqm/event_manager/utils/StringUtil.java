package com.nqm.event_manager.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtil {
    static public String normalizeString(String s) {
        try {
            String tempS = Normalizer.normalize(s, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(tempS).replaceAll("").toLowerCase().replaceAll("đ", "d");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
