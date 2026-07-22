package com.university.lms.ui.controller.inventory;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.InventoryScanRequestDTO;
import com.university.lms.dto.response.InventoryAuditDTO;
import com.university.lms.dto.response.InventoryAuditItemDTO;
import com.university.lms.entity.Branch;
import com.university.lms.exception.BusinessException;

/** Shelf verification: start an audit, scan copies against expected status, then complete it. */
public final class InventoryAuditController implements Initializable {

    private final AppContext appContext;
    private Long auditId;

    @FXML
    private ComboBox<Branch> branchCombo;

    @FXML
    private Button startButton;

    @FXML
    private Label auditStatusLabel;

    @FXML
    private TextField barcodeField;

    @FXML
    private ComboBox<String> foundStatusCombo;

    @FXML
    private TextField notesField;

    @FXML
    private Button scanButton;

    @FXML
    private Button completeButton;

    @FXML
    private TableView<InventoryAuditItemDTO> itemsTable;

    @FXML
    private TableColumn<InventoryAuditItemDTO, String> barcodeColumn;

    @FXML
    private TableColumn<InventoryAuditItemDTO, String> bookTitleColumn;

    @FXML
    private TableColumn<InventoryAuditItemDTO, String> expectedColumn;

    @FXML
    private TableColumn<InventoryAuditItemDTO, String> foundColumn;

    @FXML
    private Label messageLabel;

    public InventoryAuditController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("copyBarcode"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        expectedColumn.setCellValueFactory(new PropertyValueFactory<>("expectedStatus"));
        foundColumn.setCellValueFactory(new PropertyValueFactory<>("foundStatus"));

        foundStatusCombo.setItems(FXCollections.observableArrayList(
                "AVAILABLE", "ISSUED", "RESERVED", "MAINTENANCE", "LOST", "RETIRED"));
        foundStatusCombo.getSelectionModel().selectFirst();

        branchCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Branch branch) {
                return branch == null ? "" : branch.getName();
            }

            @Override
            public Branch fromString(String string) {
                return null;
            }
        });

        setScanControlsDisabled(true);

        appContext.getAsyncExecutor().run(
                () -> appContext.getBranchRepository().findAll(),
                branches -> {
                    branchCombo.setItems(FXCollections.observableArrayList(branches));
                    if (!branches.isEmpty()) {
                        branchCombo.getSelectionModel().selectFirst();
                    }
                },
                throwable -> messageLabel.setText("Unable to load branches."));
    }

    private void setScanControlsDisabled(boolean disabled) {
        barcodeField.setDisable(disabled);
        foundStatusCombo.setDisable(disabled);
        notesField.setDisable(disabled);
        scanButton.setDisable(disabled);
        completeButton.setDisable(disabled);
    }

    @FXML
    private void onStartAudit() {
        Branch branch = branchCombo.getValue();
        if (branch == null) {
            messageLabel.setText("Select a branch first.");
            return;
        }
        messageLabel.setText("");
        startButton.setDisable(true);
        Long conductedByUserId = appContext.getAuthContext().getCurrentUser().getId();

        appContext.getAsyncExecutor().run(
                () -> appContext.getInventoryAuditService().startAudit(branch.getId(), conductedByUserId),
                this::onAuditUpdated,
                throwable -> {
                    startButton.setDisable(false);
                    messageLabel.setText("Unable to start audit right now.");
                });
    }

    @FXML
    private void onRecordScan() {
        messageLabel.setText("");
        if (barcodeField.getText() == null || barcodeField.getText().isBlank()) {
            messageLabel.setText("Scan or type a copy barcode.");
            return;
        }
        InventoryScanRequestDTO request = new InventoryScanRequestDTO(
                auditId, barcodeField.getText(), foundStatusCombo.getValue(), notesField.getText());

        appContext.getAsyncExecutor().run(
                () -> appContext.getInventoryAuditService().recordScan(request),
                dto -> {
                    onAuditUpdated(dto);
                    barcodeField.clear();
                    notesField.clear();
                    barcodeField.requestFocus();
                },
                throwable -> messageLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to record this scan right now."));
    }

    @FXML
    private void onCompleteAudit() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getInventoryAuditService().completeAudit(auditId),
                dto -> {
                    onAuditUpdated(dto);
                    setScanControlsDisabled(true);
                    startButton.setDisable(false);
                },
                throwable -> messageLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to complete this audit right now."));
    }

    private void onAuditUpdated(InventoryAuditDTO audit) {
        auditId = audit.id();
        auditStatusLabel.setText("Audit #" + audit.id() + " — " + audit.branchName()
                + " — Status: " + audit.status() + " — Items scanned: " + audit.items().size());
        itemsTable.setItems(FXCollections.observableArrayList(audit.items()));
        setScanControlsDisabled("COMPLETED".equals(audit.status()) || "CANCELLED".equals(audit.status()));
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
