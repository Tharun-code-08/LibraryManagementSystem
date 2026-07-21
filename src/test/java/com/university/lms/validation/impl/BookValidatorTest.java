package com.university.lms.validation.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.university.lms.dto.request.BookRequestDTO;
import com.university.lms.validation.ValidationResult;

class BookValidatorTest {

    private final BookValidator validator = new BookValidator();

    @Test
    void acceptsValidBook() {
        BookRequestDTO request = BookRequestDTO.builder()
                .isbn("978-3-16-148410-0")
                .title("Clean Code")
                .cost(BigDecimal.TEN)
                .build();
        ValidationResult result = validator.validate(request);
        assertTrue(result.isValid());
    }

    @Test
    void rejectsMissingTitle() {
        BookRequestDTO request = BookRequestDTO.builder().isbn("978-3-16-148410-0").build();
        assertFalse(validator.validate(request).isValid());
    }

    @Test
    void rejectsInvalidIsbn() {
        BookRequestDTO request = BookRequestDTO.builder().isbn("not-an-isbn").title("Some Book").build();
        assertFalse(validator.validate(request).isValid());
    }

    @Test
    void rejectsNegativeCost() {
        BookRequestDTO request = BookRequestDTO.builder()
                .isbn("978-3-16-148410-0")
                .title("Some Book")
                .cost(BigDecimal.valueOf(-5))
                .build();
        assertFalse(validator.validate(request).isValid());
    }
}
