package com.university.lms.validation.impl;

import com.university.lms.dto.request.StudentRegistrationRequestDTO;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.Validator;

/** Presence checks on student registration/update input, run before any persistence. */
public final class StudentValidator implements Validator<StudentRegistrationRequestDTO> {

    @Override
    public ValidationResult validate(StudentRegistrationRequestDTO input) {
        ValidationResult result = new ValidationResult();

        if (input.getUsername() == null || input.getUsername().isBlank()) {
            result.addError("Username is required.");
        }
        if (input.getEmail() == null || input.getEmail().isBlank()) {
            result.addError("Email is required.");
        }
        if (input.getStudentId() == null || input.getStudentId().isBlank()) {
            result.addError("Student ID is required.");
        }
        if (input.getRollNumber() == null || input.getRollNumber().isBlank()) {
            result.addError("Roll number is required.");
        }
        if (input.getBranchId() == null) {
            result.addError("Branch is required.");
        }
        if (input.getId() == null && (input.getTemporaryPassword() == null || input.getTemporaryPassword().isBlank())) {
            result.addError("A temporary password is required for new students.");
        }
        return result;
    }
}
