package com.university.lms.security;

import com.university.lms.dto.response.UserDTO;

/**
 * Holds the single currently-authenticated user for this desktop client instance. Read by
 * {@link PermissionEvaluator} and every screen that needs to know "who is logged in right now."
 * Cleared on logout or session expiry.
 */
public final class AuthContext {

    private volatile UserDTO currentUser;
    private volatile String sessionToken;

    public void set(UserDTO user, String sessionToken) {
        this.currentUser = user;
        this.sessionToken = sessionToken;
    }

    public void clear() {
        this.currentUser = null;
        this.sessionToken = null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public UserDTO getCurrentUser() {
        return currentUser;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
