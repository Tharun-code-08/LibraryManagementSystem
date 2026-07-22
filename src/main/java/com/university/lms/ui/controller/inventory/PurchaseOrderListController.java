package com.university.lms.ui.controller.inventory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.PurchaseOrderSearchCriteria;
import com.university.lms.dto.response.PurchaseOrderDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.model.Page;
import com.university.lms.ui.util.TablePlaceholders;

/** Purchase order list: filter by status, pagination, and jump to the detail/approval screen. */
public final class PurchaseOrderListController implements Initializable {

    private static final int PAGE_SIZE = 25;

    private final AppContext appContext;
    private int currentPage = 0;
    private long totalElements = 0;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TableView<PurchaseOrderDTO> orderTable;

    @FXML
    private TableColumn<PurchaseOrderDTO, String> supplierColumn;

    @FXML
    private TableColumn<PurchaseOrderDTO, String> orderedByColumn;

    @FXML
    private TableColumn<PurchaseOrderDTO, String> statusColumn;

    @FXML
    private TableColumn<PurchaseOrderDTO, BigDecimal> totalCostColumn;

    @FXML
    private TableColumn<PurchaseOrderDTO, Void> actionsColumn;

    @FXML
    private Label pageLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    public PurchaseOrderListController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        orderedByColumn.setCellValueFactory(new PropertyValueFactory<>("orderedByName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));

        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "DRAFT", "PENDING_APPROVAL", "APPROVED", "RECEIVED", "CANCELLED"));
        statusFilter.getSelectionModel().selectFirst();

        orderTable.setPlaceholder(TablePlaceholders.noResults("No purchase orders found."));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("Manage");

            {
                viewButton.setOnAction(event -> {
                    PurchaseOrderDTO order = getTableView().getItems().get(getIndex());
                    appContext.setNavigationParameter(order.getId());
                    appContext.getViewNavigator().navigate("/fxml/inventory/PurchaseOrderDetail.fxml");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
            }
        });

        loadPage(0);
    }

    @FXML
    private void onFilter() {
        loadPage(0);
    }

    @FXML
    private void onPrevPage() {
        if (currentPage > 0) {
            loadPage(currentPage - 1);
        }
    }

    @FXML
    private void onNextPage() {
        if ((long) (currentPage + 1) * PAGE_SIZE < totalElements) {
            loadPage(currentPage + 1);
        }
    }

    @FXML
    private void onNewPurchaseOrder() {
        appContext.getViewNavigator().navigate("/fxml/inventory/PurchaseOrderForm.fxml");
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }

    private void loadPage(int pageNumber) {
        errorLabel.setText("");
        orderTable.setItems(FXCollections.observableArrayList());
        orderTable.setPlaceholder(TablePlaceholders.loading());
        String status = statusFilter.getValue();
        PurchaseOrderSearchCriteria criteria = PurchaseOrderSearchCriteria.builder()
                .status("All".equals(status) ? null : status)
                .pageNumber(pageNumber)
                .pageSize(PAGE_SIZE)
                .build();

        appContext.getAsyncExecutor().run(
                () -> appContext.getPurchaseOrderService().search(criteria),
                this::onPageLoaded,
                throwable -> {
                    orderTable.setPlaceholder(TablePlaceholders.noResults("No purchase orders found."));
                    errorLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to load purchase orders right now.");
                });
    }

    private void onPageLoaded(Page<PurchaseOrderDTO> page) {
        currentPage = page.getPageNumber();
        totalElements = page.getTotalElements();
        orderTable.setPlaceholder(TablePlaceholders.noResults("No purchase orders found."));
        orderTable.setItems(FXCollections.observableArrayList(page.getContent()));
        int totalPages = Math.max(page.getTotalPages(), 1);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages + " (" + totalElements + " orders)");
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable((long) (currentPage + 1) * PAGE_SIZE >= totalElements);
    }
}
