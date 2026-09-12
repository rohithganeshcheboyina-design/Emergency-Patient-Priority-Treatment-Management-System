package com.hospital.exception;

/**
 * Thrown when user credentials or session verification fails.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
