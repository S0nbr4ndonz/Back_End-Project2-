package com.group7.jobTrackerApplication.exception;

/**
 * Signals that the current user is authenticated but not authorized to perform the requested action.
 */
public class ForbiddenException extends RuntimeException {
    /**
     * Creates a new forbidden exception with a human-readable message.
     *
     * @param message error message returned to the caller
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
