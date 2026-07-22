package com.university.lms.ui.controller.circulation;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.ReturnRequestDTO;
import com.university.lms.dto.response.ReturnResultDTO;
import com.university.lms.exception.BusinessException;

/** Return Book screen: scan the copy barcode, record its condition, and see any fine owed. */
public final class ReturnController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TextField copyBarcodeField;

    @FXML
    private ComboBox<String> conditionCombo;

    @FXML
    private TextField notesField;

    @FXML
    private Label resultLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button returnButton;

    public ReturnController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conditionCombo.setItems(FXCollections.observableArrayList("GOOD", "DAMAGED", "LOST"));
        conditionCombo.getSelectionModel().selectFirst();
        resultLabel.setText("");
        errorLabel.setText("");
        copyBarcodeField.requestFocus();
    }

    @FXML
    private void onReturn() {
        errorLabel.setText("");
        resultLabel.setText("");
        returnButton.setDisable(true);

        ReturnRequestDTO request = new ReturnRequestDTO(
                copyBarcodeField.getText(), conditionCombo.getValue(), notesField.getText());
        Long receivedByUserId = appContext.getAuthContext().getCurrentUser().getId();

        appContext.getAsyncExecutor().run(
                () -> appContext.getReturnService().returnBook(request, receivedByUserId),
                this::onReturnSuccess,
                throwable -> {
                    returnButton.setDisable(false);
                    errorLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to process this return right now.");
                });
    }

    private void onReturnSuccess(ReturnResultDTO result) {
        returnButton.setDisable(false);
        StringBuilder message = new StringBuilder("Returned \"" + result.bookTitle() + "\" from " + result.memberName() + ".");
        if (result.fineAmount().compareTo(BigDecimal.ZERO) > 0) {
            message.append(" Fine due: ").append(result.fineAmount());
        }
        if (result.reservationPromoted()) {
            message.append(" This copy is now reserved for the next member in the waiting queue.");
        }
        resultLabel.setText(message.toString());

        copyBarcodeField.clear();
        notesField.clear();
        conditionCombo.getSelectionModel().selectFirst();
        copyBarcodeField.requestFocus();
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
