package com.university.lms.service.auth;

import java.util.Optional;

import com.university.lms.dto.request.ChangePasswordRequestDTO;
import com.university.lms.dto.request.LoginRequestDTO;
import com.university.lms.dto.request.ResetPasswordRequestDTO;
import com.university.lms.dto.response.AuthResultDTO;
import com.university.lms.dto.response.UserDTO;

/**
 * Use-case-oriented authentication API. Every method here is the single entry point for its
 * flow — controllers never touch {@code UserRepository}/{@code SessionManager} directly.
 */
public interface AuthService {

    AuthResultDTO login(LoginRequestDTO request, String ipAddress);

    /** Re-validates a previously issued session token (e.g. a "Remember Me" token on startup). */
    Optional<UserDTO> resumeSession(String sessionToken);

    void logout(String sessionToken);

    void changePassword(ChangePasswordRequestDTO request);

    /**
     * Issues a password-reset token for the given email if an account exists, without
     * revealing whether it does (returns empty either way to the caller's UI message).
     * The token itself is returned here only because the Email module (Phase 9) does not
     * exist yet; once it does, this becomes fire-and-forget and the token is emailed instead.
     */
    Optional<String> initiatePasswordReset(String email);

    void resetPassword(ResetPasswordRequestDTO request);
}
