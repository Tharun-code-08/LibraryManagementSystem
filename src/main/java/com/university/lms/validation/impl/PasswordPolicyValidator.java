package com.university.lms.validation.impl;

import java.util.regex.Pattern;

import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.Validator;

/**
 * Enforces the organization's password complexity policy and confirms the "new" +
 * "confirm new" fields match. Used by both the Change Password and Reset Password flows.
 */
public final class PasswordPolicyValidator implements Validator<PasswordPolicyValidator.Input> {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");

    @Override
    public ValidationResult validate(Input input) {
        ValidationResult result = new ValidationResult();
        String newPassword = input.newPassword();
        String confirmPassword = input.confirmPassword();

        if (newPassword == null || newPassword.length() < MIN_LENGTH) {
            result.addError("Password must be at least " + MIN_LENGTH + " characters long.");
        } else {
            if (!UPPERCASE.matcher(newPassword).find()) {
                result.addError("Password must contain at least one uppercase letter.");
            }
            if (!LOWERCASE.matcher(newPassword).find()) {
                result.addError("Password must contain at least one lowercase letter.");
            }
            if (!DIGIT.matcher(newPassword).find()) {
                result.addError("Password must contain at least one digit.");
            }
        }

        if (newPassword != null && !newPassword.equals(confirmPassword)) {
            result.addError("Password confirmation does not match.");
        }
        return result;
    }

    public record Input(String newPassword, String confirmPassword) {
    }
}
