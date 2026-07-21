package com.university.lms.dto.request;

/** Input to {@code AuthService.login}. */
public final class LoginRequestDTO {

    private final String usernameOrEmail;
    private final String password;
    private final boolean rememberMe;

    private LoginRequestDTO(Builder builder) {
        this.usernameOrEmail = builder.usernameOrEmail;
        this.password = builder.password;
        this.rememberMe = builder.rememberMe;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String usernameOrEmail;
        private String password;
        private boolean rememberMe;

        public Builder usernameOrEmail(String usernameOrEmail) {
            this.usernameOrEmail = usernameOrEmail;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder rememberMe(boolean rememberMe) {
            this.rememberMe = rememberMe;
            return this;
        }

        public LoginRequestDTO build() {
            return new LoginRequestDTO(this);
        }
    }
}
