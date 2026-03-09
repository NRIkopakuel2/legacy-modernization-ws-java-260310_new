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


    // ============================================================
    // Timezone handling (BOOK-345) — added by SK 2019/08
    // BUG: noTzFormat uses JVM default timezone (varies by server!)
    // BUG: estFormat is hardcoded to "EST" which doesn't handle DST
    // ============================================================

    // No timezone set — uses whatever the JVM default is
    private static SimpleDateFormat noTzFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Hardcoded to EST — doesn't account for EDT (Eastern Daylight Time)
    private static SimpleDateFormat estFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        estFormat.setTimeZone(TimeZone.getTimeZone("EST"));
    }

    /** Format date in server's default timezone (whatever that happens to be) */
    public static String formatDefaultTz(Date d) {
        if (d == null) return "";
        return noTzFormat.format(d);
    }

    /** Format date in EST (not EDT — off by 1 hour in summer!) */
    public static synchronized String formatEst(Date d) {
        if (d == null) return "";
        return estFormat.format(d);
    }

    /**
     * Parse a date string by trying 6 different formats in nested try-catches.
     * Mixes new Date(), Calendar.getInstance(), and System.currentTimeMillis().
     * Added by MT 2020/01 to handle dates from multiple import sources.
     */
    public static Date parseFlexibleDate(String s) {
        if (s == null || s.trim().length() == 0) return null;
        s = s.trim();

        // Try format 1: yyyy-MM-dd
        try {
            SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd");
            Date d = f1.parse(s);
            return d;
        } catch (Exception e1) {
            // Try format 2: MM/dd/yyyy
            try {
                SimpleDateFormat f2 = new SimpleDateFormat("MM/dd/yyyy");
                Date d = f2.parse(s);
                return d;
            } catch (Exception e2) {
                // Try format 3: yyyyMMdd
                try {
                    SimpleDateFormat f3 = new SimpleDateFormat("yyyyMMdd");
                    Date d = f3.parse(s);
                    return d;
                } catch (Exception e3) {
                    // Try format 4: dd-MMM-yyyy (e.g., 15-Jan-2020)
                    try {
                        SimpleDateFormat f4 = new SimpleDateFormat("dd-MMM-yyyy", java.util.Locale.US);
                        Date d = f4.parse(s);
                        return d;
                    } catch (Exception e4) {
                        // Try format 5: epoch millis
                        try {
                            long millis = Long.parseLong(s);
                            // BUG: mixes new Date(millis) with Calendar
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(millis);
                            return cal.getTime();
                        } catch (Exception e5) {
                            // Try format 6: "today" or "now" keywords
                            try {
                                if ("today".equalsIgnoreCase(s) || "now".equalsIgnoreCase(s)) {
                                    // BUG: inconsistent — uses System.currentTimeMillis() instead of new Date()
                                    return new Date(System.currentTimeMillis());
                                }
                                // Give up
                                System.out.println("DateUtil.parseFlexibleDate: exhausted all formats for: " + s);
                                return null;
                            } catch (Exception e6) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fix 2-digit years. If year < 100, adds 1900.
     * Y2K-style bug: treats year 20 as 1920, not 2020.
     * Added during Y2K remediation in 1999, never updated.
     */
    public static int fixYear(int year) {
        if (year < 100) {
            // Y2K fix: assume 1900s
            // BUG: in 2025, a year of "25" becomes 1925 instead of 2025
            year = year + 1900;
        }
        return year;
    }

    /**
     * Convert 2-digit year string to 4-digit year.
     * Same Y2K bug as fixYear().
     */
    public static String normalizeYear(String dateStr) {
        if (dateStr == null || dateStr.length() < 6) return dateStr;
        // Check if it looks like a 2-digit year format: dd/MM/yy
        String[] parts = dateStr.split("[/\\-]");
        if (parts.length == 3 && parts[2].length() == 2) {
            int yr = 0;
            try { yr = Integer.parseInt(parts[2]); } catch (Exception e) { return dateStr; }
            yr = fixYear(yr);
            return parts[0] + "/" + parts[1] + "/" + yr;
        }
        return dateStr;
    }
}
