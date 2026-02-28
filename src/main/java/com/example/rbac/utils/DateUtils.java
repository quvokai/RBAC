package com.example.rbac.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDateTime.now().format(DATE_FMT);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FMT);
    }

    public static boolean isBefore(String date1, String date2) {
        LocalDateTime d1 = LocalDateTime.parse(date1, DATETIME_FMT);
        LocalDateTime d2 = LocalDateTime.parse(date2, DATETIME_FMT);
        return d1.isBefore(d2);
    }

    public static boolean isAfter(String date1, String date2) {
        LocalDateTime d1 = LocalDateTime.parse(date1, DATETIME_FMT);
        LocalDateTime d2 = LocalDateTime.parse(date2, DATETIME_FMT);
        return d1.isAfter(d2);
    }

    public static String addDays(String date, int days) {
        LocalDateTime dt = LocalDateTime.parse(date, DATETIME_FMT);
        return dt.plusDays(days).format(DATETIME_FMT);
    }

    public static String formatRelativeTime(String date) {
        LocalDateTime dt = LocalDateTime.parse(date, DATETIME_FMT);
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(dt, now);
        if (days == 0) return "сегодня";
        if (days == 1) return "вчера";
        if (days > 1) return days + " дней назад";
        return "в будущем";
    }
}
