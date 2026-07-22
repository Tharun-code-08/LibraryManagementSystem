package com.university.lms.exception;

/** Raised when an authenticated user attempts an action their role does not grant. */
public class AuthorizationException extends BusinessException {

    public AuthorizationException(String permissionCode) {
        super("You do not have permission to perform this action.");
    }
}
