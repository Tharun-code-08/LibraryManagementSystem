package com.university.lms.service.auth.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.university.lms.dto.request.ChangePasswordRequestDTO;
import com.university.lms.dto.request.LoginRequestDTO;
import com.university.lms.dto.request.ResetPasswordRequestDTO;
import com.university.lms.dto.response.AuthResultDTO;
import com.university.lms.dto.response.UserDTO;
import com.university.lms.entity.PasswordResetToken;
import com.university.lms.entity.Role;
import com.university.lms.entity.User;
import com.university.lms.entity.UserSession;
import com.university.lms.entity.UserStatus;
import com.university.lms.exception.AccountLockedException;
import com.university.lms.exception.InvalidCredentialsException;
import com.university.lms.exception.InvalidResetTokenException;
import com.university.lms.exception.ValidationException;
import com.university.lms.repository.PasswordResetTokenRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PasswordEncoder;
import com.university.lms.security.SessionManager;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.auth.AuthService;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.Validator;
import com.university.lms.validation.impl.LoginValidator;
import com.university.lms.validation.impl.PasswordPolicyValidator;

public final class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SessionManager sessionManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthContext authContext;
    private final AuditLogService auditLogService;

    private final Validator<LoginRequestDTO> loginValidator = new LoginValidator();
    private final Validator<PasswordPolicyValidator.Input> passwordPolicyValidator = new PasswordPolicyValidator();
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(UserRepository userRepository,
                            PasswordResetTokenRepository passwordResetTokenRepository,
                            SessionManager sessionManager,
                            PasswordEncoder passwordEncoder,
                            AuthContext authContext,
                            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.sessionManager = sessionManager;
        this.passwordEncoder = passwordEncoder;
        this.authContext = authContext;
        this.auditLogService = auditLogService;
    }

    @Override
    public AuthResultDTO login(LoginRequestDTO request, String ipAddress) {
        ValidationResult validation = loginValidator.validate(request);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getErrors());
        }

        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountLockedException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.incrementFailedLoginAttempts();
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setStatus(UserStatus.LOCKED);
            }
            userRepository.save(user);
            auditLogService.log(user, "LOGIN_FAILURE", "User", user.getId());
            throw new InvalidCredentialsException();
        }

        user.resetFailedLoginAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        UserSession session = sessionManager.createSession(user, ipAddress, request.isRememberMe());
        UserDTO userDto = toDto(user);
        authContext.set(userDto, session.getToken());
        auditLogService.log(user, "LOGIN_SUCCESS", "User", user.getId());

        return new AuthResultDTO(userDto, session.getToken());
    }

    @Override
    public Optional<UserDTO> resumeSession(String sessionToken) {
        return sessionManager.validate(sessionToken).flatMap(session -> {
            sessionManager.touch(session);
            return userRepository.findById(session.getUser().getId()).map(user -> {
                UserDTO dto = toDto(user);
                authContext.set(dto, sessionToken);
                return dto;
            });
        });
    }

    @Override
    public void logout(String sessionToken) {
        User currentUser = authContext.isAuthenticated()
                ? userRepository.findById(authContext.getCurrentUser().getId()).orElse(null)
                : null;
        sessionManager.revoke(sessionToken);
        auditLogService.log(currentUser, "LOGOUT", "User", currentUser != null ? currentUser.getId() : null);
        authContext.clear();
    }

    @Override
    public void changePassword(ChangePasswordRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        ValidationResult validation = passwordPolicyValidator.validate(
                new PasswordPolicyValidator.Input(request.getNewPassword(), request.getConfirmNewPassword()));
        if (!validation.isValid()) {
            throw new ValidationException(validation.getErrors());
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditLogService.log(user, "PASSWORD_CHANGED", "User", user.getId());
    }

    @Override
    public Optional<String> initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        String token = generateToken();
        LocalDateTime now = LocalDateTime.now();
        passwordResetTokenRepository.save(
                new PasswordResetToken(user, token, now, now.plusMinutes(RESET_TOKEN_EXPIRY_MINUTES)));
        auditLogService.log(user, "PASSWORD_RESET_REQUESTED", "User", user.getId());
        return Optional.of(token);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .filter(PasswordResetToken::isValid)
                .orElseThrow(InvalidResetTokenException::new);

        ValidationResult validation = passwordPolicyValidator.validate(
                new PasswordPolicyValidator.Input(request.getNewPassword(), request.getConfirmNewPassword()));
        if (!validation.isValid()) {
            throw new ValidationException(validation.getErrors());
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.resetFailedLoginAttempts();
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);

        resetToken.markUsed();
        passwordResetTokenRepository.save(resetToken);
        auditLogService.log(user, "PASSWORD_RESET_COMPLETED", "User", user.getId());
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private UserDTO toDto(User user) {
        Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toUnmodifiableSet());
        Set<String> permissionCodes = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getCode())
                .collect(Collectors.toUnmodifiableSet());

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roleNames)
                .permissions(permissionCodes)
                .build();
    }
}
