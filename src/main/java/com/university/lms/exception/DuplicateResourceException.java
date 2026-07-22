package com.university.lms.exception;

/** Raised when a create/update would violate a uniqueness rule (ISBN, barcode, etc.). */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
