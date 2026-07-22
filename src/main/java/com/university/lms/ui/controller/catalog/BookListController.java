package com.university.lms.ui.controller.catalog;

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
import javafx.scene.control.TableCell;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.BookSearchCriteria;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.model.Page;
import com.university.lms.ui.util.TablePlaceholders;

/** Book catalog list: keyword search, category/status filters, and pagination. */
public final class BookListController implements Initializable {

    private static final int PAGE_SIZE = 25;

    private final AppContext appContext;
    private int currentPage = 0;
    private long totalElements = 0;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TableView<BookDTO> bookTable;

    @FXML
    private TableColumn<BookDTO, String> titleColumn;

    @FXML
    private TableColumn<BookDTO, String> isbnColumn;

    @FXML
    private TableColumn<BookDTO, String> authorsColumn;

    @FXML
    private TableColumn<BookDTO, String> categoryColumn;

    @FXML
    private TableColumn<BookDTO, String> copiesColumn;

    @FXML
    private TableColumn<BookDTO, Void> actionsColumn;

    @FXML
    private Label pageLabel;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label errorLabel;

    public BookListController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        authorsColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.join(", ", data.getValue().getAuthorNames())));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        copiesColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getAvailableCopies() + " / " + data.getValue().getTotalCopies()));

        statusFilter.setItems(FXCollections.observableArrayList("All", "AVAILABLE", "ISSUED", "RESERVED", "MAINTENANCE", "LOST"));
        statusFilter.getSelectionModel().selectFirst();

        bookTable.setPlaceholder(TablePlaceholders.noResults("No books found."));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setOnAction(event -> {
                    BookDTO book = getTableView().getItems().get(getIndex());
                    appContext.setNavigationParameter(book.getId());
                    appContext.getViewNavigator().navigate("/fxml/catalog/BookForm.fxml");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);
            }
        });

        loadPage(0);
    }

    @FXML
    private void onSearch() {
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
    private void onAddBook() {
        appContext.getViewNavigator().navigate("/fxml/catalog/BookForm.fxml");
    }

    @FXML
    private void onBulkImport() {
        appContext.getViewNavigator().navigate("/fxml/catalog/BulkImport.fxml");
    }

    @FXML
    private void onManageCategories() {
        appContext.getViewNavigator().navigate("/fxml/catalog/CategoryManagement.fxml");
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }

    private void loadPage(int pageNumber) {
        errorLabel.setText("");
        bookTable.setItems(FXCollections.observableArrayList());
        bookTable.setPlaceholder(TablePlaceholders.loading());
        String status = statusFilter.getValue();
        BookSearchCriteria criteria = BookSearchCriteria.builder()
                .keyword(searchField.getText())
                .status("All".equals(status) ? null : status)
                .pageNumber(pageNumber)
                .pageSize(PAGE_SIZE)
                .build();

        appContext.getAsyncExecutor().run(
                () -> appContext.getBookService().search(criteria),
                this::onPageLoaded,
                throwable -> {
                    bookTable.setPlaceholder(TablePlaceholders.noResults("No books found."));
                    errorLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to load books right now.");
                });
    }

    private void onPageLoaded(Page<BookDTO> page) {
        currentPage = page.getPageNumber();
        totalElements = page.getTotalElements();
        bookTable.setPlaceholder(TablePlaceholders.noResults("No books found."));
        bookTable.setItems(FXCollections.observableArrayList(page.getContent()));
        int totalPages = Math.max(page.getTotalPages(), 1);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages + " (" + totalElements + " books)");
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable((long) (currentPage + 1) * PAGE_SIZE >= totalElements);
    }
}
