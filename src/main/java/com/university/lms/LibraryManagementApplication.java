package com.university.lms;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.university.lms.config.AppContext;
import com.university.lms.config.ConfigurationManager;
import com.university.lms.config.DatabaseSetupDialog;
import com.university.lms.config.FxControllerFactory;
import com.university.lms.exception.GlobalExceptionHandler;

public final class LibraryManagementApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(LibraryManagementApplication.class);

    private ConfigurationManager configurationManager;
    private AppContext appContext;

    @Override
    public void init() {
        new GlobalExceptionHandler(this::showStartupError).install();
        // Load config only — do NOT bootstrap yet if database is unconfigured.
        configurationManager = new ConfigurationManager();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        if (!configurationManager.isDatabaseConfigured()) {
            showSetupWizard(primaryStage);
            return;
        }
        bootstrapAndShow(primaryStage);
    }

    private void showSetupWizard(Stage primaryStage) {
        DatabaseSetupDialog dialog = new DatabaseSetupDialog();
        Boolean saved = dialog.showAndWait().orElse(null);

        if (!Boolean.TRUE.equals(saved)) {
            // User cancelled — nothing to do, exit cleanly.
            Platform.exit();
            return;
        }

        // Reload config from the newly saved file, then bootstrap on a background thread.
        configurationManager = new ConfigurationManager();
        showLoadingScreen(primaryStage);

        new Thread(() -> {
            try {
                appContext = AppContext.bootstrap(configurationManager);
                Platform.runLater(() -> {
                    try {
                        showMainWindow(primaryStage);
                    } catch (IOException e) {
                        showStartupError(e.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showStartupError(e.getMessage()));
            }
        }, "app-bootstrap").start();
    }

    private void showLoadingScreen(Stage primaryStage) {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(48, 48);
        Label label = new Label("Connecting to database…");
        label.setStyle("-fx-font-size:14px;-fx-text-fill:#5B6478;");
        VBox box = new VBox(16, spinner, label);
        box.setStyle("-fx-alignment:center;-fx-background-color:#F5F7FB;");
        primaryStage.setScene(new Scene(box, 320, 200));
        primaryStage.setTitle("Library Management System");
        primaryStage.show();
    }

    private void bootstrapAndShow(Stage primaryStage) throws IOException {
        appContext = AppContext.bootstrap(configurationManager);
        showMainWindow(primaryStage);
    }

    private void showMainWindow(Stage primaryStage) throws IOException {
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
        alert.setHeaderText("Library Management System — Startup Error");
        alert.showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
