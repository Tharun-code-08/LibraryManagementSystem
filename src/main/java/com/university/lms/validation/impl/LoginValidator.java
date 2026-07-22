package com.university.lms.validation.impl;

import com.university.lms.dto.request.LoginRequestDTO;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.Validator;

/** Presence checks on login input, run before any credential lookup. */
public final class LoginValidator implements Validator<LoginRequestDTO> {

    @Override
    public ValidationResult validate(LoginRequestDTO input) {
        ValidationResult result = new ValidationResult();
        if (input.getUsernameOrEmail() == null || input.getUsernameOrEmail().isBlank()) {
            result.addError("Username or email is required.");
        }
        if (input.getPassword() == null || input.getPassword().isEmpty()) {
            result.addError("Password is required.");
        }
        return result;
    }
}
