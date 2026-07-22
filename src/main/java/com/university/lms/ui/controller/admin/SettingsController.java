package com.university.lms.ui.controller.admin;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.SettingDTO;

/** Admin screen: editable list of persisted system settings (theme, locale, borrow rules, ...). */
public final class SettingsController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TableView<SettingDTO> settingsTable;

    @FXML
    private TableColumn<SettingDTO, String> keyColumn;

    @FXML
    private TableColumn<SettingDTO, String> valueColumn;

    @FXML
    private TableColumn<SettingDTO, String> categoryColumn;

    @FXML
    private Label errorLabel;

    public SettingsController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(editableValueCellFactory());

        loadSettings();
    }

    private Callback<TableColumn<SettingDTO, String>, TableCell<SettingDTO, String>> editableValueCellFactory() {
        return column -> new TableCell<>() {
            private final TextField textField = new TextField();

            {
                textField.setOnAction(event -> commit());
                textField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                    if (!isFocused) {
                        commit();
                    }
                });
            }

            private void commit() {
                SettingDTO setting = getTableRow() == null ? null : getTableRow().getItem();
                if (setting == null || textField.getText().equals(setting.value())) {
                    return;
                }
                onValueChanged(setting.key(), textField.getText());
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                textField.setText(value == null ? "" : value);
                setGraphic(textField);
            }
        };
    }

    private void onValueChanged(String key, String newValue) {
        errorLabel.setText("");
        Long actorUserId = appContext.getAuthContext().getCurrentUser().getId();
        appContext.getAsyncExecutor().run(
                () -> appContext.getSettingsService().updateSetting(key, newValue, actorUserId),
                updated -> loadSettings(),
                throwable -> errorLabel.setText("Unable to save this setting right now."));
    }

    private void loadSettings() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getSettingsService().listAll(),
                settings -> settingsTable.setItems(FXCollections.observableArrayList(settings)),
                throwable -> errorLabel.setText("Unable to load settings right now."));
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
