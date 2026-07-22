package com.university.lms.exception;

/** Raised when an issue is attempted against a book copy that is not currently AVAILABLE. */
public class BookNotAvailableException extends BusinessException {

    public BookNotAvailableException(String barcode) {
        super("This book copy (" + barcode + ") is not available for issue.");
    }
}
