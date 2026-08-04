package com.testforge.exception;

/**
 * Thrown when an id doesn't exist in the database
 * (e.g. GET /api/users/999 when there is no user 999).
 * GlobalExceptionHandler converts it into an HTTP 404 response.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
