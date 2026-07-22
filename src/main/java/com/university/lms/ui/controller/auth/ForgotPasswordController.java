package com.university.lms.ui.controller.auth;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import com.university.lms.config.AppContext;

/**
 * Forgot Password screen. Always shows the same generic confirmation message regardless of
 * whether the email exists, to avoid leaking account existence. The reset token itself is
 * surfaced directly in the UI as a development affordance until the Email module (Phase 9)
 * exists to deliver it out-of-band instead.
 */
public final class ForgotPasswordController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    @FXML
    private Label devTokenLabel;

    @FXML
    private Button submitButton;

    public ForgotPasswordController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageLabel.setText("");
        devTokenLabel.setText("");
    }

    @FXML
    private void onSubmit() {
        submitButton.setDisable(true);
        String email = emailField.getText();

        appContext.getAsyncExecutor().run(
                () -> appContext.getAuthService().initiatePasswordReset(email),
                tokenOptional -> {
                    submitButton.setDisable(false);
                    messageLabel.setText("If an account exists for that email, password reset "
                            + "instructions have been generated.");
                    tokenOptional.ifPresent(token -> devTokenLabel.setText("Dev token (until email delivery ships): " + token));
                },
                throwable -> {
                    submitButton.setDisable(false);
                    messageLabel.setText("If an account exists for that email, password reset "
                            + "instructions have been generated.");
                });
    }

    @FXML
    private void onBackToLogin() {
        appContext.getViewNavigator().navigate("/fxml/auth/Login.fxml");
    }

    @FXML
    private void onHaveResetCode() {
        appContext.getViewNavigator().navigate("/fxml/auth/ResetPassword.fxml");
    }
}
