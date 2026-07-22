package com.university.lms.ui.controller.people;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.StudentSearchCriteria;
import com.university.lms.dto.response.StudentDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.model.Page;
import com.university.lms.ui.util.TablePlaceholders;

/** Student directory: keyword search, and pagination. */
public final class StudentListController implements Initializable {

    private static final int PAGE_SIZE = 25;

    private final AppContext appContext;
    private int currentPage = 0;
    private long totalElements = 0;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<StudentDTO> studentTable;

    @FXML
    private TableColumn<StudentDTO, String> studentIdColumn;

    @FXML
    private TableColumn<StudentDTO, String> nameColumn;

    @FXML
    private TableColumn<StudentDTO, String> rollNumberColumn;

    @FXML
    private TableColumn<StudentDTO, String> departmentColumn;

    @FXML
    private TableColumn<StudentDTO, String> statusColumn;

    @FXML
    private TableColumn<StudentDTO, String> membershipColumn;

    @FXML
    private TableColumn<StudentDTO, Void> actionsColumn;

    @FXML
    private Label pageLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    public StudentListController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        rollNumberColumn.setCellValueFactory(new PropertyValueFactory<>("rollNumber"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        membershipColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMembershipTypeName() == null ? "None" : data.getValue().getMembershipTypeName()));

        studentTable.setPlaceholder(TablePlaceholders.noResults("No students found."));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setOnAction(event -> {
                    StudentDTO student = getTableView().getItems().get(getIndex());
                    appContext.setNavigationParameter(student.getId());
                    appContext.getViewNavigator().navigate("/fxml/people/StudentForm.fxml");
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
    private void onAddStudent() {
        appContext.getViewNavigator().navigate("/fxml/people/StudentForm.fxml");
    }

    @FXML
    private void onBulkImport() {
        appContext.getViewNavigator().navigate("/fxml/people/StudentBulkImport.fxml");
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }

    private void loadPage(int pageNumber) {
        errorLabel.setText("");
        studentTable.setItems(FXCollections.observableArrayList());
        studentTable.setPlaceholder(TablePlaceholders.loading());
        StudentSearchCriteria criteria = StudentSearchCriteria.builder()
                .keyword(searchField.getText())
                .pageNumber(pageNumber)
                .pageSize(PAGE_SIZE)
                .build();

        appContext.getAsyncExecutor().run(
                () -> appContext.getStudentService().search(criteria),
                this::onPageLoaded,
                throwable -> {
                    studentTable.setPlaceholder(TablePlaceholders.noResults("No students found."));
                    errorLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to load students right now.");
                });
    }

    private void onPageLoaded(Page<StudentDTO> page) {
        currentPage = page.getPageNumber();
        totalElements = page.getTotalElements();
        studentTable.setPlaceholder(TablePlaceholders.noResults("No students found."));
        studentTable.setItems(FXCollections.observableArrayList(page.getContent()));
        int totalPages = Math.max(page.getTotalPages(), 1);
        pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages + " (" + totalElements + " students)");
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable((long) (currentPage + 1) * PAGE_SIZE >= totalElements);
    }
}
