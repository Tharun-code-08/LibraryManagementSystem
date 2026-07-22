package com.university.lms.ui.util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 * Standard "loading" and "no results" placeholders for {@link TableView}, shown via
 * {@link TableView#setPlaceholder(javafx.scene.Node)} while its item list is empty. Swapping the
 * placeholder around an async load (loading before the call, no-results/message after) needs no
 * FXML changes since the table already renders whatever placeholder is set whenever it has zero
 * rows.
 */
public final class TablePlaceholders {

    private TablePlaceholders() {
    }

    public static VBox loading() {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setMaxSize(40, 40);
        Label label = new Label("Loading...");
        label.getStyleClass().add("placeholder-label");
        VBox box = new VBox(10, indicator, label);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    public static VBox noResults(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("placeholder-label");
        VBox box = new VBox(label);
        box.setAlignment(Pos.CENTER);
        return box;
    }
}
