package com.university.lms.ui.controller.people;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.FacultyRegistrationRequestDTO;
import com.university.lms.dto.response.FacultyDTO;
import com.university.lms.dto.response.MembershipTypeDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.validation.ValidationResult;
import com.university.lms.validation.impl.FacultyValidator;

/** Add/Edit Faculty form. {@code AppContext.getNavigationParameter()} carries the faculty id to edit, if any. */
public final class FacultyFormController implements Initializable {

    private final AppContext appContext;
    private final FacultyValidator validator = new FacultyValidator();
    private Long editingFacultyId;

    @FXML
    private Label headerLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField temporaryPasswordField;

    @FXML
    private TextField facultyIdField;

    @FXML
    private TextField departmentField;

    @FXML
    private TextField designationField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField officeField;

    @FXML
    private ComboBox<MembershipTypeDTO> membershipTypeCombo;

    @FXML
    private Label messageLabel;

    @FXML
    private Button saveButton;

    public FacultyFormController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Object param = appContext.getNavigationParameter();
        appContext.setNavigationParameter(null);
        editingFacultyId = param instanceof Long id ? id : null;
        headerLabel.setText(editingFacultyId == null ? "Add Faculty" : "Edit Faculty");
        temporaryPasswordField.setVisible(editingFacultyId == null);
        temporaryPasswordField.setManaged(editingFacultyId == null);

        membershipTypeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(MembershipTypeDTO type) {
                return type == null ? "" : type.name();
            }

            @Override
            public MembershipTypeDTO fromString(String string) {
                return null;
            }
        });

        loadMembershipTypes();
    }

    private void loadMembershipTypes() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getMembershipTypeService().listAll(),
                types -> {
                    membershipTypeCombo.setItems(FXCollections.observableArrayList(types));
                    types.stream().filter(t -> t.name().equals("FACULTY_STANDARD")).findFirst()
                            .ifPresent(membershipTypeCombo.getSelectionModel()::select);
                    if (editingFacultyId != null) {
                        loadExistingFaculty();
                    }
                },
                throwable -> messageLabel.setText("Unable to load membership types."));
    }

    private void loadExistingFaculty() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getFacultyService().getById(editingFacultyId),
                facultyOptional -> facultyOptional.ifPresent(this::populateForm),
                throwable -> messageLabel.setText("Unable to load faculty details."));
    }

    private void populateForm(FacultyDTO faculty) {
        usernameField.setText(faculty.getUsername());
        usernameField.setDisable(true);
        emailField.setText(faculty.getEmail());
        facultyIdField.setText(faculty.getFacultyId());
        departmentField.setText(faculty.getDepartment());
        designationField.setText(faculty.getDesignation());
        phoneField.setText(faculty.getPhone());
        officeField.setText(faculty.getOffice());

        membershipTypeCombo.getItems().stream()
                .filter(t -> t.name().equals(faculty.getMembershipTypeName()))
                .findFirst()
                .ifPresent(membershipTypeCombo.getSelectionModel()::select);
    }

    @FXML
    private void onSave() {
        messageLabel.setText("");

        MembershipTypeDTO membershipType = membershipTypeCombo.getValue();

        FacultyRegistrationRequestDTO request = FacultyRegistrationRequestDTO.builder()
                .id(editingFacultyId)
                .username(usernameField.getText())
                .email(emailField.getText())
                .temporaryPassword(temporaryPasswordField.getText())
                .facultyId(facultyIdField.getText())
                .department(departmentField.getText())
                .designation(designationField.getText())
                .phone(phoneField.getText())
                .office(officeField.getText())
                .membershipTypeId(membershipType != null ? membershipType.id() : null)
                .build();

        ValidationResult validation = validator.validate(request);
        if (!validation.isValid()) {
            messageLabel.setText(String.join(" ", validation.getErrors()));
            return;
        }

        saveButton.setDisable(true);
        appContext.getAsyncExecutor().run(
                () -> editingFacultyId == null ? appContext.getFacultyService().register(request)
                        : appContext.getFacultyService().update(request),
                facultyDto -> {
                    saveButton.setDisable(false);
                    appContext.getViewNavigator().navigate("/fxml/people/FacultyList.fxml");
                },
                throwable -> {
                    saveButton.setDisable(false);
                    messageLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to save faculty right now.");
                });
    }

    @FXML
    private void onCancel() {
        appContext.getViewNavigator().navigate("/fxml/people/FacultyList.fxml");
    }
}
