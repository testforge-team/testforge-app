package com.testforge.exception;

/**
 * Thrown when an id doesn't exist in the database
 * (e.g. GET /api/users/999 when there is no user 999).
 * GlobalExceptionHandler converts it into an HTTP 404 response.
 */
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
        super(message);
    }
}
