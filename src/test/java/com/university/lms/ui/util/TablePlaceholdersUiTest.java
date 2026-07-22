package com.university.lms.ui.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * A real, on-screen (Xvfb-backed in CI) rendering test — the first of its kind in this project —
 * proving TestFX/JavaFX can actually mount a scene and that {@link TablePlaceholders} renders the
 * nodes callers expect. Run headless CI via {@code xvfb-run mvn test}, since there is no
 * maintained Monocle build for JavaFX 21.
 */
class TablePlaceholdersUiTest extends ApplicationTest {

    private TableView<String> tableView;

    @Override
    public void start(Stage stage) {
        tableView = new TableView<>();
        tableView.getColumns().add(new TableColumn<String, String>("Column"));
        stage.setScene(new Scene(tableView, 300, 200));
        stage.show();
    }

    @Test
    void loadingPlaceholderShowsProgressIndicator() {
        interact(() -> tableView.setPlaceholder(TablePlaceholders.loading()));

        VBox placeholder = (VBox) tableView.getPlaceholder();
        assertTrue(placeholder.getChildren().get(0) instanceof ProgressIndicator);
        verifyThat(tableView, isVisible());
    }

    @Test
    void noResultsPlaceholderShowsMessage() {
        interact(() -> tableView.setPlaceholder(TablePlaceholders.noResults("No books found.")));

        VBox placeholder = (VBox) tableView.getPlaceholder();
        Label label = (Label) placeholder.getChildren().get(0);
        assertFalse(label.getText().isBlank());
        assertTrue(label.getText().contains("No books found."));
    }
}
