package com.university.lms.security;

import java.util.prefs.Preferences;

/**
 * Persists an extended-lifetime session token in the OS user-preferences store so "Remember Me"
 * survives an application restart. Holds only an opaque session token (never a password), which
 * is still validated server-side (session expiry/revocation) on every use.
 */
public final class RememberMeStore {

    private static final String PREF_NODE = "com.university.lms";
    private static final String TOKEN_KEY = "remember_me_token";

    private final Preferences preferences = Preferences.userRoot().node(PREF_NODE);

    public void save(String sessionToken) {
        preferences.put(TOKEN_KEY, sessionToken);
    }

    public String load() {
        return preferences.get(TOKEN_KEY, null);
    }

    public void clear() {
        preferences.remove(TOKEN_KEY);
    }
}
