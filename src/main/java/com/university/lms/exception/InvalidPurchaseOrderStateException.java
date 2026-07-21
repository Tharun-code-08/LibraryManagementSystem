package com.university.lms.exception;

/** Raised when a purchase-order transition (submit/approve/cancel/receive) doesn't apply to its current status. */
public class InvalidPurchaseOrderStateException extends BusinessException {

    public InvalidPurchaseOrderStateException(String message) {
        super(message);
    }
}
