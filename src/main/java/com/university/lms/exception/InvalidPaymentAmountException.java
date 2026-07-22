package com.university.lms.exception;

/** Raised when a payment amount is non-positive or exceeds the fine's remaining balance. */
public class InvalidPaymentAmountException extends BusinessException {

    public InvalidPaymentAmountException(String message) {
        super(message);
    }
}
