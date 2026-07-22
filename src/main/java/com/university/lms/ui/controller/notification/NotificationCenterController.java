package com.university.lms.ui.controller.notification;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.NotificationDTO;

/** In-app notification center: recent notifications for the signed-in user, mark-as-read. */
public final class NotificationCenterController implements Initializable {

    private static final int LIST_LIMIT = 50;

    private final AppContext appContext;

    @FXML
    private ListView<NotificationDTO> notificationList;

    @FXML
    private Label errorLabel;

    public NotificationCenterController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        notificationList.setCellFactory(list -> new NotificationCell());
        loadNotifications();
    }

    private void loadNotifications() {
        errorLabel.setText("");
        Long userId = appContext.getAuthContext().getCurrentUser().getId();
        appContext.getAsyncExecutor().run(
                () -> appContext.getNotificationService().listForUser(userId, LIST_LIMIT),
                notifications -> notificationList.setItems(FXCollections.observableArrayList(notifications)),
                throwable -> errorLabel.setText("Unable to load notifications right now."));
    }

    @FXML
    private void onRefresh() {
        loadNotifications();
    }

    @FXML
    private void onBack() {
        appContext.getViewNavigator().navigate("/fxml/common/AuthenticatedShell.fxml");
    }

    private final class NotificationCell extends ListCell<NotificationDTO> {
        private final Label messageLabel = new Label();
        private final Label metaLabel = new Label();
        private final Button markReadButton = new Button("Mark Read");
        private final VBox box = new VBox(4, messageLabel, metaLabel, markReadButton);

        NotificationCell() {
            markReadButton.setOnAction(event -> {
                NotificationDTO notification = getItem();
                if (notification == null) {
                    return;
                }
                appContext.getAsyncExecutor().run(
                        () -> {
                            appContext.getNotificationService().markRead(notification.id());
                            return null;
                        },
                        ignored -> loadNotifications(),
                        throwable -> errorLabel.setText("Unable to mark this notification as read."));
            });
        }

        @Override
        protected void updateItem(NotificationDTO notification, boolean empty) {
            super.updateItem(notification, empty);
            if (empty || notification == null) {
                setGraphic(null);
                return;
            }
            messageLabel.setText(notification.message());
            messageLabel.setStyle(notification.read() ? "" : "-fx-font-weight: bold;");
            metaLabel.setText(notification.category() + " · " + notification.channel() + " · " + notification.createdAt());
            markReadButton.setDisable(notification.read());
            setGraphic(box);
        }
    }
}
