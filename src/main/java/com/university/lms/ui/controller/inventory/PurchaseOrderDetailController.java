package com.university.lms.ui.controller.inventory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.InvoiceRequestDTO;
import com.university.lms.dto.response.InvoiceDTO;
import com.university.lms.dto.response.PurchaseOrderDTO;
import com.university.lms.dto.response.PurchaseOrderItemDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.ui.util.TablePlaceholders;

/** Purchase order detail: approval-workflow actions plus invoice recording. */
public final class PurchaseOrderDetailController implements Initializable {

    private final AppContext appContext;
    private Long purchaseOrderId;

    @FXML
    private Label summaryLabel;

    @FXML
    private TableView<PurchaseOrderItemDTO> itemsTable;

    @FXML
    private TableColumn<PurchaseOrderItemDTO, String> descriptionColumn;

    @FXML
    private TableColumn<PurchaseOrderItemDTO, Integer> quantityColumn;

    @FXML
    private TableColumn<PurchaseOrderItemDTO, BigDecimal> unitCostColumn;

    @FXML
    private TableColumn<PurchaseOrderItemDTO, BigDecimal> lineTotalColumn;

    @FXML
    private Button submitButton;

    @FXML
    private Button approveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button receiveButton;

    @FXML
    private TextField invoiceNumberField;

    @FXML
    private TextField invoiceAmountField;

    @FXML
    private ListView<String> invoicesList;

    @FXML
    private Label messageLabel;

    public PurchaseOrderDetailController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Object param = appContext.getNavigationParameter();
        appContext.setNavigationParameter(null);
        purchaseOrderId = param instanceof Long id ? id : null;

        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitCostColumn.setCellValueFactory(new PropertyValueFactory<>("unitCost"));
        lineTotalColumn.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
        itemsTable.setPlaceholder(TablePlaceholders.noResults("No line items."));

        if (purchaseOrderId == null) {
            summaryLabel.setText("No purchase order selected.");
            return;
        }
        loadOrder();
    }

    private void loadOrder() {
        itemsTable.setItems(FXCollections.observableArrayList());
        itemsTable.setPlaceholder(TablePlaceholders.loading());
        appContext.getAsyncExecutor().run(
                () -> appContext.getPurchaseOrderService().getById(purchaseOrderId),
                orderOptional -> orderOptional.ifPresentOrElse(this::populate,
                        () -> {
                            itemsTable.setPlaceholder(TablePlaceholders.noResults("No line items."));
                            summaryLabel.setText("Purchase order not found.");
                        }),
                throwable -> {
                    itemsTable.setPlaceholder(TablePlaceholders.noResults("No line items."));
                    messageLabel.setText("Unable to load purchase order.");
                });
    }

    private void populate(PurchaseOrderDTO order) {
        summaryLabel.setText(order.getSupplierName() + " — " + order.getOrderDate()
                + " | Status: " + order.getStatus() + " | Total: " + order.getTotalCost()
                + " | Budget: " + order.getBudgetAmount()
                + (order.getApprovedByName() != null ? " | Approved by: " + order.getApprovedByName() : ""));
        itemsTable.setPlaceholder(TablePlaceholders.noResults("No line items."));
        itemsTable.setItems(FXCollections.observableArrayList(order.getItems()));

        String status = order.getStatus();
        submitButton.setDisable(!"DRAFT".equals(status));
        approveButton.setDisable(!"PENDING_APPROVAL".equals(status));
        receiveButton.setDisable(!"APPROVED".equals(status));
        cancelButton.setDisable("RECEIVED".equals(status) || "CANCELLED".equals(status));

        loadInvoices();
    }

    private void loadInvoices() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getInvoiceService().listForPurchaseOrder(purchaseOrderId),
                invoices -> invoicesList.setItems(FXCollections.observableArrayList(
                        invoices.stream().map(this::formatInvoice).toList())),
                throwable -> messageLabel.setText("Unable to load invoices."));
    }

    private String formatInvoice(InvoiceDTO invoice) {
        return invoice.invoiceNumber() + " — " + invoice.invoiceDate() + " — " + invoice.totalAmount();
    }

    @FXML
    private void onSubmitForApproval() {
        runAction(() -> appContext.getPurchaseOrderService().submitForApproval(purchaseOrderId));
    }

    @FXML
    private void onApprove() {
        Long approvedByUserId = appContext.getAuthContext().getCurrentUser().getId();
        runAction(() -> appContext.getPurchaseOrderService().approve(purchaseOrderId, approvedByUserId));
    }

    @FXML
    private void onCancel() {
        runAction(() -> appContext.getPurchaseOrderService().cancel(purchaseOrderId));
    }

    @FXML
    private void onMarkReceived() {
        runAction(() -> appContext.getPurchaseOrderService().markReceived(purchaseOrderId));
    }

    private void runAction(java.util.concurrent.Callable<PurchaseOrderDTO> action) {
        messageLabel.setText("");
        appContext.getAsyncExecutor().run(
                action,
                order -> populate(order),
                throwable -> messageLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to complete this action right now."));
    }

    @FXML
    private void onRecordInvoice() {
        messageLabel.setText("");
        BigDecimal amount;
        try {
            amount = new BigDecimal(invoiceAmountField.getText().trim());
        } catch (NumberFormatException e) {
            messageLabel.setText("Invoice amount must be a valid number.");
            return;
        }
        if (invoiceNumberField.getText() == null || invoiceNumberField.getText().isBlank()) {
            messageLabel.setText("Invoice number is required.");
            return;
        }

        InvoiceRequestDTO request = new InvoiceRequestDTO(
                purchaseOrderId, invoiceNumberField.getText(), LocalDate.now(), amount, null);

        appContext.getAsyncExecutor().run(
                () -> appContext.getInvoiceService().recordInvoice(request),
                dto -> {
                    invoiceNumberField.clear();
                    invoiceAmountField.clear();
                    loadInvoices();
                },
                throwable -> messageLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to record this invoice right now."));
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/inventory/PurchaseOrderList.fxml");
    }
}
