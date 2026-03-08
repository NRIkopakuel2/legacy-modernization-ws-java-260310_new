// BOOK-456: Email notification service
// Author: SK 2019/07
// Status: Feature-flagged behind AppConstants.ENABLE_EMAIL_NOTIFICATIONS (false)
// NOTE: Originally tried to use javax.mail but the JAR was never added to lib/
//       Rewrote to use raw Socket SMTP. Never tested in production.
//       smtp.bookstore-internal.local was decommissioned in 2020 but nobody
//       updated this code because the feature flag was never turned on.
package com.example.bookstore.service;

import java.io.*;
import java.net.Socket;
import java.util.*;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.util.CommonUtil;

public class EmailNotificationService implements AppConstants {

    private static final String SMTP_HOST = "smtp.bookstore-internal.local";
    private static final int SMTP_PORT = 25;
    private static final String FROM_ADDRESS = "noreply@bookstore.example.com";
    private static final int SOCKET_TIMEOUT = 30000; // 30 seconds

    private static EmailNotificationService instance = new EmailNotificationService();

    private EmailNotificationService() {
    }

    public static EmailNotificationService getInstance() {
        return instance;
    }

    public boolean sendOrderConfirmation(String toEmail, String orderNo, double total) {
        if (!ENABLE_EMAIL_NOTIFICATIONS) {
            System.out.println("[EmailService] Notifications disabled, skipping order confirmation");
            return false;
        }
        String subject = EMAIL_SUBJECT_ORDER + " #" + orderNo;
        String body = "Dear Customer,\n\n"
            + "Thank you for your order #" + orderNo + ".\n"
            + "Total: $" + CommonUtil.formatMoney(total) + "\n\n"
            + "Your order is being processed.\n\n"
            + "Best regards,\nBookstore Team";
        return sendEmail(toEmail, subject, body);
    }

    public boolean sendPasswordReset(String toEmail, String resetToken) {
        if (!ENABLE_EMAIL_NOTIFICATIONS) {
            return false;
        }
        String subject = "Password Reset Request";
        String body = "Dear Customer,\n\n"
            + "You have requested a password reset.\n"
            + "Please use the following token: " + resetToken + "\n"
            + "This token expires in 24 hours.\n\n"
            + "If you did not request this, please ignore this email.\n\n"
            + "Best regards,\nBookstore Team";
        return sendEmail(toEmail, subject, body);
    }

    public boolean sendLowStockAlert(String bookTitle, int currentStock) {
        if (!ENABLE_EMAIL_NOTIFICATIONS) {
            return false;
        }
        String subject = EMAIL_SUBJECT_STOCK + ": " + bookTitle;
        String body = "ALERT: Low stock detected.\n\n"
            + "Book: " + bookTitle + "\n"
            + "Current Stock: " + currentStock + "\n"
            + "Threshold: " + LOW_STOCK_THRESHOLD + "\n\n"
            + "Please reorder immediately.";
        return sendEmail("inventory@bookstore.example.com", subject, body);
    }

    private boolean sendEmail(String to, String subject, String body) {
        Socket socket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        boolean success = false;

        for (int attempt = 0; attempt < EMAIL_RETRY_COUNT; attempt++) {
            try {
                socket = new Socket(SMTP_HOST, SMTP_PORT);
                socket.setSoTimeout(SOCKET_TIMEOUT);

                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                // Read server greeting
                String response = reader.readLine();
                if (response == null || !response.startsWith("220")) {
                    System.out.println("[EmailService] Bad SMTP greeting: " + response);
                    continue;
                }

                // HELO
                writer.println("HELO bookstore.example.com");
                response = reader.readLine();

                // MAIL FROM
                writer.println("MAIL FROM:<" + FROM_ADDRESS + ">");
                response = reader.readLine();

                // RCPT TO
                writer.println("RCPT TO:<" + to + ">");
                response = reader.readLine();

                // DATA
                writer.println("DATA");
                response = reader.readLine();
                if (response != null && response.startsWith("354")) {
                    writer.println("From: " + FROM_ADDRESS);
                    writer.println("To: " + to);
                    writer.println("Subject: " + subject);
                    writer.println("Content-Type: text/plain; charset=ISO-8859-1");
                    writer.println("X-Mailer: BookstoreApp/1.0");
                    writer.println();
                    writer.println(body);
                    writer.println(".");
                    response = reader.readLine();
                    if (response != null && response.startsWith("250")) {
                        success = true;
                    }
                }

                // QUIT
                writer.println("QUIT");
                reader.readLine();

                if (success) break;
            } catch (Exception e) {
                System.out.println("[EmailService] Send attempt " + (attempt + 1)
                    + " failed: " + e.getMessage());
                // Sleep before retry
                try { Thread.sleep(1000 * (attempt + 1)); } catch (InterruptedException ie) { }
            } finally {
                try { if (writer != null) writer.close(); } catch (Exception e) { }
                try { if (reader != null) reader.close(); } catch (Exception e) { }
                try { if (socket != null) socket.close(); } catch (Exception e) { }
            }
        }

        if (!success) {
            System.out.println("[EmailService] Failed to send email to " + to
                + " after " + EMAIL_RETRY_COUNT + " attempts");
        }
        return success;
    }
}
