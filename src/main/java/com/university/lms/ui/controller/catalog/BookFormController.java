package com.university.lms.ui.controller.catalog;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.AuthorRequestDTO;
import com.university.lms.dto.request.BookRequestDTO;
import com.university.lms.dto.request.PublisherRequestDTO;
import com.university.lms.dto.response.AuthorDTO;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.dto.response.CategoryDTO;
import com.university.lms.dto.response.PublisherDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.impl.BookValidator;

/** Add/Edit Book form. {@code AppContext.getNavigationParameter()} carries the book id to edit, if any. */
public final class BookFormController implements Initializable {

    private final AppContext appContext;
    private final BookValidator validator = new BookValidator();
    private Long editingBookId;

    @FXML
    private Label headerLabel;

    @FXML
    private TextField isbnField;

    @FXML
    private TextField titleField;

    @FXML
    private TextField subtitleField;

    @FXML
    private TextField editionField;

    @FXML
    private TextField volumeField;

    @FXML
    private TextField languageField;

    @FXML
    private ComboBox<PublisherDTO> publisherCombo;

    @FXML
    private ComboBox<CategoryDTO> categoryCombo;

    @FXML
    private TextField authorNamesField;

    @FXML
    private TextField tagNamesField;

    @FXML
    private TextField costField;

    @FXML
    private DatePicker purchaseDatePicker;

    @FXML
    private TextField vendorField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button saveButton;

    private List<CategoryDTO> flattenedCategories;
    private final Map<Long, Integer> categoryDepths = new HashMap<>();

    public BookFormController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Object param = appContext.getNavigationParameter();
        appContext.setNavigationParameter(null);
        editingBookId = param instanceof Long id ? id : null;
        headerLabel.setText(editingBookId == null ? "Add Book" : "Edit Book");

        publisherCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(PublisherDTO publisher) {
                return publisher == null ? "" : publisher.name();
            }

            @Override
            public PublisherDTO fromString(String string) {
                return null;
            }
        });

        loadReferenceData();
    }

    private void loadReferenceData() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getPublisherService().listAll(),
                publishers -> {
                    publisherCombo.setItems(FXCollections.observableArrayList(publishers));
                    loadCategories();
                },
                throwable -> messageLabel.setText("Unable to load publishers."));
    }

    private void loadCategories() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getCategoryService().listTree(),
                tree -> {
                    flattenedCategories = flatten(tree, 0);
                    categoryCombo.setItems(FXCollections.observableArrayList(flattenedCategories));
                    categoryCombo.setCellFactory(column -> new CategoryListCell());
                    categoryCombo.setButtonCell(new CategoryListCell());
                    if (editingBookId != null) {
                        loadExistingBook();
                    }
                },
                throwable -> messageLabel.setText("Unable to load categories."));
    }

    private void loadExistingBook() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getBookService().getById(editingBookId),
                bookOptional -> bookOptional.ifPresent(this::populateForm),
                throwable -> messageLabel.setText("Unable to load book details."));
    }

    private void populateForm(BookDTO book) {
        isbnField.setText(book.getIsbn());
        titleField.setText(book.getTitle());
        subtitleField.setText(book.getSubtitle());
        editionField.setText(book.getEdition());
        volumeField.setText(book.getVolume());
        languageField.setText(book.getLanguage());
        authorNamesField.setText(String.join(", ", book.getAuthorNames()));
        tagNamesField.setText(String.join(", ", book.getTagNames()));
        costField.setText(book.getCost() != null ? book.getCost().toPlainString() : "");
        purchaseDatePicker.setValue(book.getPurchaseDate());
        vendorField.setText(book.getVendor());

        publisherCombo.getItems().stream()
                .filter(p -> p.name().equals(book.getPublisherName()))
                .findFirst()
                .ifPresent(publisherCombo.getSelectionModel()::select);
        flattenedCategories.stream()
                .filter(c -> c.name().equals(book.getCategoryName()))
                .findFirst()
                .ifPresent(categoryCombo.getSelectionModel()::select);
    }

    @FXML
    private void onSave() {
        messageLabel.setText("");

        BigDecimal cost;
        try {
            cost = costField.getText().isBlank() ? BigDecimal.ZERO : new BigDecimal(costField.getText().trim());
        } catch (NumberFormatException e) {
            messageLabel.setText("Cost must be a valid number.");
            return;
        }

        ValidationResult validation = validator.validate(BookRequestDTO.builder()
                .title(titleField.getText())
                .isbn(isbnField.getText())
                .cost(cost)
                .build());
        if (!validation.isValid()) {
            messageLabel.setText(String.join(" ", validation.getErrors()));
            return;
        }

        saveButton.setDisable(true);
        appContext.getAsyncExecutor().run(this::resolveAuthorIdsAndSave,
                bookDto -> {
                    saveButton.setDisable(false);
                    appContext.getViewNavigator().navigate("/fxml/catalog/BookList.fxml");
                },
                throwable -> {
                    saveButton.setDisable(false);
                    messageLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to save book right now.");
                });
    }

    /** Runs off the FX thread: resolves free-typed author/publisher names to ids, then saves. */
    private BookDTO resolveAuthorIdsAndSave() {
        Set<Long> authorIds = new HashSet<>();
        for (String rawName : authorNamesField.getText().split(",")) {
            String name = rawName.trim();
            if (name.isEmpty()) {
                continue;
            }
            AuthorDTO existing = appContext.getAuthorService().listAll().stream()
                    .filter(a -> a.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
            AuthorDTO author = existing != null ? existing
                    : appContext.getAuthorService().save(new AuthorRequestDTO(null, name, null, null));
            authorIds.add(author.id());
        }

        PublisherDTO selectedPublisher = publisherCombo.getValue();
        Long publisherId = resolvePublisherId(selectedPublisher);

        Set<String> tagNames = new HashSet<>();
        for (String tag : tagNamesField.getText().split(",")) {
            if (!tag.isBlank()) {
                tagNames.add(tag.trim());
            }
        }

        CategoryDTO selectedCategory = categoryCombo.getValue();
        BigDecimal cost = costField.getText().isBlank() ? BigDecimal.ZERO : new BigDecimal(costField.getText().trim());

        BookRequestDTO request = BookRequestDTO.builder()
                .id(editingBookId)
                .isbn(isbnField.getText())
                .title(titleField.getText())
                .subtitle(subtitleField.getText())
                .edition(editionField.getText())
                .volume(volumeField.getText())
                .language(languageField.getText())
                .publisherId(publisherId)
                .categoryId(selectedCategory != null ? selectedCategory.id() : null)
                .authorIds(authorIds)
                .tagNames(tagNames)
                .cost(cost)
                .purchaseDate(purchaseDatePicker.getValue())
                .vendor(vendorField.getText())
                .build();

        return editingBookId == null ? appContext.getBookService().createBook(request)
                : appContext.getBookService().updateBook(request);
    }

    private Long resolvePublisherId(PublisherDTO selected) {
        if (selected != null) {
            return selected.id();
        }
        String editorText = publisherCombo.getEditor().getText();
        if (editorText == null || editorText.isBlank()) {
            return null;
        }
        PublisherDTO created = appContext.getPublisherService().save(
                new PublisherRequestDTO(null, editorText.trim(), null, null, null));
        return created.id();
    }

    private List<CategoryDTO> flatten(List<CategoryDTO> nodes, int depth) {
        List<CategoryDTO> result = new ArrayList<>();
        for (CategoryDTO node : nodes) {
            categoryDepths.put(node.id(), depth);
            result.add(node);
            result.addAll(flatten(node.children(), depth + 1));
        }
        return result;
    }

    @FXML
    private void onCancel() {
        appContext.getViewNavigator().navigate("/fxml/catalog/BookList.fxml");
    }

    private final class CategoryListCell extends ListCell<CategoryDTO> {
        @Override
        protected void updateItem(CategoryDTO item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                int depth = categoryDepths.getOrDefault(item.id(), 0);
                setText("  ".repeat(depth) + item.name());
            }
        }
    }
}
