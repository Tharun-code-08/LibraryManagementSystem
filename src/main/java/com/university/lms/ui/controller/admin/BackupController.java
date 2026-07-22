package com.university.lms.ui.controller.admin;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.BackupDTO;
import com.university.lms.ui.util.TablePlaceholders;

/** Admin screen: run an on-demand database backup, view history, and restore from a past backup. */
public final class BackupController implements Initializable {

    private static final int HISTORY_LIMIT = 25;

    private final AppContext appContext;

    @FXML
    private Button runBackupButton;

    @FXML
    private TableView<BackupDTO> backupTable;

    @FXML
    private TableColumn<BackupDTO, String> filePathColumn;

    @FXML
    private TableColumn<BackupDTO, Long> sizeColumn;

    @FXML
    private TableColumn<BackupDTO, String> statusColumn;

    @FXML
    private TableColumn<BackupDTO, String> createdAtColumn;

    @FXML
    private TableColumn<BackupDTO, Void> actionsColumn;

    @FXML
    private Label statusLabel;

    public BackupController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filePathColumn.setCellValueFactory(new PropertyValueFactory<>("filePath"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("sizeBytes"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().createdAt())));

        backupTable.setPlaceholder(TablePlaceholders.noResults("No backups found."));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button restoreButton = new Button("Restore");
            private final HBox box = new HBox(restoreButton);

            {
                restoreButton.setOnAction(event -> onRestore(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        loadBackups();
    }

    @FXML
    private void onRunBackup() {
        statusLabel.setText("Running backup...");
        runBackupButton.setDisable(true);
        Long actorUserId = appContext.getAuthContext().getCurrentUser().getId();
        appContext.getAsyncExecutor().run(
                () -> appContext.getBackupService().runBackup(actorUserId),
                backup -> {
                    runBackupButton.setDisable(false);
                    statusLabel.setText("Backup finished with status " + backup.status() + ".");
                    loadBackups();
                },
                throwable -> {
                    runBackupButton.setDisable(false);
                    statusLabel.setText("Unable to run a backup right now.");
                });
    }

    private void onRestore(BackupDTO backup) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This will overwrite the current database with the contents of\n" + backup.filePath()
                        + "\n\nThis cannot be undone. Continue?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                runRestore(backup);
            }
        });
    }

    private void runRestore(BackupDTO backup) {
        statusLabel.setText("Restoring...");
        Long actorUserId = appContext.getAuthContext().getCurrentUser().getId();
        appContext.getAsyncExecutor().run(
                () -> appContext.getBackupService().restoreBackup(backup.id(), actorUserId),
                success -> statusLabel.setText(success ? "Restore completed successfully." : "Restore failed."),
                throwable -> statusLabel.setText("Unable to restore this backup right now."));
    }

    private void loadBackups() {
        backupTable.setItems(FXCollections.observableArrayList());
        backupTable.setPlaceholder(TablePlaceholders.loading());
        appContext.getAsyncExecutor().run(
                () -> appContext.getBackupService().listRecent(HISTORY_LIMIT),
                backups -> {
                    backupTable.setPlaceholder(TablePlaceholders.noResults("No backups found."));
                    backupTable.setItems(FXCollections.observableArrayList(backups));
                },
                throwable -> {
                    backupTable.setPlaceholder(TablePlaceholders.noResults("No backups found."));
                    statusLabel.setText("Unable to load backup history right now.");
                });
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
