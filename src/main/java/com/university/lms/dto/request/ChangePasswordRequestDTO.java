package com.university.lms.dto.request;

/** Input to {@code AuthService.changePassword}. */
public final class ChangePasswordRequestDTO {

    private final Long userId;
    private final String currentPassword;
    private final String newPassword;
    private final String confirmNewPassword;

    public ChangePasswordRequestDTO(Long userId, String currentPassword, String newPassword, String confirmNewPassword) {
        this.userId = userId;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }
}
