package com.xiaoma.daily.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class ReportDates {
    private ReportDates() { }

    public static String today() {
        return format(Calendar.getInstance(), "yyyy-MM-dd");
    }

    public static Calendar parse(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        formatter.setLenient(false);
        try {
            Date value = formatter.parse(date);
            if (value == null || !formatter.format(value).equals(date)) {
                throw new IllegalArgumentException("Invalid report date: " + date);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(value);
            return calendar;
        } catch (ParseException exception) {
            throw new IllegalArgumentException("Invalid report date: " + date, exception);
        }
    }

    public static String monthStart(String date) {
        Calendar calendar = parse(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return format(calendar, "yyyy-MM-dd");
    }

    public static String shiftMonth(String date, int offset) {
        Calendar calendar = parse(monthStart(date));
        calendar.add(Calendar.MONTH, offset);
        return format(calendar, "yyyy-MM-dd");
    }

    public static int firstDayOffset(String date) {
        return (parse(monthStart(date)).get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }

    public static String format(Calendar calendar, String pattern) {
        return new SimpleDateFormat(pattern, Locale.SIMPLIFIED_CHINESE).format(calendar.getTime());
    }
}
