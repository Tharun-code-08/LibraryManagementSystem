package com.university.lms.ui.controller.catalog;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.BookImportRowDTO;
import com.university.lms.dto.response.ImportResultDTO;
import com.university.lms.util.CsvImportUtil;

/** Bulk book-catalog import from a CSV file, with a per-row rejected-rows report. */
public final class BulkImportController implements Initializable {

    private final AppContext appContext;
    private java.io.File selectedFile;

    @FXML
    private Label selectedFileLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private ListView<String> rejectedRowsList;

    @FXML
    private Button importButton;

    public BulkImportController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedFileLabel.setText("No file selected.");
        summaryLabel.setText("");
        importButton.setDisable(true);
    }

    @FXML
    private void onChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Book Import CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = chooser.showOpenDialog(selectedFileLabel.getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            selectedFileLabel.setText(file.getName());
            importButton.setDisable(false);
        }
    }

    @FXML
    private void onImport() {
        if (selectedFile == null) {
            return;
        }
        importButton.setDisable(true);
        summaryLabel.setText("Importing...");

        appContext.getAsyncExecutor().run(
                () -> {
                    try (Reader reader = new FileReader(selectedFile, StandardCharsets.UTF_8)) {
                        List<BookImportRowDTO> rows = CsvImportUtil.parseBookRows(reader);
                        return appContext.getBookService().bulkImport(rows);
                    } catch (IOException e) {
                        throw new IllegalStateException("Unable to read the selected file.", e);
                    }
                },
                this::onImportComplete,
                throwable -> {
                    importButton.setDisable(false);
                    summaryLabel.setText("Import failed: " + throwable.getMessage());
                });
    }

    private void onImportComplete(ImportResultDTO result) {
        importButton.setDisable(false);
        summaryLabel.setText(result.successCount() + " book(s) imported, "
                + result.rejectedRows().size() + " row(s) rejected.");
        rejectedRowsList.setItems(FXCollections.observableArrayList(
                result.rejectedRows().stream()
                        .map(row -> "Row " + row.rowNumber() + ": " + row.reason())
                        .toList()));
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/catalog/BookList.fxml");
    }
}
