package com.university.lms.exception;

/** Raised when a password-reset token is unknown, expired, or already used. */
public class InvalidResetTokenException extends AuthenticationException {

    public InvalidResetTokenException() {
        super("This password reset link is invalid or has expired. Please request a new one.");
    }
}
