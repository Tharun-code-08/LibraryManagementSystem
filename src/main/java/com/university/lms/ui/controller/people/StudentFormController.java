package com.university.lms.ui.controller.people;

import java.io.File;
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
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.MembershipTypeDTO;
import com.university.lms.dto.response.StudentDTO;
import com.university.lms.dto.request.StudentRegistrationRequestDTO;
import com.university.lms.entity.Branch;
import com.university.lms.exception.BusinessException;

/** Add/Edit Student form. {@code AppContext.getNavigationParameter()} carries the student id to edit, if any. */
public final class StudentFormController implements Initializable {

    private final AppContext appContext;
    private Long editingStudentId;
    private String uploadedPhotoPath;

    @FXML
    private Label headerLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField temporaryPasswordField;

    @FXML
    private TextField studentIdField;

    @FXML
    private TextField rollNumberField;

    @FXML
    private TextField departmentField;

    @FXML
    private TextField yearField;

    @FXML
    private TextField semesterField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField guardianNameField;

    @FXML
    private TextField guardianPhoneField;

    @FXML
    private ComboBox<Branch> branchCombo;

    @FXML
    private ComboBox<MembershipTypeDTO> membershipTypeCombo;

    @FXML
    private Label photoLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Button saveButton;

    public StudentFormController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Object param = appContext.getNavigationParameter();
        appContext.setNavigationParameter(null);
        editingStudentId = param instanceof Long id ? id : null;
        headerLabel.setText(editingStudentId == null ? "Add Student" : "Edit Student");
        temporaryPasswordField.setVisible(editingStudentId == null);
        temporaryPasswordField.setManaged(editingStudentId == null);

        branchCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Branch branch) {
                return branch == null ? "" : branch.getName();
            }

            @Override
            public Branch fromString(String string) {
                return null;
            }
        });
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

        loadReferenceData();
    }

    private void loadReferenceData() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getBranchRepository().findAll(),
                branches -> {
                    branchCombo.setItems(FXCollections.observableArrayList(branches));
                    if (!branches.isEmpty()) {
                        branchCombo.getSelectionModel().selectFirst();
                    }
                    loadMembershipTypes();
                },
                throwable -> messageLabel.setText("Unable to load branches."));
    }

    private void loadMembershipTypes() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getMembershipTypeService().listAll(),
                types -> {
                    membershipTypeCombo.setItems(FXCollections.observableArrayList(types));
                    types.stream().filter(t -> t.name().equals("STUDENT_STANDARD")).findFirst()
                            .ifPresent(membershipTypeCombo.getSelectionModel()::select);
                    if (editingStudentId != null) {
                        loadExistingStudent();
                    }
                },
                throwable -> messageLabel.setText("Unable to load membership types."));
    }

    private void loadExistingStudent() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getStudentService().getById(editingStudentId),
                studentOptional -> studentOptional.ifPresent(this::populateForm),
                throwable -> messageLabel.setText("Unable to load student details."));
    }

    private void populateForm(StudentDTO student) {
        usernameField.setText(student.getUsername());
        usernameField.setDisable(true);
        emailField.setText(student.getEmail());
        studentIdField.setText(student.getStudentId());
        rollNumberField.setText(student.getRollNumber());
        departmentField.setText(student.getDepartment());
        yearField.setText(student.getYear() != null ? student.getYear().toString() : "");
        semesterField.setText(student.getSemester() != null ? student.getSemester().toString() : "");
        phoneField.setText(student.getPhone());
        addressField.setText(student.getAddress());
        guardianNameField.setText(student.getGuardianName());
        guardianPhoneField.setText(student.getGuardianPhone());
        photoLabel.setText(student.getPhotoPath() != null ? "Current photo: " + student.getPhotoPath() : "No photo");

        branchCombo.getItems().stream()
                .filter(b -> b.getName().equals(student.getBranchName()))
                .findFirst()
                .ifPresent(branchCombo.getSelectionModel()::select);
        membershipTypeCombo.getItems().stream()
                .filter(t -> t.name().equals(student.getMembershipTypeName()))
                .findFirst()
                .ifPresent(membershipTypeCombo.getSelectionModel()::select);
    }

    @FXML
    private void onChoosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Student Photo");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(photoLabel.getScene().getWindow());
        if (file == null) {
            return;
        }
        String fileNameKey = "student-" + (editingStudentId != null ? editingStudentId : System.currentTimeMillis());
        appContext.getAsyncExecutor().run(
                () -> appContext.getPhotoStorageUtil().store(file.toPath(), fileNameKey),
                storedPath -> {
                    uploadedPhotoPath = storedPath;
                    photoLabel.setText("Selected: " + file.getName());
                },
                throwable -> messageLabel.setText("Unable to store the selected photo."));
    }

    @FXML
    private void onSave() {
        messageLabel.setText("");
        saveButton.setDisable(true);

        Branch branch = branchCombo.getValue();
        MembershipTypeDTO membershipType = membershipTypeCombo.getValue();

        StudentRegistrationRequestDTO request = StudentRegistrationRequestDTO.builder()
                .id(editingStudentId)
                .username(usernameField.getText())
                .email(emailField.getText())
                .temporaryPassword(temporaryPasswordField.getText())
                .studentId(studentIdField.getText())
                .rollNumber(rollNumberField.getText())
                .department(departmentField.getText())
                .year(parseIntOrNull(yearField.getText()))
                .semester(parseIntOrNull(semesterField.getText()))
                .phone(phoneField.getText())
                .address(addressField.getText())
                .guardianName(guardianNameField.getText())
                .guardianPhone(guardianPhoneField.getText())
                .branchId(branch != null ? branch.getId() : null)
                .photoPath(uploadedPhotoPath)
                .membershipTypeId(membershipType != null ? membershipType.id() : null)
                .build();

        appContext.getAsyncExecutor().run(
                () -> editingStudentId == null ? appContext.getStudentService().register(request)
                        : appContext.getStudentService().update(request),
                studentDto -> {
                    saveButton.setDisable(false);
                    appContext.getViewNavigator().navigate("/fxml/people/StudentList.fxml");
                },
                throwable -> {
                    saveButton.setDisable(false);
                    messageLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to save student right now.");
                });
    }

    private Integer parseIntOrNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return Integer.parseInt(text.trim());
    }

    @FXML
    private void onCancel() {
        appContext.getViewNavigator().navigate("/fxml/people/StudentList.fxml");
    }
}
