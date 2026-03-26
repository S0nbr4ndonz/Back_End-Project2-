package com.group7.jobTrackerApplication.exception;

/**
 * Signals that a requested application resource could not be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Creates a new resource-not-found exception with a human-readable message.
     *
     * @param message error message returned to the caller
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
