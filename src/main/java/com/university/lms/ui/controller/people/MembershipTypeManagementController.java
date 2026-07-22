package com.university.lms.ui.controller.people;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.MembershipTypeRequestDTO;
import com.university.lms.dto.response.MembershipTypeDTO;
import com.university.lms.exception.BusinessException;
import com.university.lms.ui.util.TablePlaceholders;

/** Membership type reference-data management: max borrow limit, loan period, fine rules. */
public final class MembershipTypeManagementController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TableView<MembershipTypeDTO> typeTable;

    @FXML
    private TableColumn<MembershipTypeDTO, String> nameColumn;

    @FXML
    private TableColumn<MembershipTypeDTO, Integer> borrowLimitColumn;

    @FXML
    private TableColumn<MembershipTypeDTO, Integer> loanPeriodColumn;

    @FXML
    private TableColumn<MembershipTypeDTO, BigDecimal> finePerDayColumn;

    @FXML
    private TextField nameField;

    @FXML
    private TextField maxBorrowLimitField;

    @FXML
    private TextField loanPeriodDaysField;

    @FXML
    private TextField finePerDayField;

    @FXML
    private TextField gracePeriodDaysField;

    @FXML
    private TextField renewalLimitField;

    @FXML
    private Label messageLabel;

    public MembershipTypeManagementController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        borrowLimitColumn.setCellValueFactory(new PropertyValueFactory<>("maxBorrowLimit"));
        loanPeriodColumn.setCellValueFactory(new PropertyValueFactory<>("loanPeriodDays"));
        finePerDayColumn.setCellValueFactory(new PropertyValueFactory<>("finePerDay"));
        typeTable.setPlaceholder(TablePlaceholders.noResults("No membership types found."));
        loadTypes();
    }

    private void loadTypes() {
        typeTable.setItems(FXCollections.observableArrayList());
        typeTable.setPlaceholder(TablePlaceholders.loading());
        appContext.getAsyncExecutor().run(
                () -> appContext.getMembershipTypeService().listAll(),
                types -> {
                    typeTable.setPlaceholder(TablePlaceholders.noResults("No membership types found."));
                    typeTable.setItems(FXCollections.observableArrayList(types));
                },
                throwable -> {
                    typeTable.setPlaceholder(TablePlaceholders.noResults("No membership types found."));
                    messageLabel.setText("Unable to load membership types.");
                });
    }

    @FXML
    private void onSave() {
        messageLabel.setText("");
        try {
            MembershipTypeRequestDTO request = new MembershipTypeRequestDTO(
                    null,
                    nameField.getText(),
                    Integer.parseInt(maxBorrowLimitField.getText().trim()),
                    Integer.parseInt(loanPeriodDaysField.getText().trim()),
                    new BigDecimal(finePerDayField.getText().trim()),
                    gracePeriodDaysField.getText().isBlank() ? 0 : Integer.parseInt(gracePeriodDaysField.getText().trim()),
                    renewalLimitField.getText().isBlank() ? 0 : Integer.parseInt(renewalLimitField.getText().trim()));

            appContext.getAsyncExecutor().run(
                    () -> appContext.getMembershipTypeService().save(request),
                    dto -> {
                        nameField.clear();
                        maxBorrowLimitField.clear();
                        loanPeriodDaysField.clear();
                        finePerDayField.clear();
                        gracePeriodDaysField.clear();
                        renewalLimitField.clear();
                        loadTypes();
                    },
                    throwable -> messageLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to save membership type."));
        } catch (NumberFormatException e) {
            messageLabel.setText("Borrow limit, loan period, fine, grace period, and renewal limit must be numbers.");
        }
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
