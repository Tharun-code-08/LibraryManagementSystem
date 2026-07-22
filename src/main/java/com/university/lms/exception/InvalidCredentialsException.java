package com.university.lms.exception;

/** Raised when a username/email + password combination does not match any active user. */
public class InvalidCredentialsException extends AuthenticationException {

    public InvalidCredentialsException() {
        super("Invalid username or password.");
    }
}
