package com.university.lms.exception;

/** Raised when a user account's status is {@code LOCKED} or {@code DISABLED}. */
public class AccountLockedException extends AuthenticationException {

    public AccountLockedException() {
        super("This account is locked or disabled. Please contact your library administrator.");
    }
}
