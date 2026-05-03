

package com.campus.ems.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.from:noreply@campus.edu}") private String fromEmail;

    public void sendOtpEmail(String to, String name, String otp) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Smart Campus EMS - Email Verification OTP");
            helper.setText(buildOtpHtml(name, otp), true);
            mailSender.send(msg);
        } catch (Exception e) { log.error("Error sending OTP email: {}", e.getMessage()); }
    }

    public void sendRegistrationConfirmation(String to, String name, String eventTitle, String ticketNum) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Event Registration Confirmed - " + eventTitle);
            helper.setText(buildRegistrationHtml(name, eventTitle, ticketNum), true);
            mailSender.send(msg);
        } catch (Exception e) { log.error("Error sending registration email: {}", e.getMessage()); }
    }

    public void sendPasswordResetEmail(String to, String name, String otp) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Smart Campus EMS - Password Reset OTP");
            helper.setText(buildPasswordResetHtml(name, otp), true);
            mailSender.send(msg);
        } catch (Exception e) { log.error("Error sending password reset email: {}", e.getMessage()); }
    }

    private String buildOtpHtml(String name, String otp) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px'>" +
               "<div style='max-width:500px;margin:0 auto;background:white;border-radius:10px;padding:30px'>" +
               "<h2 style='color:#6c63ff'>Smart Campus EMS</h2>" +
               "<p>Hello <strong>" + name + "</strong>,</p>" +
               "<p>Your email verification OTP is:</p>" +
               "<div style='background:#6c63ff;color:white;font-size:32px;font-weight:bold;text-align:center;" +
               "padding:20px;border-radius:8px;letter-spacing:8px'>" + otp + "</div>" +
               "<p style='color:#666;margin-top:20px'>Valid for <strong>10 minutes</strong>. Do not share it.</p>" +
               "</div></body></html>";
    }

    private String buildRegistrationHtml(String name, String eventTitle, String ticketNum) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px'>" +
               "<div style='max-width:500px;margin:0 auto;background:white;border-radius:10px;padding:30px'>" +
               "<h2 style='color:#6c63ff'>✅ Registration Confirmed!</h2>" +
               "<p>Hello <strong>" + name + "</strong>,</p>" +
               "<p>You have successfully registered for <strong>" + eventTitle + "</strong>.</p>" +
               "<div style='background:#f0edff;border-left:4px solid #6c63ff;padding:15px;margin:20px 0'>" +
               "<p style='margin:0'><strong>Ticket Number:</strong> " + ticketNum + "</p></div>" +
               "<p>Your QR code ticket is available in 'My Events'. See you there! 🎉</p>" +
               "</div></body></html>";
    }

    private String buildPasswordResetHtml(String name, String otp) {
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px'>" +
               "<div style='max-width:500px;margin:0 auto;background:white;border-radius:10px;padding:30px'>" +
               "<h2 style='color:#6c63ff'>Password Reset</h2>" +
               "<p>Hello <strong>" + name + "</strong>,</p>" +
               "<p>Your password reset OTP is:</p>" +
               "<div style='background:#ff6b6b;color:white;font-size:32px;font-weight:bold;text-align:center;" +
               "padding:20px;border-radius:8px;letter-spacing:8px'>" + otp + "</div>" +
               "<p style='color:#666;margin-top:20px'>Valid for <strong>10 minutes</strong>.</p>" +
               "</div></body></html>";
    }
}
