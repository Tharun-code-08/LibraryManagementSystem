package com.university.lms.ui.controller.inventory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.PurchaseOrderItemRequestDTO;
import com.university.lms.dto.request.PurchaseOrderRequestDTO;
import com.university.lms.dto.response.SupplierDTO;
import com.university.lms.exception.BusinessException;

/** Create a new (DRAFT) purchase order with one or more line items. */
public final class PurchaseOrderFormController implements Initializable {

    private final AppContext appContext;
    private final ObservableList<PurchaseOrderItemRequestDTO> pendingItems = FXCollections.observableArrayList();

    @FXML
    private ComboBox<SupplierDTO> supplierCombo;

    @FXML
    private TextField budgetField;

    @FXML
    private TextField itemDescriptionField;

    @FXML
    private TextField itemQuantityField;

    @FXML
    private TextField itemUnitCostField;

    @FXML
    private ListView<PurchaseOrderItemRequestDTO> itemsList;

    @FXML
    private Label messageLabel;

    @FXML
    private Button createButton;

    public PurchaseOrderFormController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        supplierCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SupplierDTO supplier) {
                return supplier == null ? "" : supplier.name();
            }

            @Override
            public SupplierDTO fromString(String string) {
                return null;
            }
        });

        itemsList.setItems(pendingItems);
        itemsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(PurchaseOrderItemRequestDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.quantity() + " x " + item.description() + " @ " + item.unitCost());
            }
        });

        appContext.getAsyncExecutor().run(
                () -> appContext.getSupplierService().listAll(),
                suppliers -> supplierCombo.setItems(FXCollections.observableArrayList(suppliers)),
                throwable -> messageLabel.setText("Unable to load suppliers."));
    }

    @FXML
    private void onAddItem() {
        messageLabel.setText("");
        try {
            String description = itemDescriptionField.getText();
            int quantity = Integer.parseInt(itemQuantityField.getText().trim());
            BigDecimal unitCost = new BigDecimal(itemUnitCostField.getText().trim());
            if (description == null || description.isBlank()) {
                messageLabel.setText("Item description is required.");
                return;
            }
            pendingItems.add(new PurchaseOrderItemRequestDTO(null, description, quantity, unitCost));
            itemDescriptionField.clear();
            itemQuantityField.clear();
            itemUnitCostField.clear();
        } catch (NumberFormatException e) {
            messageLabel.setText("Quantity and unit cost must be valid numbers.");
        }
    }

    @FXML
    private void onRemoveSelectedItem() {
        PurchaseOrderItemRequestDTO selected = itemsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            pendingItems.remove(selected);
        }
    }

    @FXML
    private void onCreate() {
        messageLabel.setText("");
        SupplierDTO supplier = supplierCombo.getValue();
        if (supplier == null) {
            messageLabel.setText("Select a supplier.");
            return;
        }
        if (pendingItems.isEmpty()) {
            messageLabel.setText("Add at least one line item.");
            return;
        }

        BigDecimal budget;
        try {
            budget = budgetField.getText().isBlank() ? BigDecimal.ZERO : new BigDecimal(budgetField.getText().trim());
        } catch (NumberFormatException e) {
            messageLabel.setText("Budget amount must be a valid number.");
            return;
        }

        createButton.setDisable(true);
        List<PurchaseOrderItemRequestDTO> items = new ArrayList<>(pendingItems);
        PurchaseOrderRequestDTO request = new PurchaseOrderRequestDTO(supplier.id(), budget, items);
        Long orderedByUserId = appContext.getAuthContext().getCurrentUser().getId();

        appContext.getAsyncExecutor().run(
                () -> appContext.getPurchaseOrderService().create(request, orderedByUserId),
                dto -> appContext.getViewNavigator().navigate("/fxml/inventory/PurchaseOrderList.fxml"),
                throwable -> {
                    createButton.setDisable(false);
                    messageLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to create this purchase order right now.");
                });
    }

    @FXML
    private void onCancel() {
        appContext.getViewNavigator().navigate("/fxml/inventory/PurchaseOrderList.fxml");
    }
}
