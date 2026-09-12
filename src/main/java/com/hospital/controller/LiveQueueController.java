package com.hospital.controller;

import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.*;
import com.hospital.security.SessionManager;
import com.hospital.service.PriorityQueueService;
import com.hospital.service.TreatmentService;
import com.hospital.util.AlertHelper;
import com.hospital.util.ConfigReader;
import com.hospital.util.NotificationManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.List;

/**
 * Controller for the Live Emergency Queue screen with real-time priority sorting,
 * threshold alerts, and dynamic filtering.
 */
public class LiveQueueController extends BaseController {

    @FXML private Label lblTotalInQueue;
    @FXML private Label lblCriticalCount;
    @FXML private Label lblHighCount;
    @FXML private Label lblAvgWait;
    @FXML private Label lblMaxWait;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterLevel;
    @FXML private ComboBox<String> cmbFilterStatus;
    @FXML private Label lblAutoRefreshStatus;

    @FXML private TableView<QueueItem> tblLiveQueue;
    @FXML private TableColumn<QueueItem, String> colPos;
    @FXML private TableColumn<QueueItem, String> colToken;
    @FXML private TableColumn<QueueItem, String> colCode;
    @FXML private TableColumn<QueueItem, String> colName;
    @FXML private TableColumn<QueueItem, String> colAgeGender;
    @FXML private TableColumn<QueueItem, String> colSymptoms;
    @FXML private TableColumn<QueueItem, String> colEmergency;
    @FXML private TableColumn<QueueItem, String> colArrival;
    @FXML private TableColumn<QueueItem, String> colWaitTime;
    @FXML private TableColumn<QueueItem, String> colDoctor;
    @FXML private TableColumn<QueueItem, String> colStatus;
    @FXML private TableColumn<QueueItem, Void> colActions;

    private Timeline autoRefreshTimeline;

    @Override
    public void onAttached() {
        initFilters();
        initTableColumns();
        loadQueueData();
        setupAutoRefresh();
    }

    private void initFilters() {
        cmbFilterLevel.setItems(FXCollections.observableArrayList(
                "All Severities",
                EmergencyLevel.CRITICAL.getDisplayName(),
                EmergencyLevel.HIGH.getDisplayName(),
                EmergencyLevel.MEDIUM.getDisplayName(),
                EmergencyLevel.LOW.getDisplayName()
        ));
        cmbFilterLevel.getSelectionModel().selectFirst();
        cmbFilterLevel.setOnAction(e -> applyFilters());

        cmbFilterStatus.setItems(FXCollections.observableArrayList(
                "All Active",
                PatientStatus.WAITING.getDisplayName(),
                PatientStatus.CALLED.getDisplayName()
        ));
        cmbFilterStatus.getSelectionModel().selectFirst();
        cmbFilterStatus.setOnAction(e -> applyFilters());

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void initTableColumns() {
        colPos.setCellValueFactory(cellData -> new SimpleStringProperty("#" + cellData.getValue().getQueuePosition()));
        colToken.setCellValueFactory(new PropertyValueFactory<>("tokenNumber"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("patientCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colAgeGender.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAge() + " / " + cellData.getValue().getGender()));
        colSymptoms.setCellValueFactory(new PropertyValueFactory<>("symptoms"));
        colEmergency.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmergencyLevel().getDisplayName()));
        colArrival.setCellValueFactory(cellData -> new SimpleStringProperty(formatTime(cellData.getValue().getArrivalTime())));
        colDoctor.setCellValueFactory(cellData -> {
            String doc = cellData.getValue().getAssignedDoctorName();
            return new SimpleStringProperty(doc != null ? doc : "Unassigned");
        });
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        // Emergency Badge Cell
        colEmergency.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    QueueItem qi = getTableView().getItems().get(getIndex());
                    Label badge = new Label();
                    applyPriorityBadge(badge, qi.getEmergencyLevel());
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        // Waiting Time Cell with Overdue Alert Highlighting
        colWaitTime.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    QueueItem qi = getTableView().getItems().get(getIndex());
                    long waitMins = qi.getWaitingTimeMinutes();
                    Label lbl = new Label(waitMins + " min");

                    // Threshold alerts
                    int criticalThresh = ConfigReader.getCriticalWaitThreshold();
                    int highThresh = ConfigReader.getHighWaitThreshold();

                    if (qi.getEmergencyLevel() == EmergencyLevel.CRITICAL && waitMins >= criticalThresh) {
                        lbl.setText("⚠️ " + waitMins + "m (ALERT)");
                        lbl.getStyleClass().add("badge-wait-alert");
                    } else if (qi.getEmergencyLevel() == EmergencyLevel.HIGH && waitMins >= highThresh) {
                        lbl.setText("⚠️ " + waitMins + "m (ALERT)");
                        lbl.getStyleClass().add("badge-wait-alert");
                    } else {
                        lbl.setStyle("-fx-text-fill: #334155;");
                    }

                    setGraphic(lbl);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        // Row factory for highlighting critical patients
        tblLiveQueue.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(QueueItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("table-row-critical");
                if (item != null && !empty && item.getEmergencyLevel() == EmergencyLevel.CRITICAL) {
                    getStyleClass().add("table-row-critical");
                }
            }
        });

        // Actions column with Call to Bay & Start Treatment
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnCall = new Button("Call");
            private final Button btnTreat = new Button("Treat");
            private final HBox pane = new HBox(6, btnCall, btnTreat);

            {
                btnCall.getStyleClass().add("btn-secondary");
                btnCall.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                btnTreat.getStyleClass().add("btn-primary");
                btnTreat.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                pane.setAlignment(Pos.CENTER);

                btnCall.setOnAction(e -> {
                    QueueItem qi = getTableView().getItems().get(getIndex());
                    handleCallPatient(qi);
                });

                btnTreat.setOnAction(e -> {
                    QueueItem qi = getTableView().getItems().get(getIndex());
                    handleStartTreatment(qi);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    QueueItem qi = getTableView().getItems().get(getIndex());
                    btnCall.setDisable(qi.getStatus() == PatientStatus.CALLED);
                    setGraphic(pane);
                }
            }
        });
    }

    private void setupAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
        int interval = ConfigReader.getAutoRefreshSeconds();
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(interval), e -> {
            loadQueueData();
        }));
        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);
        autoRefreshTimeline.play();
        lblAutoRefreshStatus.setText("⏱️ Auto-refresh: " + interval + "s");
    }

    public void loadQueueData() {
        if (mainLayoutController == null) return;

        PriorityQueueService queueService = mainLayoutController.getQueueService();
        queueService.refreshFromDatabase();

        lblTotalInQueue.setText(String.valueOf(queueService.getQueueSize()));
        lblCriticalCount.setText(String.valueOf(queueService.getCriticalCount()));
        lblHighCount.setText(String.valueOf(queueService.getHighCount()));
        lblAvgWait.setText(Math.round(queueService.getAverageWaitingTimeMinutes() * 10.0) / 10.0 + " min");
        lblMaxWait.setText(queueService.getMaxWaitingTimeMinutes() + " min");

        applyFilters();
    }

    private void applyFilters() {
        if (mainLayoutController == null) return;
        PriorityQueueService queueService = mainLayoutController.getQueueService();

        String levelText = cmbFilterLevel.getSelectionModel().getSelectedItem();
        EmergencyLevel level = null;
        if (levelText != null && !levelText.contains("All")) {
            level = EmergencyLevel.fromString(levelText);
        }

        String statusText = cmbFilterStatus.getSelectionModel().getSelectedItem();
        PatientStatus status = null;
        if (statusText != null && !statusText.contains("All")) {
            status = PatientStatus.fromString(statusText);
        }

        String search = txtSearch.getText();

        List<QueueItem> filtered = queueService.filterQueue(level, status, search);
        tblLiveQueue.setItems(FXCollections.observableArrayList(filtered));
    }

    private void handleCallPatient(QueueItem item) {
        User staff = SessionManager.getInstance().getCurrentUser();
        int staffId = staff != null ? staff.getUserId() : 4;

        try {
            mainLayoutController.getPatientService().updateStatus(
                    item.getPatientId(),
                    PatientStatus.CALLED,
                    null,
                    staffId,
                    "Patient called to Triage Bay / Examination Counter."
            );
            NotificationManager.showInfo("Patient Called", item.getPatientName() + " (" + item.getTokenNumber() + ") called.");
            loadQueueData();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Update Error", e.getMessage());
        }
    }

    private void handleStartTreatment(QueueItem item) {
        User doctor = SessionManager.getInstance().getCurrentUser();
        int docId = doctor != null ? doctor.getUserId() : 2;

        try {
            mainLayoutController.getTreatmentService().startTreatment(
                    item.getPatientId(),
                    docId,
                    "Emergency treatment started from Live Queue."
            );
            NotificationManager.showInfo("Treatment Started", "Patient " + item.getPatientName() + " moved to IN_TREATMENT.");
            loadQueueData();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Treatment Error", e.getMessage());
        }
    }

    @FXML
    public void handleResetFilters() {
        txtSearch.clear();
        cmbFilterLevel.getSelectionModel().selectFirst();
        cmbFilterStatus.getSelectionModel().selectFirst();
        loadQueueData();
    }

    @FXML
    public void handleRefresh() {
        loadQueueData();
    }

    @FXML
    public void handleRegisterPatient() {
        if (mainLayoutController != null) {
            mainLayoutController.handleNavRegister();
        }
    }
}
