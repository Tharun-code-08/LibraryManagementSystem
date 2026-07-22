package com.university.lms.ui.navigation;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 * Loads FXML views into a designated content {@link Pane} (the routed content area inside the
 * main shell). Each authenticated shell owns one {@code ViewNavigator} bound to its content
 * region; screens navigate by fxml path rather than manipulating scene graphs directly.
 */
public final class ViewNavigator {

    private final Pane contentHost;
    private final Callback<Class<?>, Object> controllerFactory;

    public ViewNavigator(Pane contentHost, Callback<Class<?>, Object> controllerFactory) {
        this.contentHost = contentHost;
        this.controllerFactory = controllerFactory;
    }

    public void navigate(String fxmlClasspathResource) {
        URL location = getClass().getResource(fxmlClasspathResource);
        if (location == null) {
            throw new IllegalArgumentException("FXML resource not found: " + fxmlClasspathResource);
        }
        try {
            FXMLLoader loader = new FXMLLoader(location);
            loader.setControllerFactory(controllerFactory);
            Parent view = loader.load();
            contentHost.getChildren().setAll(view);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load view: " + fxmlClasspathResource, e);
        }
    }
}
