package com.university.lms.ui.controller.inventory;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.SupplierRequestDTO;
import com.university.lms.dto.response.SupplierDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.ui.util.TablePlaceholders;

/** Supplier reference-data management: a simple list + add form. */
public final class SupplierManagementController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TableView<SupplierDTO> supplierTable;

    @FXML
    private TableColumn<SupplierDTO, String> nameColumn;

    @FXML
    private TableColumn<SupplierDTO, String> contactColumn;

    @FXML
    private TableColumn<SupplierDTO, String> phoneColumn;

    @FXML
    private TableColumn<SupplierDTO, String> emailColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField contactField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField addressField;

    @FXML
    private Label messageLabel;

    public SupplierManagementController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        supplierTable.setPlaceholder(TablePlaceholders.noResults("No suppliers found."));
        loadSuppliers();
    }

    private void loadSuppliers() {
        supplierTable.setItems(FXCollections.observableArrayList());
        supplierTable.setPlaceholder(TablePlaceholders.loading());
        appContext.getAsyncExecutor().run(
                () -> appContext.getSupplierService().listAll(),
                list -> {
                    supplierTable.setPlaceholder(TablePlaceholders.noResults("No suppliers found."));
                    supplierTable.setItems(FXCollections.observableArrayList(list));
                },
                throwable -> {
                    supplierTable.setPlaceholder(TablePlaceholders.noResults("No suppliers found."));
                    messageLabel.setText("Unable to load suppliers.");
                });
    }

    @FXML
    private void onSave() {
        messageLabel.setText("");
        if (nameField.getText() == null || nameField.getText().isBlank()) {
            messageLabel.setText("Supplier name is required.");
            return;
        }

        SupplierRequestDTO request = new SupplierRequestDTO(null, nameField.getText(), contactField.getText(),
                phoneField.getText(), emailField.getText(), addressField.getText());

        appContext.getAsyncExecutor().run(
                () -> appContext.getSupplierService().save(request),
                dto -> {
                    nameField.clear();
                    contactField.clear();
                    phoneField.clear();
                    emailField.clear();
                    addressField.clear();
                    loadSuppliers();
                },
                throwable -> messageLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to save supplier."));
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
