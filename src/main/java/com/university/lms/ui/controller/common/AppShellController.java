package com.university.lms.ui.controller.common;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;

import com.university.lms.config.AppContext;
import com.university.lms.config.FxControllerFactory;
import com.university.lms.ui.navigation.ViewNavigator;

/**
 * Root container of the application window. Owns the single {@link ViewNavigator} that every
 * other controller uses to switch screens (Login → role shell → forms), publishing it onto
 * {@link AppContext} so it can be reached without threading it through every constructor.
 *
 * <p>Also owns the window-wide {@code Ctrl+K} global search shortcut: the content host is a
 * {@link StackPane}, so the search overlay is simply pushed on top of whatever screen is
 * currently showing rather than requiring every screen to embed its own search entry point.
 */
public final class AppShellController implements Initializable {

    private final AppContext appContext;
    private final FxControllerFactory controllerFactory;

    @FXML
    private StackPane contentHost;

    public AppShellController(AppContext appContext) {
        this.appContext = appContext;
        this.controllerFactory = new FxControllerFactory(appContext);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewNavigator navigator = new ViewNavigator(contentHost, controllerFactory);
        appContext.setViewNavigator(navigator);
        appContext.setOverlayHost(contentHost);
        appContext.setGlobalSearchOpener(this::toggleGlobalSearch);

        contentHost.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN), this::toggleGlobalSearch);
            }
        });

        navigator.navigate("/fxml/auth/Login.fxml");
    }

    private void toggleGlobalSearch() {
        if (!appContext.getAuthContext().isAuthenticated()) {
            return;
        }
        boolean alreadyOpen = contentHost.getChildren().stream()
                .anyMatch(node -> "global-search-overlay".equals(node.getId()));
        if (alreadyOpen) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/search/GlobalSearchOverlay.fxml"));
            loader.setControllerFactory(controllerFactory);
            Parent overlay = loader.load();
            contentHost.getChildren().add(overlay);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load the global search overlay", e);
        }
    }
}
