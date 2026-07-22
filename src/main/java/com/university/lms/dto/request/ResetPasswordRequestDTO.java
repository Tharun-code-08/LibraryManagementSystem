package com.university.lms.dto.request;

/** Input to {@code AuthService.resetPassword} — completes a Forgot Password flow via token. */
public final class ResetPasswordRequestDTO {

    private final String token;
    private final String newPassword;
    private final String confirmNewPassword;

    public ResetPasswordRequestDTO(String token, String newPassword, String confirmNewPassword) {
        this.token = token;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }

    public String getToken() {
        return token;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }
}
