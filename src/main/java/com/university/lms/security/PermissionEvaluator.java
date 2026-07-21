package com.university.lms.security;

import com.university.lms.exception.AuthorizationException;

/**
 * Service-layer authorization gate. Every service method that mutates state (or exposes
 * sensitive data) checks a required permission code here first — this is the defense-in-depth
 * layer that holds even if a UI screen forgets to hide a button for a given role.
 */
public final class PermissionEvaluator {

    private final AuthContext authContext;

    public PermissionEvaluator(AuthContext authContext) {
        this.authContext = authContext;
    }

    public boolean hasPermission(String permissionCode) {
        return authContext.isAuthenticated() && authContext.getCurrentUser().getPermissions().contains(permissionCode);
    }

    public void requirePermission(String permissionCode) {
        if (!hasPermission(permissionCode)) {
            throw new AuthorizationException(permissionCode);
        }
    }
}
