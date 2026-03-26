package com.group7.jobTrackerApplication.exception;

/**
 * Signals that the current request requires authentication but no valid authenticated user was available.
 */
public class NotAuthenticatedException extends RuntimeException {
    /**
     * Creates a new not-authenticated exception with a human-readable message.
     *
     * @param message error message returned to the caller
     */
    public NotAuthenticatedException(String message) {
        super(message);
    }
}
