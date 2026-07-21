package com.university.lms.ui.controller.common;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.Scene;

import com.university.lms.config.AppContext;

/**
 * Controller for the Phase-0 bootstrap shell: confirms the composition root, database
 * connectivity (via a successful Flyway migration at startup), and theme switching all work
 * end-to-end. Replaced by the real role-based shells (Login → Dashboard) starting in Phase 1.
 */
public final class MainShellController implements Initializable {

    private static final String LIGHT_THEME = "/css/theme-light.css";
    private static final String DARK_THEME = "/css/theme-dark.css";

    private final AppContext appContext;

    @FXML
    private Label statusLabel;

    @FXML
    private ToggleButton darkModeToggle;

    public MainShellController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String appName = appContext.getConfigurationManager().app("app.name");
        String appVersion = appContext.getConfigurationManager().app("app.version");
        statusLabel.setText(appName + " v" + appVersion + " — Phase 0 bootstrap complete. "
                + "Database connected and migrations applied.");
    }

    @FXML
    private void onToggleDarkMode() {
        Scene scene = darkModeToggle.getScene();
        scene.getStylesheets().removeIf(sheet -> sheet.endsWith("theme-light.css") || sheet.endsWith("theme-dark.css"));
        String theme = darkModeToggle.isSelected() ? DARK_THEME : LIGHT_THEME;
        scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
    }
}
