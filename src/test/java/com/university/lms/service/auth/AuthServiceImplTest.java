package com.university.lms.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.request.ChangePasswordRequestDTO;
import com.university.lms.dto.request.LoginRequestDTO;
import com.university.lms.dto.response.AuthResultDTO;
import com.university.lms.entity.Permission;
import com.university.lms.entity.Role;
import com.university.lms.entity.User;
import com.university.lms.entity.UserSession;
import com.university.lms.entity.UserStatus;
import com.university.lms.exception.AccountLockedException;
import com.university.lms.exception.InvalidCredentialsException;
import com.university.lms.repository.PasswordResetTokenRepository;
import com.university.lms.repository.SessionRepository;
import com.university.lms.repository.UserRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.security.PasswordEncoder;
import com.university.lms.security.SessionManager;
import com.university.lms.service.auth.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    private AuthContext authContext;
    private AuthServiceImpl authService;
    private User activeUser;

    @BeforeEach
    void setUp() {
        authContext = new AuthContext();
        SessionManager sessionManager = new SessionManager(sessionRepository, 15);
        authService = new AuthServiceImpl(
                userRepository, passwordResetTokenRepository, sessionManager,
                passwordEncoder, authContext, auditLogService);

        activeUser = new User("jdoe", "jdoe@library.local", "hashed-password", null);
        Role librarianRole = new Role("LIBRARIAN", "Library staff");
        librarianRole.getPermissions().add(new Permission("BOOK_MANAGE", "Manage books"));
        activeUser.getRoles().add(librarianRole);
    }

    @Test
    void loginSucceedsForActiveUserWithCorrectPassword() {
        when(userRepository.findByUsernameOrEmail("jdoe")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(sessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginRequestDTO request = LoginRequestDTO.builder()
                .usernameOrEmail("jdoe")
                .password("correct-password")
                .rememberMe(false)
                .build();

        AuthResultDTO result = authService.login(request, "127.0.0.1");

        assertEquals("jdoe", result.getUser().getUsername());
        assertTrue(result.getUser().getPermissions().contains("BOOK_MANAGE"));
        assertTrue(authContext.isAuthenticated());
        verify(auditLogService).log(activeUser.getId(), "LOGIN_SUCCESS", "User", activeUser.getId());
    }

    @Test
    void loginFailsWithInvalidCredentialsForWrongPassword() {
        when(userRepository.findByUsernameOrEmail("jdoe")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        LoginRequestDTO request = LoginRequestDTO.builder()
                .usernameOrEmail("jdoe")
                .password("wrong-password")
                .build();

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request, "127.0.0.1"));
        verify(auditLogService).log(activeUser.getId(), "LOGIN_FAILURE", "User", activeUser.getId());
    }

    @Test
    void loginFailsForUnknownUser() {
        when(userRepository.findByUsernameOrEmail("ghost")).thenReturn(Optional.empty());

        LoginRequestDTO request = LoginRequestDTO.builder()
                .usernameOrEmail("ghost")
                .password("whatever")
                .build();

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request, "127.0.0.1"));
    }

    @Test
    void loginFailsForLockedAccount() {
        activeUser.setStatus(UserStatus.LOCKED);
        when(userRepository.findByUsernameOrEmail("jdoe")).thenReturn(Optional.of(activeUser));

        LoginRequestDTO request = LoginRequestDTO.builder()
                .usernameOrEmail("jdoe")
                .password("correct-password")
                .build();

        assertThrows(AccountLockedException.class, () -> authService.login(request, "127.0.0.1"));
    }

    @Test
    void changePasswordFailsWhenCurrentPasswordIsWrong() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong-current", "hashed-password")).thenReturn(false);

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(1L, "wrong-current", "NewPass1", "NewPass1");

        assertThrows(InvalidCredentialsException.class, () -> authService.changePassword(request));
    }
}
