package com.university.lms.config;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * First-run setup wizard shown when no database credentials are configured.
 * Saves to ~/.librarymanagementsystem/database.properties so credentials survive app updates.
 */
public final class DatabaseSetupDialog extends Dialog<Boolean> {

    private final TextField hostField     = new TextField("localhost");
    private final TextField portField     = new TextField("3306");
    private final TextField dbNameField   = new TextField("library_management");
    private final TextField usernameField = new TextField("lms_app");
    private final PasswordField passwordField = new PasswordField();
    private final Label statusLabel = new Label();
    private final Button testBtn    = new Button("Test Connection");
    private final ButtonType saveBtn = new ButtonType("Save & Start", ButtonBar.ButtonData.OK_DONE);

    public DatabaseSetupDialog() {
        setTitle("Library Management System — Database Setup");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        getDialogPane().setContent(buildContent());
        getDialogPane().setPrefWidth(480);

        getDialogPane().getStylesheets().addAll(
                getClass().getResource("/css/base.css").toExternalForm(),
                getClass().getResource("/css/theme-light.css").toExternalForm());

        Button okButton = (Button) getDialogPane().lookupButton(saveBtn);
        okButton.setDisable(true);
        okButton.setStyle("-fx-background-color:#3B5BFB;-fx-text-fill:white;-fx-background-radius:6;-fx-font-weight:700;-fx-padding:8 20;");

        testBtn.setOnAction(e -> testConnection(okButton));
        testBtn.setStyle("-fx-background-color:#EEF1F8;-fx-text-fill:#1B1F2A;-fx-background-radius:6;-fx-padding:8 20;");

        setResultConverter(btn -> btn == saveBtn ? saveConfig() : null);
    }

    private VBox buildContent() {
        Label title = new Label("Connect to MySQL");
        title.setStyle("-fx-font-size:22px;-fx-font-weight:700;-fx-text-fill:#3B5BFB;");

        Label subtitle = new Label(
                "Enter your MySQL 8 connection details. Flyway will create\n" +
                "all tables automatically on first launch.");
        subtitle.setStyle("-fx-font-size:13px;-fx-text-fill:#5B6478;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16, 0, 8, 0));

        addRow(grid, 0, "Host",          hostField);
        addRow(grid, 1, "Port",          portField);
        addRow(grid, 2, "Database name", dbNameField);
        addRow(grid, 3, "Username",      usernameField);
        addRow(grid, 4, "Password",      passwordField);

        for (var tf : new TextField[]{hostField, portField, dbNameField, usernameField}) {
            tf.setStyle("-fx-background-radius:6;-fx-border-radius:6;-fx-border-color:#E1E5F0;-fx-padding:8;");
        }
        passwordField.setStyle("-fx-background-radius:6;-fx-border-radius:6;-fx-border-color:#E1E5F0;-fx-padding:8;");

        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(420);
        statusLabel.setStyle("-fx-font-size:12px;");

        HBox buttons = new HBox(10, testBtn, statusLabel);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, title, subtitle, grid, buttons);
        box.setPadding(new Insets(20));
        return box;
    }

    private void addRow(GridPane grid, int row, String label, Control field) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:13px;-fx-font-weight:600;-fx-text-fill:#1B1F2A;");
        lbl.setPrefWidth(120);
        GridPane.setHgrow(field, Priority.ALWAYS);
        ((Region) field).setMaxWidth(Double.MAX_VALUE);
        grid.addRow(row, lbl, field);
    }

    private void testConnection(Button okButton) {
        testBtn.setDisable(true);
        statusLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#5B6478;");
        statusLabel.setText("Testing…");

        String url = buildJdbcUrl();
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        new Thread(() -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection c = DriverManager.getConnection(url, user, pass)) {
                    Platform.runLater(() -> {
                        statusLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#16a34a;");
                        statusLabel.setText("Connected successfully!");
                        okButton.setDisable(false);
                        testBtn.setDisable(false);
                    });
                }
            } catch (ClassNotFoundException | SQLException ex) {
                String msg = ex instanceof SQLException se
                        ? friendlyError(se)
                        : "MySQL driver not found.";
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-font-size:12px;-fx-text-fill:#B91C1C;");
                    statusLabel.setText(msg);
                    testBtn.setDisable(false);
                });
            }
        }, "db-test").start();
    }

    private String friendlyError(SQLException se) {
        String msg = se.getMessage() != null ? se.getMessage() : se.getSQLState();
        if (msg.contains("Communications link failure") || msg.contains("Connection refused")) {
            return "Cannot reach MySQL at " + hostField.getText().trim() + ":" + portField.getText().trim()
                    + ". Is MySQL running?";
        }
        if (msg.contains("Access denied")) {
            return "Access denied — check username and password.";
        }
        if (msg.contains("Unknown database")) {
            return "Database '" + dbNameField.getText().trim() + "' does not exist. Create it first:\n"
                    + "CREATE DATABASE " + dbNameField.getText().trim() + ";";
        }
        return msg.length() > 120 ? msg.substring(0, 120) + "…" : msg;
    }

    private Boolean saveConfig() {
        try {
            Path dir = configDir();
            Files.createDirectories(dir);
            Path file = dir.resolve("database.properties");

            Properties p = new Properties();
            p.setProperty("db.jdbc-url", buildJdbcUrl());
            p.setProperty("db.username", usernameField.getText().trim());
            p.setProperty("db.password", passwordField.getText());
            p.setProperty("db.driver-class-name", "com.mysql.cj.jdbc.Driver");

            try (OutputStream out = Files.newOutputStream(file)) {
                p.store(out, "Library Management System — saved by setup wizard");
            }
            return Boolean.TRUE;
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Could not save config: " + e.getMessage()).showAndWait();
            return null;
        }
    }

    private String buildJdbcUrl() {
        return "jdbc:mysql://"
                + hostField.getText().trim() + ":"
                + portField.getText().trim() + "/"
                + dbNameField.getText().trim()
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }

    static Path configDir() {
        return Path.of(System.getProperty("user.home"), ".librarymanagementsystem");
    }
}
