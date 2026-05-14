package com.donorconnect.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // it pull the email from properties file so that email knows who the sender is
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetToken(String toEmail, String name, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - DonorConnect");
            helper.setText(buildResetEmailContent(name, token), true);

            mailSender.send(message);
            log.info("Password reset token sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send reset email to {}: {}", toEmail, e.getMessage());
        }
    }






    private String buildResetEmailContent(String name, String token) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #C1121F, #8B0000); color: white; padding: 24px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { padding: 28px; background-color: #f9f9f9; }
                    .token-box {
                        background: #fff5f5;
                        border: 2px dashed #C1121F;
                        border-radius: 8px;
                        padding: 20px;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .token {
                        font-family: monospace;
                        font-size: 1.6rem;
                        font-weight: 900;
                        color: #C1121F;
                        letter-spacing: 0.3em;
                        word-break: break-all;
                    }
                    .footer { padding: 16px; text-align: center; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 style="margin:0;font-size:1.5rem">🩸 DonorConnect</h1>
                        <p style="margin:6px 0 0;opacity:0.85;font-size:0.85rem">Blood Bank Management System</p>
                    </div>
                    <div class="content">
                        <h2 style="color:#C1121F">Password Reset Request</h2>
                        <p>Hi <strong>%s</strong>,</p>
                        <p>We received a request to reset your DonorConnect password. Use the token below on the reset password page:</p>
                        
                        <div class="token-box">
                            <div style="font-size:0.75rem;font-weight:700;text-transform:uppercase;letter-spacing:0.1em;color:#C1121F;margin-bottom:10px">Your Reset Token</div>
                            <div class="token">%s</div>
                            <div style="font-size:0.75rem;color:#999;margin-top:10px">⏱ Valid for 15 minutes only</div>
                        </div>

                        <p>Copy this token and paste it into the reset password page.</p>
                        <p>If you did not request this, please ignore this email. Your password will not change.</p>

                        <p style="font-size:0.85rem;color:#666">For security, please:</p>
                        <ul style="font-size:0.85rem;color:#666">
                            <li>Use a strong password with at least 6 characters</li>
                            <li>Do not share this token with anyone</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 DonorConnect. All rights reserved.</p>
                        <p>This is an automated message, please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, token);
    }
}
