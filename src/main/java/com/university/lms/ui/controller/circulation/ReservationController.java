package com.university.lms.ui.controller.circulation;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import com.university.lms.config.AppContext;
import com.university.lms.dto.request.BookSearchCriteria;
import com.university.lms.dto.request.ReservationRequestDTO;
import com.university.lms.dto.response.BookDTO;
import com.university.lms.dto.response.ReservationDTO;
import com.university.lms.exception.BusinessException;

/** Reserve Book screen: search the catalog, pick a title, and join its waiting queue for a member. */
public final class ReservationController implements Initializable {

    private final AppContext appContext;

    @FXML
    private TextField bookSearchField;

    @FXML
    private ListView<BookDTO> searchResultsList;

    @FXML
    private TextField memberIdentifierField;

    @FXML
    private Label resultLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button reserveButton;

    public ReservationController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resultLabel.setText("");
        errorLabel.setText("");
        searchResultsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(BookDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle() + " (" + item.getIsbn() + ")");
            }
        });
    }

    @FXML
    private void onSearchBooks() {
        errorLabel.setText("");
        BookSearchCriteria criteria = BookSearchCriteria.builder()
                .keyword(bookSearchField.getText())
                .pageNumber(0)
                .pageSize(20)
                .build();

        appContext.getAsyncExecutor().run(
                () -> appContext.getBookService().search(criteria),
                page -> searchResultsList.setItems(FXCollections.observableArrayList(page.getContent())),
                throwable -> errorLabel.setText("Unable to search books right now."));
    }

    @FXML
    private void onReserve() {
        BookDTO selectedBook = searchResultsList.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            errorLabel.setText("Select a book from the search results first.");
            return;
        }

        errorLabel.setText("");
        resultLabel.setText("");
        reserveButton.setDisable(true);

        ReservationRequestDTO request = new ReservationRequestDTO(memberIdentifierField.getText(), selectedBook.getId());

        appContext.getAsyncExecutor().run(
                () -> appContext.getReservationService().reserve(request),
                this::onReserveSuccess,
                throwable -> {
                    reserveButton.setDisable(false);
                    errorLabel.setText(throwable instanceof BusinessException
                            ? throwable.getMessage() : "Unable to create this reservation right now.");
                });
    }

    private void onReserveSuccess(ReservationDTO reservation) {
        reserveButton.setDisable(false);
        resultLabel.setText(reservation.memberName() + " is now #" + reservation.queuePosition()
                + " in the queue for \"" + reservation.bookTitle() + "\".");
        memberIdentifierField.clear();
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }
}
