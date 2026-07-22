package com.university.lms.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Accumulates validation error messages for a single form/DTO submission. */
public final class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public void addError(String message) {
        errors.add(message);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
