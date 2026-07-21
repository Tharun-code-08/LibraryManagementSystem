package com.university.lms.ui.controller.common;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.UserDTO;

/**
 * Placeholder shell shown immediately after a successful login. Confirms role-based routing and
 * client-side session-idle-timeout work end-to-end; replaced by the real sidebar/dashboard shell
 * in later phases (see docs/13-ImplementationRoadmap.md, Phase 7).
 */
public final class AuthenticatedShellController implements Initializable {

    private final AppContext appContext;
    private PauseTransition idleTimer;

    @FXML
    private Label welcomeLabel;

    @FXML
    private ToggleButton darkModeToggle;

    public AuthenticatedShellController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserDTO currentUser = appContext.getAuthContext().getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getUsername()
                + " (" + String.join(", ", currentUser.getRoles()) + ")");

        long timeoutMinutes = Long.parseLong(
                appContext.getConfigurationManager().app("app.session.idle-timeout-minutes", "15"));
        idleTimer = new PauseTransition(Duration.minutes(timeoutMinutes));
        idleTimer.setOnFinished(event -> onSessionTimeout());

        welcomeLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                attachActivityListeners(newScene);
                idleTimer.playFromStart();
            }
        });
    }

    private void attachActivityListeners(Scene scene) {
        scene.addEventFilter(javafx.scene.input.MouseEvent.ANY, e -> idleTimer.playFromStart());
        scene.addEventFilter(javafx.scene.input.KeyEvent.ANY, e -> idleTimer.playFromStart());
    }

    private void onSessionTimeout() {
        appContext.getAuthService().logout(appContext.getAuthContext().getSessionToken());
        appContext.getRememberMeStore().clear();
        appContext.getViewNavigator().navigate("/fxml/auth/Login.fxml");
    }

    @FXML
    private void onLogout() {
        if (idleTimer != null) {
            idleTimer.stop();
        }
        appContext.getAsyncExecutor().run(
                () -> {
                    appContext.getAuthService().logout(appContext.getAuthContext().getSessionToken());
                    return null;
                },
                ignored -> {
                    appContext.getRememberMeStore().clear();
                    appContext.getViewNavigator().navigate("/fxml/auth/Login.fxml");
                },
                throwable -> appContext.getViewNavigator().navigate("/fxml/auth/Login.fxml"));
    }

    @FXML
    private void onChangePassword() {
        appContext.getViewNavigator().navigate("/fxml/auth/ChangePassword.fxml");
    }

    @FXML
    private void onOpenBookCatalog() {
        appContext.getViewNavigator().navigate("/fxml/catalog/BookList.fxml");
    }

    @FXML
    private void onOpenStudents() {
        appContext.getViewNavigator().navigate("/fxml/people/StudentList.fxml");
    }

    @FXML
    private void onOpenFaculty() {
        appContext.getViewNavigator().navigate("/fxml/people/FacultyList.fxml");
    }

    @FXML
    private void onOpenMembershipTypes() {
        appContext.getViewNavigator().navigate("/fxml/people/MembershipTypeManagement.fxml");
    }

    @FXML
    private void onOpenIssue() {
        appContext.getViewNavigator().navigate("/fxml/circulation/Issue.fxml");
    }

    @FXML
    private void onOpenReturn() {
        appContext.getViewNavigator().navigate("/fxml/circulation/Return.fxml");
    }

    @FXML
    private void onOpenReservation() {
        appContext.getViewNavigator().navigate("/fxml/circulation/Reservation.fxml");
    }

    @FXML
    private void onToggleDarkMode() {
        Scene scene = darkModeToggle.getScene();
        scene.getStylesheets().removeIf(sheet -> sheet.endsWith("theme-light.css") || sheet.endsWith("theme-dark.css"));
        String theme = darkModeToggle.isSelected() ? "/css/theme-dark.css" : "/css/theme-light.css";
        scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
    }
}
