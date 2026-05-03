package tn.enicarthage.qartnet.shared.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public static InvalidTokenException verificationToken() {
        return new InvalidTokenException("Invalid or expired verification token");
    }

    public static InvalidTokenException refreshToken() {
        return new InvalidTokenException("Invalid or expired refresh token");
    }
}
