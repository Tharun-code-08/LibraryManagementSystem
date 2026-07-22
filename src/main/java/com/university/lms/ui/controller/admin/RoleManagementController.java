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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.PermissionDTO;
import com.university.lms.dto.response.RoleDTO;
import com.university.lms.exception.BusinessException;

/** Admin screen: the permission matrix editor — pick a role, toggle its permission set. */
public final class RoleManagementController implements Initializable {

    private final AppContext appContext;
    private List<PermissionDTO> allPermissions = List.of();
    private RoleDTO selectedRole;

    @FXML
    private ListView<RoleDTO> roleList;

    @FXML
    private VBox permissionCheckBoxContainer;

    @FXML
    private Button saveButton;

    @FXML
    private Label errorLabel;

    public RoleManagementController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(RoleDTO role, boolean empty) {
                super.updateItem(role, empty);
                setText(empty || role == null ? null : role.name());
            }
        });
        saveButton.setDisable(true);

        roleList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedRole = newValue;
            onRoleSelected(newValue);
        });

        loadPermissionsThenRoles();
    }

    private void loadPermissionsThenRoles() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getRoleService().listPermissions(),
                permissions -> {
                    allPermissions = permissions;
                    loadRoles();
                },
                throwable -> errorLabel.setText("Unable to load permissions right now."));
    }

    private void loadRoles() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getRoleService().listRoles(),
                roles -> roleList.setItems(FXCollections.observableArrayList(roles)),
                throwable -> errorLabel.setText("Unable to load roles right now."));
    }

    private void onRoleSelected(RoleDTO role) {
        permissionCheckBoxContainer.getChildren().clear();
        if (role == null) {
            saveButton.setDisable(true);
            return;
        }
        for (PermissionDTO permission : allPermissions) {
            CheckBox checkBox = new CheckBox(permission.code() + " — " + permission.description());
            checkBox.setSelected(role.permissionIds().contains(permission.id()));
            checkBox.setUserData(permission.id());
            permissionCheckBoxContainer.getChildren().add(checkBox);
        }
        saveButton.setDisable(false);
    }

    @FXML
    private void onSave() {
        if (selectedRole == null) {
            return;
        }
        errorLabel.setText("");
        Long actorUserId = appContext.getAuthContext().getCurrentUser().getId();

        Set<Long> selectedPermissionIds = new HashSet<>();
        for (var node : permissionCheckBoxContainer.getChildren()) {
            if (node instanceof CheckBox checkBox && checkBox.isSelected()) {
                selectedPermissionIds.add((Long) checkBox.getUserData());
            }
        }

        appContext.getAsyncExecutor().run(
                () -> appContext.getRoleService().updatePermissions(selectedRole.id(), selectedPermissionIds, actorUserId),
                updated -> loadRoles(),
                throwable -> errorLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to save this role right now."));
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
