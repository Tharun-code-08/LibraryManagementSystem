package com.university.lms.business;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.university.lms.entity.MembershipType;
import com.university.lms.exception.BorrowLimitExceededException;

class BorrowLimitValidatorTest {

    private final BorrowLimitValidator validator = new BorrowLimitValidator();

    @Test
    void allowsBorrowingBelowTheLimit() {
        MembershipType type = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);
        assertDoesNotThrow(() -> validator.validate(type, 2));
    }

    @Test
    void rejectsBorrowingAtTheLimit() {
        MembershipType type = new MembershipType("STUDENT_STANDARD", 3, 14, BigDecimal.valueOf(5), 1, 2);
        assertThrows(BorrowLimitExceededException.class, () -> validator.validate(type, 3));
    }
}
