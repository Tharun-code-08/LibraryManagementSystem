package com.university.lms.validation.impl;

import com.university.lms.dto.request.FacultyRegistrationRequestDTO;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.Validator;

/** Presence checks on faculty registration/update input, run before any persistence. */
public final class FacultyValidator implements Validator<FacultyRegistrationRequestDTO> {

    @Override
    public ValidationResult validate(FacultyRegistrationRequestDTO input) {
        ValidationResult result = new ValidationResult();

        if (input.getUsername() == null || input.getUsername().isBlank()) {
            result.addError("Username is required.");
        }
        if (input.getEmail() == null || input.getEmail().isBlank()) {
            result.addError("Email is required.");
        }
        if (input.getFacultyId() == null || input.getFacultyId().isBlank()) {
            result.addError("Faculty ID is required.");
        }
        if (input.getId() == null && (input.getTemporaryPassword() == null || input.getTemporaryPassword().isBlank())) {
            result.addError("A temporary password is required for new faculty.");
        }
        return result;
    }
}
