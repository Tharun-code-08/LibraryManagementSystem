package com.university.lms.ui.controller.admin;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.RoleDTO;
import com.university.lms.dto.response.UserSummaryDTO;
import com.university.lms.entity.UserStatus;
import com.university.lms.exception.BusinessException;
import com.university.lms.ui.util.TablePlaceholders;

/** Admin screen: user directory with account-status changes and role assignment. */
public final class UserManagementController implements Initializable {

    private final AppContext appContext;
    private List<RoleDTO> allRoles = List.of();
    private UserSummaryDTO selectedUser;

    @FXML
    private TableView<UserSummaryDTO> userTable;

    @FXML
    private TableColumn<UserSummaryDTO, String> usernameColumn;

    @FXML
    private TableColumn<UserSummaryDTO, String> emailColumn;

    @FXML
    private TableColumn<UserSummaryDTO, String> statusColumn;

    @FXML
    private TableColumn<UserSummaryDTO, String> rolesColumn;

    @FXML
    private ComboBox<UserStatus> statusCombo;

    @FXML
    private VBox roleCheckBoxContainer;

    @FXML
    private Button saveButton;

    @FXML
    private Label errorLabel;

    public UserManagementController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        rolesColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.join(", ", data.getValue().roleNames())));

        statusCombo.setItems(FXCollections.observableArrayList(UserStatus.values()));
        saveButton.setDisable(true);
        userTable.setPlaceholder(TablePlaceholders.noResults("No users found."));

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedUser = newValue;
            onUserSelected(newValue);
        });

        loadRolesThenUsers();
    }

    private void loadRolesThenUsers() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getRoleService().listRoles(),
                roles -> {
                    allRoles = roles;
                    loadUsers();
                },
                throwable -> errorLabel.setText("Unable to load roles right now."));
    }

    private void loadUsers() {
        userTable.setItems(FXCollections.observableArrayList());
        userTable.setPlaceholder(TablePlaceholders.loading());
        appContext.getAsyncExecutor().run(
                () -> appContext.getUserManagementService().listUsers(),
                users -> {
                    userTable.setPlaceholder(TablePlaceholders.noResults("No users found."));
                    userTable.setItems(FXCollections.observableArrayList(users));
                },
                throwable -> {
                    userTable.setPlaceholder(TablePlaceholders.noResults("No users found."));
                    errorLabel.setText("Unable to load users right now.");
                });
    }

    private void onUserSelected(UserSummaryDTO user) {
        roleCheckBoxContainer.getChildren().clear();
        if (user == null) {
            saveButton.setDisable(true);
            return;
        }
        statusCombo.setValue(UserStatus.valueOf(user.status()));
        for (RoleDTO role : allRoles) {
            CheckBox checkBox = new CheckBox(role.name());
            checkBox.setSelected(user.roleNames().contains(role.name()));
            checkBox.setUserData(role.id());
            roleCheckBoxContainer.getChildren().add(checkBox);
        }
        saveButton.setDisable(false);
    }

    @FXML
    private void onSave() {
        if (selectedUser == null) {
            return;
        }
        errorLabel.setText("");
        Long actorUserId = appContext.getAuthContext().getCurrentUser().getId();
        UserStatus newStatus = statusCombo.getValue();

        Set<Long> selectedRoleIds = new HashSet<>();
        for (var node : roleCheckBoxContainer.getChildren()) {
            if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                selectedRoleIds.add((Long) checkBox.getUserData());
            }
        }

        appContext.getAsyncExecutor().run(
                () -> {
                    appContext.getUserManagementService().setStatus(selectedUser.id(), newStatus, actorUserId);
                    return appContext.getUserManagementService().assignRoles(selectedUser.id(), selectedRoleIds, actorUserId);
                },
                updated -> loadUsers(),
                throwable -> errorLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to save this user right now."));
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
