package com.university.lms.ui.controller.search;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.GlobalSearchResultDTO;

/** The Ctrl+K instant-results overlay: type to search books/authors/students/faculty, pick a
 *  result to jump straight to it. Pushed onto {@link AppContext#getOverlayHost()} on open and
 *  removes itself on close (Escape, backdrop click, or a result being chosen). */
public final class GlobalSearchOverlayController implements Initializable {

    private static final Duration DEBOUNCE = Duration.millis(250);

    private final AppContext appContext;
    private final PauseTransition debounce = new PauseTransition(DEBOUNCE);

    @FXML
    private StackPane overlayRoot;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<GlobalSearchResultDTO> resultsList;

    public GlobalSearchOverlayController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resultsList.setCellFactory(list -> new ResultCell());

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            debounce.stop();
            debounce.setOnFinished(event -> runSearch(newValue));
            debounce.playFromStart();
        });

        resultsList.setOnMouseClicked(event -> {
            GlobalSearchResultDTO selected = resultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectResult(selected);
            }
        });

        overlayRoot.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
            } else if (event.getCode() == KeyCode.ENTER) {
                GlobalSearchResultDTO selected = resultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectResult(selected);
                }
            }
        });

        overlayRoot.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), overlayRoot);
        fadeIn.setToValue(1);
        fadeIn.play();

        Platform.runLater(searchField::requestFocus);
    }

    @FXML
    private void onBackdropClicked(MouseEvent event) {
        if (event.getTarget() == overlayRoot) {
            close();
        }
    }

    @FXML
    private void onCardClicked(MouseEvent event) {
        event.consume();
    }

    private void runSearch(String keyword) {
        appContext.getAsyncExecutor().run(
                () -> appContext.getGlobalSearchService().search(keyword),
                results -> resultsList.setItems(FXCollections.observableArrayList(results)),
                throwable -> resultsList.setItems(FXCollections.observableArrayList()));
    }

    private void selectResult(GlobalSearchResultDTO result) {
        close();
        switch (result.entityType()) {
            case "BOOK" -> {
                appContext.setNavigationParameter(result.id());
                appContext.getViewNavigator().navigate("/fxml/catalog/BookForm.fxml");
            }
            case "STUDENT" -> {
                appContext.setNavigationParameter(result.id());
                appContext.getViewNavigator().navigate("/fxml/people/StudentForm.fxml");
            }
            case "FACULTY" -> {
                appContext.setNavigationParameter(result.id());
                appContext.getViewNavigator().navigate("/fxml/people/FacultyForm.fxml");
            }
            case "AUTHOR" -> appContext.getViewNavigator().navigate("/fxml/catalog/BookList.fxml");
            default -> { }
        }
    }

    private void close() {
        if (overlayRoot.getParent() instanceof javafx.scene.layout.Pane parent) {
            parent.getChildren().remove(overlayRoot);
        }
    }

    private final class ResultCell extends ListCell<GlobalSearchResultDTO> {
        @Override
        protected void updateItem(GlobalSearchResultDTO result, boolean empty) {
            super.updateItem(result, empty);
            if (empty || result == null) {
                setText(null);
                return;
            }
            setText("[" + result.entityType() + "] " + result.title() + " — " + result.subtitle());
        }
    }
}
