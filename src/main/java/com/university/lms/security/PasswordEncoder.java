package com.university.lms.security;

/** Abstraction over the password hashing algorithm so the rest of the app never touches BCrypt directly. */
public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
