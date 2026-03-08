package com.example.bookstore.util;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.net.URLEncoder;

import com.example.bookstore.constant.AppConstants;

/**
 * Formatting helper utilities.
 * Provides convenient formatting methods for dates, money, strings, etc.
 * NOTE: Some methods are similar to CommonUtil and DateUtil -- use whichever
 *       is most convenient for your use case.
 * @author dev3
 * @since 2009-06-22
 */
public class FormatHelper implements AppConstants {

    // thread-unsafe static formatters (same pattern as CommonUtil)
    private static SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy");
    private static SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat slashFmt = new SimpleDateFormat("yyyy/MM/dd");
    private static SimpleDateFormat hyphenFmt = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static SimpleDateFormat mmddyyyyDash = new SimpleDateFormat("MM-dd-yyyy");

    private static DecimalFormat moneyFmt = new DecimalFormat("#,##0.00");
    private static DecimalFormat percentFmt = new DecimalFormat("0.0");

    private static int formatCallCount = 0;

    /**
     * Format date as MM/dd/yyyy.
     * NOTE: This is DIFFERENT from CommonUtil.formatDate (yyyyMMdd)
     *       and CommonUtil.formatDate2 (yyyy/MM/dd).
     */
    public static String fmtDate(Date d) {
        if (d == null) return "";
        formatCallCount++;
        return mmddyyyy.format(d);
    }

    /**
     * Parse a date string and reformat it.
     * Tries multiple input formats (in different order than CommonUtil.formatDateNew).
     * Returns reformatted string as yyyy/MM/dd.
     */
    public static String fmtDate2(String s) {
        if (s == null || s.trim().length() == 0) return "";
        String input = s.trim();

        // try yyyy/MM/dd first
        try {
            Date d = slashFmt.parse(input);
            return slashFmt.format(d);
        } catch (Exception e) { }

        // try yyyyMMdd
        try {
            Date d = yyyymmdd.parse(input);
            return slashFmt.format(d);
        } catch (Exception e) { }

        // try yyyy-MM-dd
        try {
            Date d = hyphenFmt.parse(input);
            return slashFmt.format(d);
        } catch (Exception e) { }

        // try MM/dd/yyyy
        try {
            Date d = mmddyyyy.parse(input);
            return slashFmt.format(d);
        } catch (Exception e) { }

        // try MM-dd-yyyy
        try {
            Date d = mmddyyyyDash.parse(input);
            return slashFmt.format(d);
        } catch (Exception e) { }

        System.out.println("FormatHelper.fmtDate2: could not parse date: " + s);
        return s;
    }

    /**
     * Format money value using DecimalFormat.
     * NOTE: Produces different output than CommonUtil.formatMoney for some edge cases
     *       (e.g., large numbers get comma separators: "1,234.56" vs "1234.56").
     */
    public static String fmtMoney(double d) {
        formatCallCount++;
        return moneyFmt.format(d);
    }

    /**
     * Format as percentage.
     * NOTE: Adds a SPACE before % sign, unlike CommonUtil.formatPercent which does not.
     */
    public static String fmtPercent(double d) {
        return percentFmt.format(d) + " %";
    }

    /**
     * Generic date formatting. Creates a NEW SimpleDateFormat each time.
     * Use when you need a custom pattern.
     */
    public static String toDateStr(Date d, String pattern) {
        if (d == null) return "";
        if (pattern == null || pattern.length() == 0) {
            pattern = "yyyy/MM/dd";
        }
        // create new formatter each time -- not ideal but safe for threading
        SimpleDateFormat fmt = new SimpleDateFormat(pattern);
        return fmt.format(d);
    }

    /**
     * Try to parse a date string from any of 6 common formats.
     * Each format attempt is in its own try/catch with a new SimpleDateFormat.
     */
    public static Date parseAny(String s) {
        if (s == null || s.trim().length() == 0) return null;
        String input = s.trim();

        // format 1: yyyyMMdd
        try {
            SimpleDateFormat f1 = new SimpleDateFormat("yyyyMMdd");
            f1.setLenient(false);
            return f1.parse(input);
        } catch (Exception e) {
            // try next format
        }

        // format 2: yyyy/MM/dd
        try {
            SimpleDateFormat f2 = new SimpleDateFormat("yyyy/MM/dd");
            f2.setLenient(false);
            return f2.parse(input);
        } catch (Exception e) {
            // try next format
        }

        // format 3: yyyy-MM-dd
        try {
            SimpleDateFormat f3 = new SimpleDateFormat("yyyy-MM-dd");
            f3.setLenient(false);
            return f3.parse(input);
        } catch (Exception e) {
            // try next format
        }

        // format 4: MM/dd/yyyy
        try {
            SimpleDateFormat f4 = new SimpleDateFormat("MM/dd/yyyy");
            f4.setLenient(false);
            return f4.parse(input);
        } catch (Exception e) {
            // try next format
        }

        // format 5: dd-MM-yyyy
        try {
            SimpleDateFormat f5 = new SimpleDateFormat("dd-MM-yyyy");
            f5.setLenient(false);
            return f5.parse(input);
        } catch (Exception e) {
            // try next format
        }

        // format 6: MM-dd-yyyy
        try {
            SimpleDateFormat f6 = new SimpleDateFormat("MM-dd-yyyy");
            f6.setLenient(false);
            return f6.parse(input);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("FormatHelper.parseAny: unable to parse: " + s);
        return null;
    }

    /**
     * Format file size in bytes to human-readable string.
     * (Not currently used anywhere -- kept for future use)
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 0) return "0 B";
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            double kb = bytes / 1024.0;
            return new DecimalFormat("0.#").format(kb) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            double mb = bytes / (1024.0 * 1024.0);
            return new DecimalFormat("0.##").format(mb) + " MB";
        } else {
            double gb = bytes / (1024.0 * 1024.0 * 1024.0);
            return new DecimalFormat("0.##").format(gb) + " GB";
        }
    }

    /**
     * Truncate string and add ellipsis if too long.
     */
    public static String truncateWithEllipsis(String s, int maxLen) {
        if (s == null) return null;
        if (maxLen < 4) maxLen = 4;
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    /**
     * Pad string on the left with spaces.
     * Similar to CommonUtil.leftPad but only uses spaces.
     */
    public static String padLeft(String s, int len) {
        if (s == null) s = "";
        StringBuffer sb = new StringBuffer();
        for (int i = s.length(); i < len; i++) {
            sb.append(' ');
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Pad string on the right with spaces.
     * Similar to CommonUtil.rightPad but only uses spaces.
     */
    public static String padRight(String s, int len) {
        if (s == null) s = "";
        StringBuffer sb = new StringBuffer(s);
        for (int i = s.length(); i < len; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Format an integer with leading zeros.
     */
    public static String zeroPad(int num, int width) {
        String s = String.valueOf(num);
        StringBuffer sb = new StringBuffer();
        for (int i = s.length(); i < width; i++) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Get format call count (for debugging / monitoring).
     */
    public static int getFormatCallCount() {
        return formatCallCount;
    }

    /**
     * Reset format call count.
     */
    public static void resetFormatCallCount() {
        formatCallCount = 0;
    }
}
