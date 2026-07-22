package com.university.lms.exception;

/**
 * Base type for all domain rule violations raised by the business/service layers
 * (e.g. borrow-limit exceeded, book unavailable, invalid fine state). Distinct from
 * unexpected/technical failures, so the presentation layer can always render a friendly,
 * actionable message rather than a stack trace.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
