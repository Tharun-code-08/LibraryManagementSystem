package com.university.lms.ui.controller.admin;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.AuditLogSearchCriteria;
import com.university.lms.dto.response.AuditLogEntryDTO;
import com.university.lms.dto.response.ReportDTO;
import com.university.lms.model.Page;
import com.university.lms.ui.util.TablePlaceholders;
import com.university.lms.util.ExportFormat;

/** Admin screen: a filterable, paginated view over the append-only audit trail, with export. */
public final class AuditLogController implements Initializable {

    private static final int PAGE_SIZE = 25;

    private final AppContext appContext;
    private int currentPage = 0;
    private long totalElements = 0;
    private List<AuditLogEntryDTO> currentEntries = List.of();

    @FXML
    private TextField entityTypeField;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private TableView<AuditLogEntryDTO> auditLogTable;

    @FXML
    private TableColumn<AuditLogEntryDTO, String> actorColumn;

    @FXML
    private TableColumn<AuditLogEntryDTO, String> actionColumn;

    @FXML
    private TableColumn<AuditLogEntryDTO, String> entityTypeColumn;

    @FXML
    private TableColumn<AuditLogEntryDTO, Long> entityIdColumn;

    @FXML
    private TableColumn<AuditLogEntryDTO, String> createdAtColumn;

    @FXML
    private Label pageLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    public AuditLogController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        actorColumn.setCellValueFactory(new PropertyValueFactory<>("actorUsername"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        entityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        entityIdColumn.setCellValueFactory(new PropertyValueFactory<>("entityId"));
        createdAtColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().createdAt())));
        auditLogTable.setPlaceholder(TablePlaceholders.noResults("No audit log entries found."));

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

    private void loadPage(int pageNumber) {
        errorLabel.setText("");
        auditLogTable.setItems(FXCollections.observableArrayList());
        auditLogTable.setPlaceholder(TablePlaceholders.loading());
        LocalDateTime from = fromDatePicker.getValue() != null ? fromDatePicker.getValue().atStartOfDay() : null;
        LocalDateTime to = toDatePicker.getValue() != null ? toDatePicker.getValue().atTime(23, 59, 59) : null;
        String entityType = entityTypeField.getText() == null || entityTypeField.getText().isBlank()
                ? null : entityTypeField.getText().trim();

        AuditLogSearchCriteria criteria = AuditLogSearchCriteria.builder()
                .entityType(entityType)
                .fromDate(from)
                .toDate(to)
                .pageNumber(pageNumber)
                .pageSize(PAGE_SIZE)
                .build();

        appContext.getAsyncExecutor().run(
                () -> appContext.getAuditLogService().search(criteria),
                this::onPageLoaded,
                throwable -> {
                    auditLogTable.setPlaceholder(TablePlaceholders.noResults("No audit log entries found."));
                    errorLabel.setText("Unable to load the audit log right now.");
                });
    }

    private void onPageLoaded(Page<AuditLogEntryDTO> page) {
        currentPage = page.getPageNumber();
        totalElements = page.getTotalElements();
        currentEntries = page.getContent();
        auditLogTable.setPlaceholder(TablePlaceholders.noResults("No audit log entries found."));
        auditLogTable.setItems(FXCollections.observableArrayList(currentEntries));
        int totalPages = Math.max(page.getTotalPages(), 1);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages + " (" + totalElements + " entries)");
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable((long) (currentPage + 1) * PAGE_SIZE >= totalElements);
    }

    @FXML
    private void onExportPdf() {
        exportAndOpen(ExportFormat.PDF);
    }

    @FXML
    private void onExportExcel() {
        exportAndOpen(ExportFormat.EXCEL);
    }

    private void exportAndOpen(ExportFormat format) {
        errorLabel.setText("");
        List<List<String>> rows = currentEntries.stream()
                .map(entry -> List.of(
                        String.valueOf(entry.id()),
                        entry.actorUsername(),
                        entry.action(),
                        entry.entityType(),
                        entry.entityId() == null ? "" : String.valueOf(entry.entityId()),
                        String.valueOf(entry.createdAt())))
                .toList();
        ReportDTO report = new ReportDTO("Audit Log",
                List.of("ID", "Actor", "Action", "Entity Type", "Entity ID", "Created At"),
                rows, LocalDateTime.now());

        appContext.getAsyncExecutor().run(
                () -> appContext.getReportService().export(report, format),
                this::openFile,
                throwable -> errorLabel.setText("Unable to export the audit log right now."));
    }

    private void openFile(String path) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(path));
            }
        } catch (IOException e) {
            errorLabel.setText("Exported to " + path + " but could not be opened automatically.");
        }
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
