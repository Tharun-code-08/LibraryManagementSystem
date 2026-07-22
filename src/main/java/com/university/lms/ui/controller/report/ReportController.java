package com.university.lms.ui.controller.report;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.SimpleStringProperty;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.ReportCriteriaDTO;
import com.university.lms.dto.response.ReportDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.service.report.ExportFormat;
import com.university.lms.service.report.ReportType;

/** Generic reports screen: pick a report type and filters, preview a table, then export/print. */
public final class ReportController implements Initializable {

    private final AppContext appContext;
    private ReportDTO currentReport;

    @FXML
    private ComboBox<ReportType> reportTypeCombo;

    @FXML
    private TextField departmentField;

    @FXML
    private TextField yearField;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Label titleLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private TableView<java.util.Map<String, String>> reportTable;

    @FXML
    private Button exportPdfButton;

    @FXML
    private Button exportExcelButton;

    @FXML
    private Button printButton;

    public ReportController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reportTypeCombo.setItems(FXCollections.observableArrayList(ReportType.values()));
        reportTypeCombo.getSelectionModel().selectFirst();
        setExportButtonsDisabled(true);
    }

    @FXML
    private void onGenerate() {
        errorLabel.setText("");
        ReportType reportType = reportTypeCombo.getValue();
        if (reportType == null) {
            errorLabel.setText("Choose a report type.");
            return;
        }

        Integer year = null;
        if (yearField.getText() != null && !yearField.getText().isBlank()) {
            try {
                year = Integer.parseInt(yearField.getText().trim());
            } catch (NumberFormatException e) {
                errorLabel.setText("Year must be a whole number.");
                return;
            }
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        String department = departmentField.getText() == null || departmentField.getText().isBlank()
                ? null : departmentField.getText().trim();

        ReportCriteriaDTO criteria = ReportCriteriaDTO.builder()
                .reportType(reportType)
                .department(department)
                .year(year)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();

        setExportButtonsDisabled(true);
        appContext.getAsyncExecutor().run(
                () -> appContext.getReportService().generate(criteria),
                this::onReportGenerated,
                throwable -> errorLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to generate this report right now."));
    }

    private void onReportGenerated(ReportDTO report) {
        this.currentReport = report;
        titleLabel.setText(report.title() + " (" + report.rows().size() + " rows)");
        renderTable(report);
        setExportButtonsDisabled(report.rows().isEmpty());
    }

    private void renderTable(ReportDTO report) {
        reportTable.getColumns().clear();
        List<String> headers = report.columnHeaders();
        for (int i = 0; i < headers.size(); i++) {
            TableColumn<java.util.Map<String, String>, String> column = new TableColumn<>(headers.get(i));
            String key = String.valueOf(i);
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(key, "")));
            column.setPrefWidth(140);
            reportTable.getColumns().add(column);
        }

        var items = FXCollections.<java.util.Map<String, String>>observableArrayList();
        for (List<String> row : report.rows()) {
            var rowMap = new java.util.HashMap<String, String>();
            for (int i = 0; i < row.size(); i++) {
                rowMap.put(String.valueOf(i), row.get(i));
            }
            items.add(rowMap);
        }
        reportTable.setItems(items);
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
        if (currentReport == null) {
            return;
        }
        errorLabel.setText("");
        appContext.getAsyncExecutor().run(
                () -> appContext.getReportService().export(currentReport, format),
                this::openFile,
                throwable -> errorLabel.setText("Unable to export this report right now."));
    }

    @FXML
    private void onPrint() {
        if (currentReport == null) {
            return;
        }
        errorLabel.setText("");
        appContext.getAsyncExecutor().run(
                () -> appContext.getReportService().export(currentReport, ExportFormat.PDF),
                this::printFile,
                throwable -> errorLabel.setText("Unable to print this report right now."));
    }

    private void openFile(String path) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(path));
            }
        } catch (IOException e) {
            errorLabel.setText("Report exported to " + path + " but could not be opened automatically.");
        }
    }

    private void printFile(String path) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.PRINT)) {
                Desktop.getDesktop().print(new File(path));
            } else {
                errorLabel.setText("Printing is not supported on this system; report saved to " + path);
            }
        } catch (IOException e) {
            errorLabel.setText("Unable to send the report to the printer.");
        }
    }

    private void setExportButtonsDisabled(boolean disabled) {
        exportPdfButton.setDisable(disabled);
        exportExcelButton.setDisable(disabled);
        printButton.setDisable(disabled);
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
