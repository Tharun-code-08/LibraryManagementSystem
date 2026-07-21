package com.university.lms.dto.response;

/** Result of a successful {@code AuthService.login} call. */
public final class AuthResultDTO {

    private final UserDTO user;
    private final String sessionToken;

    public AuthResultDTO(UserDTO user, String sessionToken) {
        this.user = user;
        this.sessionToken = sessionToken;
    }

    public UserDTO getUser() {
        return user;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}
