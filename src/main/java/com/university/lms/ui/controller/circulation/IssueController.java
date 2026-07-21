package com.university.lms.ui.controller.circulation;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.IssueRequestDTO;
import com.university.lms.dto.response.IssueResultDTO;
import com.university.lms.exception.BusinessException;

/**
 * Issue Book screen. Both fields accept either a barcode-scanner "type + Enter" burst or manual
 * typing followed by pressing Enter/clicking Issue — the scanner is just a fast keyboard.
 */
public final class IssueController implements Initializable {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private final AppContext appContext;

    @FXML
    private TextField memberIdentifierField;

    @FXML
    private TextField copyBarcodeField;

    @FXML
    private Label resultLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button issueButton;

    public IssueController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resultLabel.setText("");
        errorLabel.setText("");
        memberIdentifierField.requestFocus();
    }

    @FXML
    private void onIssue() {
        errorLabel.setText("");
        resultLabel.setText("");
        issueButton.setDisable(true);

        IssueRequestDTO request = new IssueRequestDTO(memberIdentifierField.getText(), copyBarcodeField.getText());
        Long issuedByUserId = appContext.getAuthContext().getCurrentUser().getId();

        appContext.getAsyncExecutor().run(
                () -> appContext.getIssueService().issueBook(request, issuedByUserId),
                this::onIssueSuccess,
                throwable -> {
                    issueButton.setDisable(false);
                    errorLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to issue this book right now.");
                });
    }

    private void onIssueSuccess(IssueResultDTO result) {
        issueButton.setDisable(false);
        resultLabel.setText("Issued \"" + result.bookTitle() + "\" to " + result.memberName()
                + " — due " + result.dueDate().format(DATE_FORMAT));
        copyBarcodeField.clear();
        copyBarcodeField.requestFocus();
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
