package com.university.lms.ui.controller.finance;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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
import com.university.lms.dto.request.PaymentRequestDTO;
import com.university.lms.dto.response.FineDTO;
import com.university.lms.dto.response.PaymentDTO;
import com.university.lms.exception.BusinessException;

/** Collects a full or partial payment against a fine and produces a printable PDF receipt. */
public final class PaymentCollectionController implements Initializable {

    private final AppContext appContext;
    private Long fineId;
    private String lastReceiptPath;

    @FXML
    private Label fineSummaryLabel;

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<String> methodCombo;

    @FXML
    private Label messageLabel;

    @FXML
    private Button collectButton;

    @FXML
    private Button openReceiptButton;

    public PaymentCollectionController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Object param = appContext.getNavigationParameter();
        appContext.setNavigationParameter(null);
        fineId = param instanceof Long id ? id : null;

        methodCombo.setItems(FXCollections.observableArrayList("CASH", "CARD", "ONLINE"));
        methodCombo.getSelectionModel().selectFirst();
        messageLabel.setText("");
        openReceiptButton.setDisable(true);

        if (fineId == null) {
            fineSummaryLabel.setText("No fine selected.");
            collectButton.setDisable(true);
            return;
        }
        loadFine();
    }

    private void loadFine() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getFineService().getById(fineId),
                fineOptional -> fineOptional.ifPresentOrElse(this::populateSummary,
                        () -> fineSummaryLabel.setText("Fine not found.")),
                throwable -> messageLabel.setText("Unable to load fine details."));
    }

    private void populateSummary(FineDTO fine) {
        fineSummaryLabel.setText(fine.bookTitle() + " — " + fine.memberName()
                + " | Amount: " + fine.amount() + " | Paid: " + fine.paidAmount()
                + " | Remaining: " + fine.remainingAmount() + " | Status: " + fine.status());
        amountField.setText(fine.remainingAmount().toPlainString());
    }

    @FXML
    private void onCollectPayment() {
        messageLabel.setText("");
        collectButton.setDisable(true);
        openReceiptButton.setDisable(true);

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
        } catch (NumberFormatException e) {
            collectButton.setDisable(false);
            messageLabel.setText("Enter a valid payment amount.");
            return;
        }

        PaymentRequestDTO request = new PaymentRequestDTO(fineId, amount, methodCombo.getValue());
        Long receivedByUserId = appContext.getAuthContext().getCurrentUser().getId();

        appContext.getAsyncExecutor().run(
                () -> appContext.getPaymentService().collectPayment(request, receivedByUserId),
                this::onPaymentCollected,
                throwable -> {
                    collectButton.setDisable(false);
                    messageLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to collect this payment right now.");
                });
    }

    private void onPaymentCollected(PaymentDTO payment) {
        collectButton.setDisable(false);
        lastReceiptPath = payment.receiptFilePath();
        openReceiptButton.setDisable(lastReceiptPath == null);
        messageLabel.setText("Payment collected. Receipt: " + payment.receiptNumber());
        loadFine();
    }

    @FXML
    private void onOpenReceipt() {
        if (lastReceiptPath == null) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(lastReceiptPath));
            } else {
                messageLabel.setText("Receipt saved at: " + lastReceiptPath);
            }
        } catch (IOException e) {
            messageLabel.setText("Unable to open the receipt file. Saved at: " + lastReceiptPath);
        }
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/finance/FineManagement.fxml");
    }
}
