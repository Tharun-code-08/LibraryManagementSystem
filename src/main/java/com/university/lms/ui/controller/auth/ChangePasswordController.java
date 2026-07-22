package com.university.lms.ui.controller.auth;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.ChangePasswordRequestDTO;
import com.university.lms.exception.BusinessException;

/** Change Password screen, reachable from the authenticated shell's profile menu. */
public final class ChangePasswordController implements Initializable {

    private final AppContext appContext;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button submitButton;

    public ChangePasswordController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        messageLabel.setText("");
    }

    @FXML
    private void onSubmit() {
        submitButton.setDisable(true);
        Long userId = appContext.getAuthContext().getCurrentUser().getId();
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO(
                userId, currentPasswordField.getText(), newPasswordField.getText(), confirmPasswordField.getText());

        appContext.getAsyncExecutor().run(
                () -> {
                    appContext.getAuthService().changePassword(request);
                    return null;
                },
                ignored -> {
                    submitButton.setDisable(false);
                    messageLabel.setText("Password updated successfully.");
                },
                throwable -> {
                    submitButton.setDisable(false);
                    messageLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage()
                            : "Unable to update password right now. Please try again.");
                });
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
