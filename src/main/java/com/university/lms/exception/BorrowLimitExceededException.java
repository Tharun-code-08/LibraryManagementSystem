package com.university.lms.exception;

/** Raised when a member has already reached their membership type's maximum concurrent borrows. */
public class BorrowLimitExceededException extends BusinessException {

    public BorrowLimitExceededException(int maxBorrowLimit) {
        super("Borrow limit reached (" + maxBorrowLimit + " books). Please return a book before borrowing another.");
    }
}
