package com.example.bookstore.util;

import java.util.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.SimpleDateFormat;

import com.example.bookstore.constant.AppConstants;

/**
 * Validation helper utilities.
 * Provides validation methods for various data types and business objects.
 * NOTE: Some methods overlap with CommonUtil validation methods but use
 *       different validation logic / rules.
 * @author dev4
 * @since 2010-01-10
 */
public class ValidationHelper implements AppConstants {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^-?[0-9]+(\\.[0-9]+)?$");
    private static final Pattern ISBN10_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern ISBN13_PATTERN = Pattern.compile("^[0-9]{13}$");
    private static final Pattern PRICE_PATTERN = Pattern.compile("^[0-9]+(\\.[0-9]{1,2})?$");

    private static Map validationRules = new HashMap();

    private static int validationCount = 0;

    /**
     * Check if email is valid using regex.
     * NOTE: Different from CommonUtil.isValidEmail which just checks indexOf("@").
     * This uses a proper regex pattern.
     */
    public static boolean checkEmail(String email) {
        if (email == null || email.trim().length() == 0) return false;
        validationCount++;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Check if phone number has exactly 10 digits (after stripping formatting).
     * NOTE: Different from CommonUtil.isValidPhone which allows 7-15 digits.
     */
    public static boolean checkPhone(String phone) {
        if (phone == null || phone.trim().length() == 0) return false;
        // strip all non-digit characters
        String digits = phone.replaceAll("[^0-9]", "");
        return PHONE_PATTERN.matcher(digits).matches();
    }

    /**
     * Check that ALL provided fields are non-null and non-empty.
     * Returns false if any field is empty.
     */
    public static boolean checkRequired(String[] fields) {
        if (fields == null || fields.length == 0) return false;
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] == null || fields[i].trim().length() == 0) {
                System.out.println("ValidationHelper.checkRequired: field " + i + " is empty");
                return false;
            }
        }
        return true;
    }

    /**
     * Check if string is numeric.
     * Handles negative numbers and decimals.
     * NOTE: Different from CommonUtil.isNumeric -- this rejects leading zeros
     *       on the integer part (e.g., "007" returns false, but "0.7" returns true).
     */
    public static boolean checkNumeric(String s) {
        if (s == null || s.trim().length() == 0) return false;
        String trimmed = s.trim();
        // reject leading zeros (but allow "0" and "0.xxx")
        if (trimmed.length() > 1 && trimmed.charAt(0) == '0' && trimmed.charAt(1) != '.') {
            // leading zero on non-decimal
            if (!trimmed.startsWith("-")) {
                return false;
            }
        }
        if (trimmed.startsWith("-") && trimmed.length() > 2 && trimmed.charAt(1) == '0' && trimmed.charAt(2) != '.') {
            return false;
        }
        return NUMERIC_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validate book data map.
     * Returns error message string, or null if valid.
     * Expected keys: "title", "isbn", "categoryId", "price"
     */
    public static String validateBookData(Map data) {
        if (data == null || data.isEmpty()) return "Book data is required";

        StringBuffer errors = new StringBuffer();

        // validate title
        String title = (String) data.get("title");
        if (title == null || title.trim().length() == 0) {
            errors.append("Title is required. ");
        } else if (title.trim().length() > 200) {
            errors.append("Title must be less than 200 characters. ");
        }

        // validate isbn
        String isbn = (String) data.get("isbn");
        if (isbn == null || isbn.trim().length() == 0) {
            errors.append("ISBN is required. ");
        } else {
            String isbnTrimmed = isbn.trim();
            if (isbnTrimmed.length() != 10 && isbnTrimmed.length() != 13) {
                errors.append("ISBN must be 10 or 13 characters. ");
            }
        }

        // validate categoryId
        String catId = (String) data.get("categoryId");
        if (catId == null || catId.trim().length() == 0) {
            errors.append("Category is required. ");
        }

        // validate price
        String price = (String) data.get("price");
        if (price == null || price.trim().length() == 0) {
            errors.append("Price is required. ");
        } else {
            try {
                double p = Double.parseDouble(price.trim());
                if (p < 0) {
                    errors.append("Price must be positive. ");
                }
                if (p > 99999.99) {
                    errors.append("Price exceeds maximum value. ");
                }
            } catch (NumberFormatException e) {
                errors.append("Price must be a valid number. ");
            }
        }

        String result = errors.toString().trim();
        if (result.length() > 0) {
            System.out.println("ValidationHelper.validateBookData errors: " + result);
            return result;
        }
        return null;
    }

    /**
     * Validate order data map.
     * Returns error message string, or null if valid.
     * Expected keys: "email", "payMethod", "shipName", "shipAddr"
     * NOTE: Uses different email validation than checkEmail -- also checks for
     *       .com or .org domain suffix.
     */
    public static String validateOrderData(Map data) {
        if (data == null || data.isEmpty()) return "Order data is required";

        StringBuffer errors = new StringBuffer();

        // validate email -- special validation with domain check
        String email = (String) data.get("email");
        if (email == null || email.trim().length() == 0) {
            errors.append("Email is required. ");
        } else {
            String emailTrimmed = email.trim();
            if (emailTrimmed.indexOf("@") <= 0) {
                errors.append("Email must contain @. ");
            } else if (!emailTrimmed.endsWith(".com") && !emailTrimmed.endsWith(".org")
                       && !emailTrimmed.endsWith(".net") && !emailTrimmed.endsWith(".edu")) {
                errors.append("Email must have a valid domain (.com, .org, .net, .edu). ");
            }
        }

        // validate payMethod
        String payMethod = (String) data.get("payMethod");
        if (payMethod == null || payMethod.trim().length() == 0) {
            errors.append("Payment method is required. ");
        } else {
            String pm = payMethod.trim();
            if (!"CREDIT".equals(pm) && !"DEBIT".equals(pm) && !"CASH".equals(pm) && !"CHECK".equals(pm)) {
                errors.append("Invalid payment method. ");
            }
        }

        // validate shipName
        String shipName = (String) data.get("shipName");
        if (shipName == null || shipName.trim().length() == 0) {
            errors.append("Shipping name is required. ");
        }

        // validate shipAddr
        String shipAddr = (String) data.get("shipAddr");
        if (shipAddr == null || shipAddr.trim().length() == 0) {
            errors.append("Shipping address is required. ");
        }

        String result = errors.toString().trim();
        if (result.length() > 0) {
            return result;
        }
        return null;
    }

    /**
     * Check if ISBN format is valid (10 or 13 digits only).
     * In practice this is dead code -- validateBookData also checks ISBN.
     */
    public static boolean isValidIsbn(String isbn) {
        if (isbn == null || isbn.trim().length() == 0) return false;
        String trimmed = isbn.trim();
        return ISBN10_PATTERN.matcher(trimmed).matches() || ISBN13_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Check if price string is a valid positive number with max 2 decimal places.
     */
    public static boolean isValidPrice(String price) {
        if (price == null || price.trim().length() == 0) return false;
        String trimmed = price.trim();
        if (!PRICE_PATTERN.matcher(trimmed).matches()) return false;
        try {
            double d = Double.parseDouble(trimmed);
            return d > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if value is within range (inclusive).
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Sanitize input by HTML entity encoding.
     * Similar to CommonUtil.escapeHtml but ALSO escapes single quotes.
     */
    public static String sanitize(String input) {
        if (input == null) return "";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Load validation rules from hardcoded values.
     * (Not currently called anywhere -- kept for future configuration-driven validation)
     */
    public static void loadRules() {
        validationRules.put("title.maxLen", "200");
        validationRules.put("title.required", "true");
        validationRules.put("isbn.pattern", "^[0-9]{10,13}$");
        validationRules.put("isbn.required", "true");
        validationRules.put("price.min", "0.01");
        validationRules.put("price.max", "99999.99");
        validationRules.put("price.required", "true");
        validationRules.put("categoryId.required", "true");
        validationRules.put("email.required", "true");
        validationRules.put("email.pattern", "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        validationRules.put("phone.minDigits", "10");
        validationRules.put("phone.maxDigits", "10");
        validationRules.put("shipName.required", "true");
        validationRules.put("shipAddr.required", "true");
        System.out.println("ValidationHelper: loaded " + validationRules.size() + " rules");
    }

    /**
     * Get validation count (for monitoring).
     */
    public static int getValidationCount() {
        return validationCount;
    }
}
