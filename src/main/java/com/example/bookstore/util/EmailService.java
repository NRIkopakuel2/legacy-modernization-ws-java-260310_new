package com.example.bookstore.util;

import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

import com.example.bookstore.constant.AppConstants;

// Email service - SMTP configuration pending
// TODO: configure SMTP server settings - MT 2019/06
// NOTE: all methods currently log to stdout instead of sending emails
public class EmailService implements AppConstants {

    private static EmailService instance = null;

    private String smtpHost = null;
    private int smtpPort = 0;
    private String smtpUser = null;
    private String smtpPassword = null;
    private boolean sslEnabled = false;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private int sendCount = 0;
    private int failCount = 0;

    private EmailService() {
        // TODO: load from properties file or DB config
        System.out.println("[EmailService] Initialized (SMTP not configured)");
    }

    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    private boolean isConfigured() {
        return smtpHost != null && smtpHost.length() > 0 && smtpPort > 0;
    }

    public void sendOrderConfirmation(String email, String orderNo, double total) {
        if (!isConfigured()) {
            System.out.println("[EMAIL NOT CONFIGURED] Order confirmation for " + email
                + " - Order#" + orderNo + " Total: $" + CommonUtil.formatMoney(total));
            failCount++;
            return;
        }
        String subject = EMAIL_SUBJECT_ORDER + " #" + orderNo;
        String body = buildHtmlTemplate("Order Confirmation",
            "<p>Thank you for your order!</p>"
            + "<p>Order Number: <strong>" + orderNo + "</strong></p>"
            + "<p>Total Amount: <strong>$" + CommonUtil.formatMoney(total) + "</strong></p>"
            + "<p>You will receive a shipping notification once your order has been dispatched.</p>");
        sendEmail(email, subject, body);
    }

    public void sendLowStockAlert(String bookTitle, int currentQty, int threshold) {
        if (!isConfigured()) {
            System.out.println("[EMAIL NOT CONFIGURED] Low stock alert - Book: " + bookTitle
                + " Qty: " + currentQty + " Threshold: " + threshold);
            failCount++;
            return;
        }
        String subject = EMAIL_SUBJECT_STOCK + " - " + bookTitle;
        String body = buildHtmlTemplate("Low Stock Alert",
            "<p style='color:red;'><strong>WARNING: Low Stock Detected</strong></p>"
            + "<p>Book: " + bookTitle + "</p>"
            + "<p>Current Quantity: " + currentQty + "</p>"
            + "<p>Threshold: " + threshold + "</p>"
            + "<p>Please reorder immediately.</p>");
        sendEmail(EMAIL_FROM, subject, body);
    }

    public void sendPasswordReset(String email, String token) {
        if (!isConfigured()) {
            System.out.println("[EMAIL NOT CONFIGURED] Password reset for " + email
                + " - Token: " + token);
            failCount++;
            return;
        }
        String resetUrl = "http://localhost:8080/bookstore/resetPassword?token=" + token;
        String body = buildHtmlTemplate("Password Reset",
            "<p>You requested a password reset.</p>"
            + "<p>Click the link below to reset your password:</p>"
            + "<p><a href='" + resetUrl + "'>" + resetUrl + "</a></p>"
            + "<p>This link will expire in 24 hours.</p>"
            + "<p>If you did not request this, please ignore this email.</p>");
        sendEmail(email, "Password Reset Request", body);
    }

    public void sendWelcomeEmail(String email, String name) {
        if (!isConfigured()) {
            System.out.println("[EMAIL NOT CONFIGURED] Welcome email for " + name + " (" + email + ")");
            failCount++;
            return;
        }
        String body = buildHtmlTemplate("Welcome to Bookstore",
            "<p>Dear " + name + ",</p>"
            + "<p>Welcome to our bookstore! Your account has been created successfully.</p>"
            + "<p>You can now browse our catalog and place orders.</p>"
            + "<p>Happy reading!</p>");
        sendEmail(email, "Welcome to Bookstore!", body);
    }

    public void sendShippingNotification(String email, String orderNo, String trackingNo) {
        if (!isConfigured()) {
            System.out.println("[EMAIL NOT CONFIGURED] Shipping notification for " + email
                + " - Order#" + orderNo + " Tracking: " + trackingNo);
            failCount++;
            return;
        }
        String body = buildHtmlTemplate("Your Order Has Shipped",
            "<p>Great news! Your order has been shipped.</p>"
            + "<p>Order Number: <strong>" + orderNo + "</strong></p>"
            + "<p>Tracking Number: <strong>" + trackingNo + "</strong></p>"
            + "<p>You can track your package using the tracking number above.</p>"
            + "<p>Estimated delivery: 3-5 business days.</p>");
        sendEmail(email, "Your order #" + orderNo + " has shipped!", body);
    }

    private String buildHtmlTemplate(String title, String body) {
        StringBuffer sb = new StringBuffer();
        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset='UTF-8'>");
        sb.append("<title>").append(title).append("</title>");
        sb.append("<style>");
        sb.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4; }");
        sb.append(".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 5px; }");
        sb.append(".header { background-color: #2c3e50; color: #ffffff; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }");
        sb.append(".header h1 { margin: 0; font-size: 24px; }");
        sb.append(".content { padding: 20px 0; }");
        sb.append(".footer { text-align: center; color: #999999; font-size: 12px; padding-top: 20px; border-top: 1px solid #eeeeee; }");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<div class='container'>");
        sb.append("<div class='header'><h1>").append(title).append("</h1></div>");
        sb.append("<div class='content'>").append(body).append("</div>");
        sb.append("<div class='footer'>");
        sb.append("<p>Bookstore - Your favorite online bookstore</p>");
        sb.append("<p>This is an automated message. Please do not reply.</p>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    private void sendEmail(String to, String subject, String body) {
        if (!isConfigured()) {
            System.out.println("[EmailService] Cannot send email - SMTP not configured");
            System.out.println("[EmailService]   To: " + to);
            System.out.println("[EmailService]   Subject: " + subject);
            System.out.println("[EmailService]   Body length: " + (body != null ? body.length() : 0) + " chars");
            failCount++;
            return;
        }

        // TODO: implement actual SMTP sending using JavaMail API
        // Properties props = new Properties();
        // props.put("mail.smtp.host", smtpHost);
        // props.put("mail.smtp.port", String.valueOf(smtpPort));
        // Session session = Session.getInstance(props, authenticator);
        // ...
        System.out.println("[EmailService] Would send email to " + to + " - " + subject);
        sendCount++;
    }

    public void setSmtpHost(String host) { this.smtpHost = host; }
    public void setSmtpPort(int port) { this.smtpPort = port; }
    public void setSmtpUser(String user) { this.smtpUser = user; }
    public void setSmtpPassword(String password) { this.smtpPassword = password; }
    public void setSslEnabled(boolean ssl) { this.sslEnabled = ssl; }

    public int getSendCount() { return sendCount; }
    public int getFailCount() { return failCount; }
}
