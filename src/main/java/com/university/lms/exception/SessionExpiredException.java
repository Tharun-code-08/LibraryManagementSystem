package com.university.lms.exception;

/** Raised when an operation is attempted with an expired or revoked session token. */
public class SessionExpiredException extends AuthenticationException {

    public SessionExpiredException() {
        super("Your session has expired. Please log in again.");
    }
}
