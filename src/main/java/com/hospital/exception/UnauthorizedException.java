package com.hospital.exception;

/**
 * Thrown when an unauthorized user attempts to perform a restricted role action.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
