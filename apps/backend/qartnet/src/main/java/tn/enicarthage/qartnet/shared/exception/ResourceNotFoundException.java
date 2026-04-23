package tn.enicarthage.qartnet.shared.exception;

/**
 * Thrown when a requested resource does not exist.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 *
 * Usage:
 *   throw new ResourceNotFoundException("User not found with id: " + id);
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException(resource + " not found with id: " + id);
    }
}

