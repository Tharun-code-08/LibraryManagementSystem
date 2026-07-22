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
import javafx.scene.control.cell.PropertyValueFactory;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.FacultyDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.ui.util.TablePlaceholders;

/** Faculty directory: a simple full listing (faculty headcount is small relative to students). */
public final class FacultyListController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TableView<FacultyDTO> facultyTable;

    @FXML
    private TableColumn<FacultyDTO, String> facultyIdColumn;

    @FXML
    private TableColumn<FacultyDTO, String> nameColumn;

    @FXML
    private TableColumn<FacultyDTO, String> departmentColumn;

    @FXML
    private TableColumn<FacultyDTO, String> designationColumn;

    @FXML
    private TableColumn<FacultyDTO, String> membershipColumn;

    @FXML
    private TableColumn<FacultyDTO, Void> actionsColumn;

    @FXML
    private Label errorLabel;

    public FacultyListController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        facultyIdColumn.setCellValueFactory(new PropertyValueFactory<>("facultyId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        designationColumn.setCellValueFactory(new PropertyValueFactory<>("designation"));
        membershipColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getMembershipTypeName() == null ? "None" : data.getValue().getMembershipTypeName()));

        facultyTable.setPlaceholder(TablePlaceholders.noResults("No faculty found."));

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");

            {
                editButton.setOnAction(event -> {
                    FacultyDTO faculty = getTableView().getItems().get(getIndex());
                    appContext.setNavigationParameter(faculty.getId());
                    appContext.getViewNavigator().navigate("/fxml/people/FacultyForm.fxml");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);
            }
        });

        loadFaculty();
    }

    private void loadFaculty() {
        errorLabel.setText("");
        facultyTable.setItems(FXCollections.observableArrayList());
        facultyTable.setPlaceholder(TablePlaceholders.loading());
        appContext.getAsyncExecutor().run(
                () -> appContext.getFacultyService().listAll(),
                list -> {
                    facultyTable.setPlaceholder(TablePlaceholders.noResults("No faculty found."));
                    facultyTable.setItems(FXCollections.observableArrayList(list));
                },
                throwable -> {
                    facultyTable.setPlaceholder(TablePlaceholders.noResults("No faculty found."));
                    errorLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to load faculty right now.");
                });
    }

    @FXML
    private void onAddFaculty() {
        appContext.getViewNavigator().navigate("/fxml/people/FacultyForm.fxml");
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
