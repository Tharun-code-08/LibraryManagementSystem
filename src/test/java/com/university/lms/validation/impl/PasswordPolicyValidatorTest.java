package com.university.lms.validation.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.university.lms.validation.ValidationResult;

class PasswordPolicyValidatorTest {

    private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

    @Test
    void acceptsAStrongMatchingPassword() {
        ValidationResult result = validator.validate(new PasswordPolicyValidator.Input("Str0ngPass", "Str0ngPass"));
        assertTrue(result.isValid());
    }

    @Test
    void rejectsMismatchedConfirmation() {
        ValidationResult result = validator.validate(new PasswordPolicyValidator.Input("Str0ngPass", "Different1"));
        assertFalse(result.isValid());
    }

    @Test
    void rejectsPasswordShorterThanMinimumLength() {
        ValidationResult result = validator.validate(new PasswordPolicyValidator.Input("Sh0rt", "Sh0rt"));
        assertFalse(result.isValid());
    }

    @Test
    void rejectsPasswordMissingComplexityRequirements() {
        ValidationResult result = validator.validate(new PasswordPolicyValidator.Input("alllowercase1", "alllowercase1"));
        assertFalse(result.isValid());
    }
}
