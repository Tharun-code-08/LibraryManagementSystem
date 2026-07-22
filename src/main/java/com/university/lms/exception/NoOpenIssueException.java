package com.university.lms.exception;

/** Raised when a return is attempted for a copy with no currently open issue. */
public class NoOpenIssueException extends BusinessException {

    public NoOpenIssueException(String barcode) {
        super("No open issue found for book copy " + barcode + ".");
    }
}
