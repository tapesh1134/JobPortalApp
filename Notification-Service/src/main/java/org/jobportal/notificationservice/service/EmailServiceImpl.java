package org.jobportal.notificationservice.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendEmail(String from, String to, String message) {
        try {
            // 🔹 HTML Template
            String content = """
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f8; padding:20px; font-family:Arial;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="20" cellspacing="0" style="background-color:#ffffff; border-radius:8px;">
                            <tr>
                                <td align="center">
                                    <h2 style="color:#2c3e50;">Job Portal Notification</h2>
                                    <p style="font-size:16px; color:#555;">
                                     %s
                                    </p>
                                    <p style="font-size:14px; color:#777; margin-top:20px;">
                                        This is an automated message from Job Portal system.
                                    </p>
                                    <p style="font-size:12px; color:#aaa;">
                                        Please do not reply.
                                    </p>
                                   </td>
                            </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            """.formatted(message);

            // 🔹 Create Email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(from); // ⚠️ must match SMTP auth email
            helper.setTo(to);
            helper.setSubject("Job Portal Notification");
            helper.setText(content, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }
}
