package com.university.lms.exception;

/** Raised when a payment or waive is attempted against a fine that is already PAID or WAIVED. */
public class FineAlreadySettledException extends BusinessException {

    public FineAlreadySettledException() {
        super("This fine has already been settled.");
    }
}
