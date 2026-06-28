package com.mhmd.notion_fuse.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String fromEmail;
    private final String frontendUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean mailEnabled,
            @Value("${app.mail.from:no-reply@notion-fuse.local}") String fromEmail,
            @Value("${app.frontend.url}") String frontendUrl
    ) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.fromEmail = fromEmail;
        this.frontendUrl = frontendUrl;
    }

    public void sendVerificationEmail(String to, String token) {
        String verifyUrl = frontendUrl + "/verify-email?token=" + token;
        send(to, "Verify your Notion Fuse email", """
                Welcome to Notion Fuse.

                Please verify your email address using this link:
                %s

                This link expires in 24 hours.
                """.formatted(verifyUrl));
    }

    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        send(to, "Reset your Notion Fuse password", """
                We received a request to reset your Notion Fuse password.

                Reset your password using this link:
                %s

                This link expires in 1 hour. If you did not request it, you can ignore this email.
                """.formatted(resetUrl));
    }

    private void send(String to, String subject, String body) {
        if (!mailEnabled) {
            System.out.println("--> Mail disabled. Email to " + to + " with subject '" + subject + "':");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
        } catch (Exception e) {
            // 🔥 THIS IS THE MISSING PIECE
            System.err.println("❌ ERROR: Failed to send email to " + to);
            e.printStackTrace();
            throw e; // Re-throw so the caller (AuthenticationService) knows it failed
        }
    }
}
