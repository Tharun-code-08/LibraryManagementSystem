package com.university.lms.ui.controller.auth;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.ResetPasswordRequestDTO;
import com.university.lms.exception.BusinessException;

/** Completes a Forgot Password flow: token + new password → active account with new credentials. */
public final class ResetPasswordController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TextField tokenField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button submitButton;

    public ResetPasswordController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageLabel.setText("");
    }

    @FXML
    private void onSubmit() {
        submitButton.setDisable(true);
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO(
                tokenField.getText(), newPasswordField.getText(), confirmPasswordField.getText());

        appContext.getAsyncExecutor().run(
                () -> {
                    appContext.getAuthService().resetPassword(request);
                    return null;
                },
                ignored -> {
                    submitButton.setDisable(false);
                    messageLabel.setText("Password reset. You can now log in with your new password.");
                },
                throwable -> {
                    submitButton.setDisable(false);
                    messageLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage()
                            : "Unable to reset password right now. Please try again.");
                });
    }

    @FXML
    private void onBackToLogin() {
        appContext.getViewNavigator().navigate("/fxml/auth/Login.fxml");
    }
}
