package com.university.lms.exception;

/** Raised when a scan or completion is attempted against an audit that isn't IN_PROGRESS. */
public class InvalidInventoryAuditStateException extends BusinessException {

    public InvalidInventoryAuditStateException(String message) {
        super(message);
    }
}
