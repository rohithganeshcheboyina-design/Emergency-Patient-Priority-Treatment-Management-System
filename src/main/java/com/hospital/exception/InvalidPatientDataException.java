package com.hospital.exception;

/**
 * Thrown when patient registration or modification fields violate validation rules.
 */
public class InvalidPatientDataException extends Exception {
    public InvalidPatientDataException(String message) {
        super(message);
    }

    public InvalidPatientDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
