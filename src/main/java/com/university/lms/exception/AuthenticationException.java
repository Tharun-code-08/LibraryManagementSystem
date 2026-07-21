package com.university.lms.exception;

/** Base type for all login/session failures. */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message);
    }
}
