package com.university.lms.exception;

import java.util.List;

/** Raised when a form/DTO fails validation before reaching the business layer. */
public class ValidationException extends BusinessException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join(" ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
