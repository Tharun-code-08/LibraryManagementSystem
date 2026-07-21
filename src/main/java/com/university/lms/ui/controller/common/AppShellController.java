package com.university.lms.ui.controller.common;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import com.university.lms.config.AppContext;
import com.university.lms.config.FxControllerFactory;
import com.university.lms.ui.navigation.ViewNavigator;

/**
 * Root container of the application window. Owns the single {@link ViewNavigator} that every
 * other controller uses to switch screens (Login → role shell → forms), publishing it onto
 * {@link AppContext} so it can be reached without threading it through every constructor.
 */
public final class AppShellController implements Initializable {

    private final AppContext appContext;

    @FXML
    private StackPane contentHost;

    public AppShellController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ViewNavigator navigator = new ViewNavigator(contentHost, new FxControllerFactory(appContext));
        appContext.setViewNavigator(navigator);
        navigator.navigate("/fxml/auth/Login.fxml");
    }
}
