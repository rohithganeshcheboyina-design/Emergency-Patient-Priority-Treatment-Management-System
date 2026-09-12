package com.hospital.controller;

import com.hospital.model.ActivityLog;
import com.hospital.service.ActivityLogService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Admin Activity Logs auditing.
 */
public class ActivityLogsController extends BaseController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbActionFilter;
    @FXML private Label lblLogCountBadge;

    @FXML private TableView<ActivityLog> tblLogs;
    @FXML private TableColumn<ActivityLog, Integer> colId;
    @FXML private TableColumn<ActivityLog, String> colTimestamp;
    @FXML private TableColumn<ActivityLog, String> colAction;
    @FXML private TableColumn<ActivityLog, String> colUser;
    @FXML private TableColumn<ActivityLog, String> colEntity;
    @FXML private TableColumn<ActivityLog, String> colDescription;

    private List<ActivityLog> allLogs = new ArrayList<>();

    @Override
    public void onAttached() {
        initFilters();
        initTable();
        loadLogs();
    }

    private void initFilters() {
        cmbActionFilter.setItems(FXCollections.observableArrayList(
                "All Actions",
                "LOGIN",
                "LOGOUT",
                "PATIENT_REGISTERED",
                "PRIORITY_ASSIGNED",
                "STATUS_CHANGED",
                "TREATMENT_STARTED",
                "TREATMENT_COMPLETED",
                "PATIENT_DISCHARGED",
                "USER_CREATED",
                "USER_UPDATED"
        ));
        cmbActionFilter.getSelectionModel().selectFirst();
        cmbActionFilter.setOnAction(e -> applyFilter());

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    private void initTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("logId"));
        colTimestamp.setCellValueFactory(c -> new SimpleStringProperty(formatDateTime(c.getValue().getTimestamp())));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername() != null ? c.getValue().getUsername() : "System"));
        colEntity.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEntityType() + " #" + c.getValue().getEntityId()));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        colAction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(action);
                    badge.getStyleClass().add("badge-status");
                    if (action.contains("LOGIN")) {
                        badge.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 6px;");
                    } else if (action.contains("REGISTERED")) {
                        badge.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #059669; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 6px;");
                    } else if (action.contains("TREATMENT")) {
                        badge.setStyle("-fx-background-color: #F5F3FF; -fx-text-fill: #7C3AED; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 6px;");
                    } else if (action.contains("DISCHARGED")) {
                        badge.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 6px;");
                    }
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });
    }

    public void loadLogs() {
        if (mainLayoutController == null) return;
        ActivityLogService service = mainLayoutController.getActivityLogService();
        allLogs = service.getAllLogs();
        applyFilter();
    }

    private void applyFilter() {
        String actionFilter = cmbActionFilter.getSelectionModel().getSelectedItem();
        String query = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";

        List<ActivityLog> filtered = allLogs.stream().filter(l -> {
            if (actionFilter != null && !actionFilter.contains("All") && !actionFilter.equalsIgnoreCase(l.getAction())) {
                return false;
            }
            if (!query.isEmpty()) {
                boolean matchDesc = l.getDescription() != null && l.getDescription().toLowerCase().contains(query);
                boolean matchUser = l.getUsername() != null && l.getUsername().toLowerCase().contains(query);
                boolean matchAction = l.getAction() != null && l.getAction().toLowerCase().contains(query);
                return matchDesc || matchUser || matchAction;
            }
            return true;
        }).collect(Collectors.toList());

        tblLogs.setItems(FXCollections.observableArrayList(filtered));
        lblLogCountBadge.setText("Total: " + filtered.size() + " Logs");
    }

    @FXML
    public void handleReset() {
        txtSearch.clear();
        cmbActionFilter.getSelectionModel().selectFirst();
        applyFilter();
    }

    @FXML
    public void handleRefresh() {
        loadLogs();
    }
}
