package com.university.lms.validation.impl;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import com.university.lms.dto.request.BookRequestDTO;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.Validator;

/** Presence/format checks on book input, run before any persistence or duplicate-ISBN lookup. */
public final class BookValidator implements Validator<BookRequestDTO> {

    // ISBN-10 or ISBN-13, optionally hyphenated.
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(97[89])?[-]?\\d{1,5}[-]?\\d{1,7}[-]?\\d{1,7}[-]?[\\dX]$");

    @Override
    public ValidationResult validate(BookRequestDTO input) {
        ValidationResult result = new ValidationResult();

        if (input.getTitle() == null || input.getTitle().isBlank()) {
            result.addError("Title is required.");
        }
        if (input.getIsbn() == null || input.getIsbn().isBlank()) {
            result.addError("ISBN is required.");
        } else if (!ISBN_PATTERN.matcher(input.getIsbn().trim()).matches()) {
            result.addError("ISBN format is invalid.");
        }
        if (input.getCost() != null && input.getCost().compareTo(BigDecimal.ZERO) < 0) {
            result.addError("Cost cannot be negative.");
        }
        return result;
    }
}
