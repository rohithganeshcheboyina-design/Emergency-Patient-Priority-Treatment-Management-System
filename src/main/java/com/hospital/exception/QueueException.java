package com.hospital.exception;

/**
 * Thrown when queue operations fail or encounter invalid state.
 */
public class QueueException extends RuntimeException {
    public QueueException(String message) {
        super(message);
    }

    public QueueException(String message, Throwable cause) {
        super(message, cause);
    }
}
