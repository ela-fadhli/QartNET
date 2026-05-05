package tn.enicarthage.qartnet.shared.exception;

/**
 * Thrown when a call to the upstream LLM provider fails or returns an
 * unusable response. Mapped to HTTP 503 by GlobalExceptionHandler.
 */
public class LlmUnavailableException extends RuntimeException {

    public LlmUnavailableException(String message) {
        super(message);
    }
}
