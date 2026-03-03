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

    
    public static synchronized String formatYmd(Date d) {
        if (d == null) return "";
        return ymdFmt.format(d);
    }

    
    public static synchronized String formatSlash(Date d) {
        if (d == null) return "";
        return slashFmt.format(d);
    }

    
    public static synchronized String formatDateTime(Date d) {
        if (d == null) return null;
        return dtFmt.format(d);
    }

    
    public static synchronized Date parseDate(String s) {
        if (s == null || s.trim().length() == 0) return null;
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

    
    public static synchronized String getCurrentDateStr() {
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

    
    public static synchronized String addDays(String dateStr, int days) {
        try {
            Date d = ymdFmt.parse(dateStr);
            long millis = d.getTime() + (long) days * 24 * 60 * 60 * 1000;
            return ymdFmt.format(new Date(millis));
        } catch (Exception e) {
            return dateStr;
        }
    }
}
