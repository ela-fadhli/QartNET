package tn.enicarthage.qartnet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.enicarthage.qartnet.service.IEmailService;

@Service
@ConditionalOnMissingBean(JavaMailSender.class)
public class LogEmailServiceImpl implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(LogEmailServiceImpl.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        log.warn("Mail not configured — password reset link for {}: {}", toEmail, resetLink);
    }
}
