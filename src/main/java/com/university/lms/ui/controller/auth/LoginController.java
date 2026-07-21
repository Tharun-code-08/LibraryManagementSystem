package com.university.lms.ui.controller.auth;

import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.LoginRequestDTO;
import com.university.lms.dto.response.AuthResultDTO;
import com.university.lms.exception.BusinessException;

/**
 * Controller for the Login screen. Attempts a "Remember Me" auto-login on load; otherwise
 * collects credentials and calls {@code AuthService.login} off the FX thread via
 * {@code AsyncExecutor}, routing to the authenticated shell on success.
 */
public final class LoginController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    public LoginController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        errorLabel.setText("");
        attemptRememberedSession();
    }

    private void attemptRememberedSession() {
        String rememberedToken = appContext.getRememberMeStore().load();
        if (rememberedToken == null) {
            return;
        }
        appContext.getAsyncExecutor().run(
                () -> appContext.getAuthService().resumeSession(rememberedToken),
                userDtoOptional -> {
                    if (userDtoOptional.isPresent()) {
                        navigateToShell();
                    } else {
                        appContext.getRememberMeStore().clear();
                    }
                },
                throwable -> appContext.getRememberMeStore().clear());
    }

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        loginButton.setDisable(true);

        LoginRequestDTO request = LoginRequestDTO.builder()
                .usernameOrEmail(usernameField.getText())
                .password(passwordField.getText())
                .rememberMe(rememberMeCheckBox.isSelected())
                .build();

        appContext.getAsyncExecutor().run(
                () -> appContext.getAuthService().login(request, resolveClientAddress()),
                this::onLoginSuccess,
                this::onLoginFailure);
    }

    private void onLoginSuccess(AuthResultDTO result) {
        loginButton.setDisable(false);
        if (rememberMeCheckBox.isSelected()) {
            appContext.getRememberMeStore().save(result.getSessionToken());
        }
        navigateToShell();
    }

    private void onLoginFailure(Throwable throwable) {
        loginButton.setDisable(false);
        String message = throwable instanceof BusinessException
                ? throwable.getMessage()
                : "Unable to sign in right now. Please try again.";
        errorLabel.setText(message);
    }

    private void navigateToShell() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }

    @FXML
    private void onForgotPassword() {
        appContext.getViewNavigator().navigate("/fxml/auth/ForgotPassword.fxml");
    }

    private String resolveClientAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
