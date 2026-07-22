package com.university.lms.ui.controller.finance;

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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.FineSearchCriteria;
import com.university.lms.dto.request.ManualFineRequestDTO;
import com.university.lms.dto.response.FineDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.model.Page;
import com.university.lms.ui.util.TablePlaceholders;

/** Fine dashboard: filter by status, waive a fine, jump to payment collection, or add a manual fine. */
public final class FineManagementController implements Initializable {

    private static final int PAGE_SIZE = 25;

    private final AppContext appContext;
    private int currentPage = 0;
    private long totalElements = 0;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TableView<FineDTO> fineTable;

    @FXML
    private TableColumn<FineDTO, String> bookColumn;

    @FXML
    private TableColumn<FineDTO, String> memberColumn;

    @FXML
    private TableColumn<FineDTO, String> reasonColumn;

    @FXML
    private TableColumn<FineDTO, BigDecimal> amountColumn;

    @FXML
    private TableColumn<FineDTO, BigDecimal> remainingColumn;

    @FXML
    private TableColumn<FineDTO, String> statusColumn;

    @FXML
    private TableColumn<FineDTO, Void> actionsColumn;

    @FXML
    private Label pageLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private TextField manualIssueIdField;

    @FXML
    private TextField manualAmountField;

    @FXML
    private TextField manualReasonField;

    public FineManagementController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bookColumn.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        memberColumn.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        remainingColumn.setCellValueFactory(new PropertyValueFactory<>("remainingAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusFilter.setItems(FXCollections.observableArrayList("All", "PENDING", "PARTIAL", "PAID", "WAIVED"));
        statusFilter.getSelectionModel().selectFirst();

        fineTable.setPlaceholder(TablePlaceholders.noResults("No fines found."));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button payButton = new Button("Pay");
            private final Button waiveButton = new Button("Waive");
            private final HBox box = new HBox(6, payButton, waiveButton);

            {
                payButton.setOnAction(event -> {
                    FineDTO fine = getTableView().getItems().get(getIndex());
                    appContext.setNavigationParameter(fine.id());
                    appContext.getViewNavigator().navigate("/fxml/finance/PaymentCollection.fxml");
                });
                waiveButton.setOnAction(event -> {
                    FineDTO fine = getTableView().getItems().get(getIndex());
                    onWaive(fine);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                FineDTO fine = getTableView().getItems().get(getIndex());
                boolean settled = "PAID".equals(fine.status()) || "WAIVED".equals(fine.status());
                payButton.setDisable(settled);
                waiveButton.setDisable(settled);
                setGraphic(box);
            }
        });

        loadPage(0);
    }

    private void onWaive(FineDTO fine) {
        errorLabel.setText("");
        Long waivedByUserId = appContext.getAuthContext().getCurrentUser().getId();
        appContext.getAsyncExecutor().run(
                () -> appContext.getFineService().waive(fine.id(), waivedByUserId),
                dto -> loadPage(currentPage),
                throwable -> errorLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to waive this fine right now."));
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
    private void onAddManualFine() {
        errorLabel.setText("");
        Long issueId;
        BigDecimal amount;
        try {
            issueId = Long.parseLong(manualIssueIdField.getText().trim());
            amount = new BigDecimal(manualAmountField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Issue ID and amount must be valid numbers.");
            return;
        }

        ManualFineRequestDTO request = new ManualFineRequestDTO(issueId, amount, manualReasonField.getText());
        appContext.getAsyncExecutor().run(
                () -> appContext.getFineService().createManualFine(request),
                dto -> {
                    manualIssueIdField.clear();
                    manualAmountField.clear();
                    manualReasonField.clear();
                    loadPage(0);
                },
                throwable -> errorLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to create this fine right now."));
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }

    private void loadPage(int pageNumber) {
        errorLabel.setText("");
        fineTable.setItems(FXCollections.observableArrayList());
        fineTable.setPlaceholder(TablePlaceholders.loading());
        String status = statusFilter.getValue();
        FineSearchCriteria criteria = FineSearchCriteria.builder()
                .status("All".equals(status) ? null : status)
                .pageNumber(pageNumber)
                .pageSize(PAGE_SIZE)
                .build();

        appContext.getAsyncExecutor().run(
                () -> appContext.getFineService().search(criteria),
                this::onPageLoaded,
                throwable -> {
                    fineTable.setPlaceholder(TablePlaceholders.noResults("No fines found."));
                    errorLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to load fines right now.");
                });
    }

    private void onPageLoaded(Page<FineDTO> page) {
        currentPage = page.getPageNumber();
        totalElements = page.getTotalElements();
        fineTable.setPlaceholder(TablePlaceholders.noResults("No fines found."));
        fineTable.setItems(FXCollections.observableArrayList(page.getContent()));
        int totalPages = Math.max(page.getTotalPages(), 1);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages + " (" + totalElements + " fines)");
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable((long) (currentPage + 1) * PAGE_SIZE >= totalElements);
    }
}
