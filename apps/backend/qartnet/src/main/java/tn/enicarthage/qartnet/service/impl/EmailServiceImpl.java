package tn.enicarthage.qartnet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.enicarthage.qartnet.service.IEmailService;

@Service
@ConditionalOnBean(JavaMailSender.class)
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("QartNET — Reset your password");
        message.setText(
                "Hello,\n\n" +
                "You requested a password reset for your QartNET account.\n\n" +
                "Click the link below to set a new password (valid for 1 hour):\n" +
                resetLink + "\n\n" +
                "If you did not request this, you can safely ignore this email.\n\n" +
                "— The QartNET Team"
        );
        mailSender.send(message);
    }

    @Override
    public void sendEmailVerification(String toEmail, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("QartNET — Verify your email address");
        message.setText(
                "Welcome to QartNET!\n\n" +
                "Please verify your email address by clicking the link below (valid for 24 hours):\n" +
                verificationLink + "\n\n" +
                "If you did not create an account, you can safely ignore this email.\n\n" +
                "— The QartNET Team"
        );
        mailSender.send(message);
    }
}
