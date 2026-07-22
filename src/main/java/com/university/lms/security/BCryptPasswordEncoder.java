package com.university.lms.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

/** BCrypt-backed {@link PasswordEncoder}, cost factor 12 per the NFR-3 security requirement. */
public final class BCryptPasswordEncoder implements PasswordEncoder {

    private static final int COST_FACTOR = 12;

    @Override
    public String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(COST_FACTOR, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword).verified;
    }
}
