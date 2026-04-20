package tn.enicarthage.qartnet.service;

public interface IEmailService {

    void sendPasswordResetEmail(String toEmail, String resetLink);
}
