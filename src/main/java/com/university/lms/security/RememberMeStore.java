package com.university.lms.security;

import java.util.prefs.Preferences;

/**
 * Persists an extended-lifetime session token in the OS user-preferences store so "Remember Me"
 * survives an application restart. Holds only an opaque session token (never a password), which
 * is still validated server-side (session expiry/revocation) on every use.
 *
 * <p>The token is stored as-is (not further encrypted) because it must be presented to
 * {@code SessionManager} verbatim on every resume — this is the same bearer-token tradeoff every
 * "remember me" cookie makes. What limits the blast radius of a leaked token: the server only
 * ever stores and compares its SHA-256 hash (see {@link SessionManager}), so a database leak
 * can't be replayed here, and the token is revocable/expirable at any time. This store is only as
 * safe as the OS user-preferences backing file, which is scoped to the local OS user account.
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
