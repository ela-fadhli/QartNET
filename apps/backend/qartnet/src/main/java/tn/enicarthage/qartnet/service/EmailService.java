package tn.enicarthage.qartnet.service;

public interface EmailService {

    void sendPasswordResetEmail(String toEmail, String resetLink);
}
