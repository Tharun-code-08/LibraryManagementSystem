package com.university.lms;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.university.lms.config.AppContext;
import com.university.lms.config.FxControllerFactory;
import com.university.lms.exception.GlobalExceptionHandler;

/**
 * JavaFX application entry point. Bootstraps the composition root ({@link AppContext}) —
 * configuration, connection pool, Flyway migrations, Hibernate — before showing any UI, then
 * loads the main shell. Role-based routing (Login → Dashboard/Admin/Student shells) replaces
 * the placeholder shell starting in Phase 1.
 */
public final class LibraryManagementApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(LibraryManagementApplication.class);

    private AppContext appContext;

    @Override
    public void init() {
        new GlobalExceptionHandler(this::showStartupError).install();
        appContext = AppContext.bootstrap();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/common/AppShell.fxml"));
        loader.setControllerFactory(new FxControllerFactory(appContext));
        Parent root = loader.load();

        Scene scene = new Scene(root, 960, 640);
        scene.getStylesheets().addAll(
                getClass().getResource("/css/base.css").toExternalForm(),
                getClass().getResource("/css/theme-light.css").toExternalForm());

        primaryStage.setTitle(appContext.getConfigurationManager().app("app.name"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (appContext != null) {
            appContext.shutdown();
        }
    }

    private void showStartupError(String message) {
        log.error("Startup error: {}", message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Library Management System");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
