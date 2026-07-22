package com.university.lms.ui.controller.catalog;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.CategoryRequestDTO;
import com.university.lms.dto.response.CategoryDTO;
import com.university.lms.exception.BusinessException;

/** Nested category management: add root/sub-categories, edit, and delete leaf categories. */
public final class CategoryManagementController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TreeView<CategoryDTO> categoryTree;

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Label messageLabel;

    @FXML
    private Button deleteButton;

    public CategoryManagementController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryTree.setShowRoot(false);
        categoryTree.setRoot(new TreeItem<>());
        deleteButton.setDisable(true);
        categoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) ->
                deleteButton.setDisable(newItem == null || newItem.getValue() == null));
        loadTree();
    }

    private void loadTree() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getCategoryService().listTree(),
                this::populateTree,
                throwable -> messageLabel.setText("Unable to load categories."));
    }

    private void populateTree(java.util.List<CategoryDTO> roots) {
        TreeItem<CategoryDTO> root = new TreeItem<>();
        root.setExpanded(true);
        for (CategoryDTO category : roots) {
            root.getChildren().add(buildNode(category));
        }
        categoryTree.setRoot(root);
    }

    private TreeItem<CategoryDTO> buildNode(CategoryDTO category) {
        TreeItem<CategoryDTO> node = new TreeItem<>(category);
        node.setExpanded(true);
        for (CategoryDTO child : category.children()) {
            node.getChildren().add(buildNode(child));
        }
        return node;
    }

    @FXML
    private void onAddRootCategory() {
        saveCategory(null);
    }

    @FXML
    private void onAddSubCategory() {
        TreeItem<CategoryDTO> selected = categoryTree.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null) {
            messageLabel.setText("Select a parent category first.");
            return;
        }
        saveCategory(selected.getValue().id());
    }

    private void saveCategory(Long parentId) {
        messageLabel.setText("");
        String name = nameField.getText();
        String description = descriptionField.getText();
        if (name == null || name.isBlank()) {
            messageLabel.setText("Category name is required.");
            return;
        }

        CategoryRequestDTO request = new CategoryRequestDTO(null, name.trim(), description, parentId);
        appContext.getAsyncExecutor().run(
                () -> appContext.getCategoryService().save(request),
                dto -> {
                    nameField.clear();
                    descriptionField.clear();
                    loadTree();
                },
                throwable -> messageLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to save category."));
    }

    @FXML
    private void onDelete() {
        TreeItem<CategoryDTO> selected = categoryTree.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null) {
            return;
        }
        appContext.getAsyncExecutor().run(
                () -> {
                    appContext.getCategoryService().delete(selected.getValue().id());
                    return null;
                },
                ignored -> loadTree(),
                throwable -> messageLabel.setText(throwable instanceof BusinessException
                        ? throwable.getMessage() : "Unable to delete category."));
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/catalog/BookList.fxml");
    }
}
