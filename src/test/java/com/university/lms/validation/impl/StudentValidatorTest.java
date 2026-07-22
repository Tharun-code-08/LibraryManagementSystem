package com.university.lms.validation.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.university.lms.dto.request.StudentRegistrationRequestDTO;
import com.university.lms.validation.ValidationResult;

class StudentValidatorTest {

    private final StudentValidator validator = new StudentValidator();

    @Test
    void acceptsValidNewStudent() {
        StudentRegistrationRequestDTO request = StudentRegistrationRequestDTO.builder()
                .username("jdoe")
                .email("jdoe@university.edu")
                .temporaryPassword("Temp@1234")
                .studentId("STU1001")
                .rollNumber("R1001")
                .branchId(1L)
                .build();

        assertTrue(validator.validate(request).isValid());
    }

    @Test
    void rejectsMissingTemporaryPasswordOnCreate() {
        StudentRegistrationRequestDTO request = StudentRegistrationRequestDTO.builder()
                .username("jdoe")
                .email("jdoe@university.edu")
                .studentId("STU1001")
                .rollNumber("R1001")
                .branchId(1L)
                .build();

        assertFalse(validator.validate(request).isValid());
    }

    @Test
    void allowsMissingTemporaryPasswordOnUpdate() {
        StudentRegistrationRequestDTO request = StudentRegistrationRequestDTO.builder()
                .id(5L)
                .username("jdoe")
                .email("jdoe@university.edu")
                .studentId("STU1001")
                .rollNumber("R1001")
                .branchId(1L)
                .build();

        ValidationResult result = validator.validate(request);
        assertTrue(result.isValid());
    }

    @Test
    void rejectsMissingBranch() {
        StudentRegistrationRequestDTO request = StudentRegistrationRequestDTO.builder()
                .username("jdoe")
                .email("jdoe@university.edu")
                .temporaryPassword("Temp@1234")
                .studentId("STU1001")
                .rollNumber("R1001")
                .build();

        assertFalse(validator.validate(request).isValid());
    }
}
