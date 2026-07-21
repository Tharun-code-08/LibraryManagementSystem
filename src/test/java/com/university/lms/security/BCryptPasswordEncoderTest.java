package com.university.lms.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordEncoderTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void encodedPasswordIsNotThePlainTextValue() {
        String encoded = encoder.encode("Admin@123");
        assertNotEquals("Admin@123", encoded);
    }

    @Test
    void matchesReturnsTrueForCorrectPassword() {
        String encoded = encoder.encode("Admin@123");
        assertTrue(encoder.matches("Admin@123", encoded));
    }

    @Test
    void matchesReturnsFalseForIncorrectPassword() {
        String encoded = encoder.encode("Admin@123");
        assertFalse(encoder.matches("WrongPassword", encoded));
    }
}
