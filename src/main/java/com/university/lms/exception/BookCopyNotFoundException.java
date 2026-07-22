package com.university.lms.exception;

/** Raised when a scanned barcode does not match any known book copy. */
public class BookCopyNotFoundException extends BusinessException {

    public BookCopyNotFoundException(String barcode) {
        super("No book copy found with barcode " + barcode + ".");
    }
}
