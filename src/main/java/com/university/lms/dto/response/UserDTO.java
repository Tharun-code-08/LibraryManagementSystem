package com.university.lms.dto.response;

import java.util.Set;

/** Presentation-safe projection of {@code User} — never carries the password hash. */
public final class UserDTO {

    private final Long id;
    private final String username;
    private final String email;
    private final Set<String> roles;
    private final Set<String> permissions;

    private UserDTO(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.roles = builder.roles;
        this.permissions = builder.permissions;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public boolean hasRole(String roleName) {
        return roles.contains(roleName);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private String username;
        private String email;
        private Set<String> roles;
        private Set<String> permissions;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder permissions(Set<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public UserDTO build() {
            return new UserDTO(this);
        }
    }
}
