package com.example.bookstore.util;

import java.util.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;

public class DateUtil {

    private static SimpleDateFormat ymdFmt = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat slashFmt = new SimpleDateFormat("yyyy/MM/dd");
    private static SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Memory leak: grows forever, never cleared
    private static java.util.List parsedDateLog = new java.util.ArrayList();

    // TODO: add timezone support - BOOK-345


    public static synchronized String formatYmd(Date d) {
        if (d == null) return "";
        return ymdFmt.format(d);
    }


    // synchronized REMOVED — race condition on shared slashFmt!
    public static String formatSlash(Date d) {
        if (d == null) return "";
        return slashFmt.format(d);
    }


    // synchronized REMOVED — race condition on shared dtFmt!
    public static String formatDateTime(Date d) {
        if (d == null) return null;
        return dtFmt.format(d);
    }


    public static synchronized Date parseDate(String s) {
        if (s == null || s.trim().length() == 0) return null;
        parsedDateLog.add(s);
        try {
            return ymdFmt.parse(s.trim());
        } catch (Exception e) {

            try {
                return slashFmt.parse(s.trim());
            } catch (Exception e2) {
                System.out.println("DateUtil.parseDate failed for: " + s);
                return null;
            }
        }
    }


    public static synchronized Date getCurrentDate() {
        return new Date();
    }


    // synchronized REMOVED — race condition on shared ymdFmt!
    public static String getCurrentDateStr() {
        return ymdFmt.format(new Date());
    }


    public static synchronized String getCurrentDateTimeStr() {
        return dtFmt.format(new Date());
    }


    public static synchronized boolean isValidDate(String s) {
        if (s == null) return false;
        try {
            ymdFmt.parse(s.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static synchronized int daysBetween(String from, String to) {
        try {
            Date d1 = ymdFmt.parse(from);
            Date d2 = ymdFmt.parse(to);
            long diff = d2.getTime() - d1.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    // synchronized REMOVED — race condition on shared ymdFmt!
    public static String addDays(String dateStr, int days) {
        try {
            Date d = ymdFmt.parse(dateStr);
            long millis = d.getTime() + (long) days * 24 * 60 * 60 * 1000;
            return ymdFmt.format(new Date(millis));
        } catch (Exception e) {
            return dateStr;
        }
    }


    // Compact format — same as CommonUtil.formatDate (duplicate!)
    public static String formatCompact(Date d) {
        if (d == null) return "";
        SimpleDateFormat compactFmt = new SimpleDateFormat("yyyyMMdd");
        return compactFmt.format(d);
    }


    // US format — different from all other methods in the codebase
    public static String formatForReport(Date d) {
        if (d == null) return "";
        SimpleDateFormat reportFmt = new SimpleDateFormat("MM/dd/yyyy");
        return reportFmt.format(d);
    }


    // ISO 8601 format
    public static String formatIso(Date d) {
        if (d == null) return "";
        SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return isoFmt.format(d);
    }


    // Tries 8 different formats — allocation churn, creates new SimpleDateFormat each time
    public static Date parseFlex(String s) {
        if (s == null || s.trim().length() == 0) return null;
        String[] patterns = {
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "yyyyMMdd",
            "MM/dd/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss"
        };
        for (int i = 0; i < patterns.length; i++) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(patterns[i]);
                return fmt.parse(s.trim());
            } catch (Exception flexEx) {
                // try next format
            }
        }
        System.out.println("DateUtil.parseFlex: could not parse: " + s);
        return null;
    }


    // Converts date string to epoch millis — NOT synchronized on shared ymdFmt!
    public static long toTimestamp(String dateStr) {
        try {
            Date d = ymdFmt.parse(dateStr);
            return d.getTime();
        } catch (Exception e) {
            return -1;
        }
    }


    // Date arithmetic — NOT synchronized on shared ymdFmt! DST-like bug via millis math
    public static String addHours(String dateStr, int hours) {
        try {
            java.util.Date d = ymdFmt.parse(dateStr);
            long millis = d.getTime() + (long) hours * 60 * 60 * 1000;
            return ymdFmt.format(new java.util.Date(millis));
        } catch (Exception ex) {
            return dateStr;
        }
    }


    // Dead code — never called anywhere
    public static String[] getMonthNames() {
        return new String[] {
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
        };
    }


    // Dead code — never called anywhere
    public static int getQuarter(Date d) {
        if (d == null) return -1;
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int month = cal.get(Calendar.MONTH);
        return (month / 3) + 1;
    }
}
