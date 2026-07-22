package com.university.lms.ui.controller.common;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.util.Duration;

import com.university.lms.config.AppContext;
import com.university.lms.dto.response.CategoryDistributionDTO;
import com.university.lms.dto.response.DashboardStatsDTO;
import com.university.lms.dto.response.MonthlyActivityDTO;
import com.university.lms.dto.response.PopularBookDTO;
import com.university.lms.dto.response.RecentActivityDTO;
import com.university.lms.dto.response.UserDTO;

/**
 * The authenticated home screen: live dashboard stat cards and analytics charts, plus the quick
 * action buttons that route to every other module (a full sidebar/navigation shell arrives in
 * Phase 11's UI polish pass — see docs/13-ImplementationRoadmap.md).
 */
public final class AuthenticatedShellController implements Initializable {

    private static final int MONTHS_OF_HISTORY = 6;
    private static final int POPULAR_BOOKS_LIMIT = 5;
    private static final int RECENT_ACTIVITY_LIMIT = 15;

    private final AppContext appContext;
    private PauseTransition idleTimer;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button notificationsButton;

    @FXML
    private ToggleButton darkModeToggle;

    @FXML
    private Label totalBooksLabel;

    @FXML
    private Label issuedBooksLabel;

    @FXML
    private Label availableBooksLabel;

    @FXML
    private Label overdueBooksLabel;

    @FXML
    private Label reservationsLabel;

    @FXML
    private Label studentsLabel;

    @FXML
    private Label facultyLabel;

    @FXML
    private LineChart<String, Number> monthlyActivityChart;

    @FXML
    private PieChart categoryDistributionChart;

    @FXML
    private BarChart<String, Number> popularBooksChart;

    @FXML
    private ListView<String> recentActivityList;

    public AuthenticatedShellController(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UserDTO currentUser = appContext.getAuthContext().getCurrentUser();
        welcomeLabel.setText("Welcome, " + currentUser.getUsername()
                + " (" + String.join(", ", currentUser.getRoles()) + ")");

        long timeoutMinutes = Long.parseLong(
                appContext.getConfigurationManager().app("app.session.idle-timeout-minutes", "15"));
        idleTimer = new PauseTransition(Duration.minutes(timeoutMinutes));
        idleTimer.setOnFinished(event -> onSessionTimeout());

        welcomeLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                attachActivityListeners(newScene);
                idleTimer.playFromStart();
            }
        });

        loadDashboard();
        loadUnreadNotificationCount();
    }

    private void loadUnreadNotificationCount() {
        Long userId = appContext.getAuthContext().getCurrentUser().getId();
        appContext.getAsyncExecutor().run(
                () -> appContext.getNotificationService().countUnread(userId),
                count -> notificationsButton.setText(count > 0 ? "Notifications (" + count + ")" : "Notifications"),
                throwable -> { /* badge simply stays unlabeled if it can't load */ });
    }

    private void loadDashboard() {
        appContext.getAsyncExecutor().run(
                () -> appContext.getDashboardService().getStats(),
                this::onStatsLoaded,
                throwable -> { /* stat cards simply stay blank if analytics can't load */ });

        appContext.getAsyncExecutor().run(
                () -> appContext.getDashboardService().getMonthlyActivity(MONTHS_OF_HISTORY),
                this::onMonthlyActivityLoaded,
                throwable -> { });

        appContext.getAsyncExecutor().run(
                () -> appContext.getDashboardService().getCategoryDistribution(),
                this::onCategoryDistributionLoaded,
                throwable -> { });

        appContext.getAsyncExecutor().run(
                () -> appContext.getDashboardService().getPopularBooks(POPULAR_BOOKS_LIMIT),
                this::onPopularBooksLoaded,
                throwable -> { });

        appContext.getAsyncExecutor().run(
                () -> appContext.getDashboardService().getRecentActivity(RECENT_ACTIVITY_LIMIT),
                this::onRecentActivityLoaded,
                throwable -> { });
    }

    private void onStatsLoaded(DashboardStatsDTO stats) {
        totalBooksLabel.setText(String.valueOf(stats.totalBooks()));
        issuedBooksLabel.setText(String.valueOf(stats.issuedBooks()));
        availableBooksLabel.setText(String.valueOf(stats.availableBooks()));
        overdueBooksLabel.setText(String.valueOf(stats.overdueBooks()));
        reservationsLabel.setText(String.valueOf(stats.activeReservations()));
        studentsLabel.setText(String.valueOf(stats.totalStudents()));
        facultyLabel.setText(String.valueOf(stats.totalFaculty()));
    }

    private void onMonthlyActivityLoaded(List<MonthlyActivityDTO> months) {
        XYChart.Series<String, Number> issuedSeries = new XYChart.Series<>();
        issuedSeries.setName("Issued");
        XYChart.Series<String, Number> returnedSeries = new XYChart.Series<>();
        returnedSeries.setName("Returned");

        for (MonthlyActivityDTO month : months) {
            issuedSeries.getData().add(new XYChart.Data<>(month.yearMonth(), month.issuedCount()));
            returnedSeries.getData().add(new XYChart.Data<>(month.yearMonth(), month.returnedCount()));
        }

        monthlyActivityChart.getData().setAll(issuedSeries, returnedSeries);
    }

    private void onCategoryDistributionLoaded(List<CategoryDistributionDTO> categories) {
        categoryDistributionChart.setData(FXCollections.observableArrayList(
                categories.stream()
                        .map(category -> new PieChart.Data(category.categoryName(), category.bookCount()))
                        .toList()));
    }

    private void onPopularBooksLoaded(List<PopularBookDTO> books) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Times Issued");
        for (PopularBookDTO book : books) {
            series.getData().add(new XYChart.Data<>(book.bookTitle(), book.issueCount()));
        }
        popularBooksChart.getData().setAll(series);
    }

    private void onRecentActivityLoaded(List<RecentActivityDTO> activities) {
        recentActivityList.setItems(FXCollections.observableArrayList(
                activities.stream()
                        .map(activity -> activity.actorUsername() + " — " + activity.action()
                                + " (" + activity.entityType() + (activity.entityId() != null ? " #" + activity.entityId() : "") + ")"
                                + " — " + activity.createdAt())
                        .toList()));
    }

    private void attachActivityListeners(Scene scene) {
        scene.addEventFilter(javafx.scene.input.MouseEvent.ANY, e -> idleTimer.playFromStart());
        scene.addEventFilter(javafx.scene.input.KeyEvent.ANY, e -> idleTimer.playFromStart());
    }

    private void onSessionTimeout() {
        appContext.getAuthService().logout(appContext.getAuthContext().getSessionToken());
        appContext.getRememberMeStore().clear();
        appContext.getViewNavigator().navigate("/fxml/auth/Login.fxml");
    }

    @FXML
    private void onLogout() {
        if (idleTimer != null) {
            idleTimer.stop();
        }
        appContext.getAsyncExecutor().run(
                () -> {
                    appContext.getAuthService().logout(appContext.getAuthContext().getSessionToken());
                    return null;
                },
                ignored -> {
                    appContext.getRememberMeStore().clear();
                    appContext.getViewNavigator().navigate("/fxml/auth/Login.fxml");
                },
                throwable -> appContext.getViewNavigator().navigate("/fxml/auth/Login.fxml"));
    }

    @FXML
    private void onChangePassword() {
        appContext.getViewNavigator().navigate("/fxml/auth/ChangePassword.fxml");
    }

    @FXML
    private void onOpenNotifications() {
        appContext.getViewNavigator().navigate("/fxml/notification/NotificationCenter.fxml");
    }

    @FXML
    private void onOpenBookCatalog() {
        appContext.getViewNavigator().navigate("/fxml/catalog/BookList.fxml");
    }

    @FXML
    private void onOpenStudents() {
        appContext.getViewNavigator().navigate("/fxml/people/StudentList.fxml");
    }

    @FXML
    private void onOpenFaculty() {
        appContext.getViewNavigator().navigate("/fxml/people/FacultyList.fxml");
    }

    @FXML
    private void onOpenMembershipTypes() {
        appContext.getViewNavigator().navigate("/fxml/people/MembershipTypeManagement.fxml");
    }

    @FXML
    private void onOpenIssue() {
        appContext.getViewNavigator().navigate("/fxml/circulation/Issue.fxml");
    }

    @FXML
    private void onOpenReturn() {
        appContext.getViewNavigator().navigate("/fxml/circulation/Return.fxml");
    }

    @FXML
    private void onOpenReservation() {
        appContext.getViewNavigator().navigate("/fxml/circulation/Reservation.fxml");
    }

    @FXML
    private void onOpenFines() {
        appContext.getViewNavigator().navigate("/fxml/finance/FineManagement.fxml");
    }

    @FXML
    private void onOpenSuppliers() {
        appContext.getViewNavigator().navigate("/fxml/inventory/SupplierManagement.fxml");
    }

    @FXML
    private void onOpenPurchaseOrders() {
        appContext.getViewNavigator().navigate("/fxml/inventory/PurchaseOrderList.fxml");
    }

    @FXML
    private void onOpenInventoryAudit() {
        appContext.getViewNavigator().navigate("/fxml/inventory/InventoryAudit.fxml");
    }

    @FXML
    private void onOpenReports() {
        appContext.getViewNavigator().navigate("/fxml/report/Report.fxml");
    }

    @FXML
    private void onToggleDarkMode() {
        Scene scene = darkModeToggle.getScene();
        scene.getStylesheets().removeIf(sheet -> sheet.endsWith("theme-light.css") || sheet.endsWith("theme-dark.css"));
        String theme = darkModeToggle.isSelected() ? "/css/theme-dark.css" : "/css/theme-light.css";
        scene.getStylesheets().add(getClass().getResource(theme).toExternalForm());
    }
}
