package com.example.rbac.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static boolean isBefore(String date1, String date2) {
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        LocalDateTime dt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return dt.plusDays(days).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String formatRelativeTime(String date) {
        LocalDateTime dt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateTime now = LocalDateTime.now();
        long daysDiff = now.toLocalDate().toEpochDay() - dt.toLocalDate().toEpochDay();
        if (daysDiff == 0) return "сегодня";
        if (daysDiff == 1) return "завтра";
        if (daysDiff == -1) return "вчера";
        if (daysDiff > 0) return "через " + daysDiff + " дней";
        return -daysDiff + " дней назад";
    }
}