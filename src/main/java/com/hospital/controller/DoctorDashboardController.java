package com.hospital.controller;

import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.*;
import com.hospital.security.SessionManager;
import com.hospital.service.PatientService;
import com.hospital.service.PriorityQueueService;
import com.hospital.service.ReportService;
import com.hospital.service.TreatmentService;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for Doctor Dashboard featuring NEXT PATIENT from Priority Queue and treatment tracking.
 */
public class DoctorDashboardController extends BaseController {

    @FXML private Label lblWaitingPatients;
    @FXML private Label lblCriticalPatients;
    @FXML private Label lblHighPatients;
    @FXML private Label lblInTreatmentPatients;
    @FXML private Label lblTreatedPatients;

    // NEXT PATIENT Components
    @FXML private VBox boxNextPatientCard;
    @FXML private VBox boxEmptyQueueNotice;
    @FXML private Label lblQueuePositionInfo;
    @FXML private Label lblNextPatientToken;
    @FXML private Label lblNextPatientName;
    @FXML private Label lblNextPatientDemographics;
    @FXML private Label lblNextPatientPriorityBadge;
    @FXML private Label lblNextPatientWaitTime;
    @FXML private Label lblNextPatientSymptoms;
    @FXML private Label lblNextPatientArrival;

    // Active Treatments Table
    @FXML private TableView<Patient> tblActiveTreatment;
    @FXML private TableColumn<Patient, String> colTrToken;
    @FXML private TableColumn<Patient, String> colTrCode;
    @FXML private TableColumn<Patient, String> colTrName;
    @FXML private TableColumn<Patient, String> colTrEmergency;
    @FXML private TableColumn<Patient, String> colTrStartTime;
    @FXML private TableColumn<Patient, String> colTrSymptoms;
    @FXML private TableColumn<Patient, Void> colTrAction;

    private QueueItem currentNextPatient;

    @Override
    public void onAttached() {
        initTableColumns();
        loadDashboardData();
    }

    private void initTableColumns() {
        colTrToken.setCellValueFactory(new PropertyValueFactory<>("tokenNumber"));
        colTrCode.setCellValueFactory(new PropertyValueFactory<>("patientCode"));
        colTrName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colTrEmergency.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmergencyLevel().getDisplayName()));
        colTrStartTime.setCellValueFactory(cellData -> new SimpleStringProperty(formatTime(cellData.getValue().getUpdatedAt())));
        colTrSymptoms.setCellValueFactory(new PropertyValueFactory<>("symptoms"));

        colTrEmergency.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Patient p = getTableView().getItems().get(getIndex());
                    Label badge = new Label();
                    applyPriorityBadge(badge, p.getEmergencyLevel());
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        colTrAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnComplete = new Button("Complete");
            private final Button btnDischarge = new Button("Discharge");
            private final HBox pane = new HBox(6, btnComplete, btnDischarge);

            {
                btnComplete.getStyleClass().add("btn-success");
                btnComplete.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");
                btnDischarge.getStyleClass().add("btn-secondary");
                btnDischarge.setStyle("-fx-font-size: 11px; -fx-padding: 4px 8px;");
                pane.setAlignment(Pos.CENTER);

                btnComplete.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleCompleteTreatment(patient);
                });

                btnDischarge.setOnAction(event -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    handleDischargePatient(patient);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    public void loadDashboardData() {
        if (mainLayoutController == null) return;

        PriorityQueueService queueService = mainLayoutController.getQueueService();
        ReportService reportService = mainLayoutController.getReportService();
        PatientService patientService = mainLayoutController.getPatientService();

        // 1. Update Metrics
        Map<String, Integer> summary = reportService.getDashboardSummary();
        lblWaitingPatients.setText(String.valueOf(summary.getOrDefault("waiting", 0) + summary.getOrDefault("called", 0)));
        lblCriticalPatients.setText(String.valueOf(summary.getOrDefault("critical", 0)));
        lblHighPatients.setText(String.valueOf(summary.getOrDefault("high", 0)));
        lblInTreatmentPatients.setText(String.valueOf(summary.getOrDefault("in_treatment", 0)));
        lblTreatedPatients.setText(String.valueOf(summary.getOrDefault("treated", 0)));

        // 2. Load NEXT PATIENT from Priority Queue
        currentNextPatient = queueService.peekNext();
        if (currentNextPatient != null) {
            boxNextPatientCard.setVisible(true);
            boxEmptyQueueNotice.setVisible(false);

            lblQueuePositionInfo.setText("Position in Queue: #" + currentNextPatient.getQueuePosition());
            lblNextPatientToken.setText(currentNextPatient.getTokenNumber());
            lblNextPatientName.setText(currentNextPatient.getPatientName());
            lblNextPatientDemographics.setText("Age: " + currentNextPatient.getAge() + " | Gender: " +
                    currentNextPatient.getGender() + " | Code: " + currentNextPatient.getPatientCode());

            applyPriorityBadge(lblNextPatientPriorityBadge, currentNextPatient.getEmergencyLevel());

            long waitMinutes = currentNextPatient.getWaitingTimeMinutes();
            lblNextPatientWaitTime.setText("Waiting: " + waitMinutes + " min");
            if (waitMinutes > 15 && currentNextPatient.getEmergencyLevel() == EmergencyLevel.CRITICAL) {
                lblNextPatientWaitTime.setText("⚠️ OVERDUE CRITICAL: " + waitMinutes + " min");
            }

            lblNextPatientSymptoms.setText(currentNextPatient.getSymptoms());
            lblNextPatientArrival.setText("Arrival Time: " + formatDateTime(currentNextPatient.getArrivalTime()));
        } else {
            boxNextPatientCard.setVisible(false);
            boxEmptyQueueNotice.setVisible(true);
            lblQueuePositionInfo.setText("Queue: Empty");
        }

        // 3. Load In-Treatment Patients
        List<Patient> inTreatment = patientService.getAllPatients().stream()
                .filter(p -> p.getCurrentStatus() == PatientStatus.IN_TREATMENT)
                .collect(Collectors.toList());
        tblActiveTreatment.setItems(FXCollections.observableArrayList(inTreatment));
    }

    @FXML
    public void handleStartTreatmentForNext() {
        if (currentNextPatient == null) {
            AlertHelper.showWarning("Queue Empty", "There is no patient waiting in the queue.");
            return;
        }

        User doctor = SessionManager.getInstance().getCurrentUser();
        int docId = doctor != null ? doctor.getUserId() : 2;

        try {
            mainLayoutController.getTreatmentService().startTreatment(
                    currentNextPatient.getPatientId(),
                    docId,
                    "Treatment started by " + (doctor != null ? doctor.getFullName() : "Doctor")
            );

            NotificationManager.showInfo("Treatment Commenced", "Started treatment for " +
                    currentNextPatient.getPatientName() + " (" + currentNextPatient.getTokenNumber() + ").");

            handleRefresh();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Treatment Error", e.getMessage());
        }
    }

    @FXML
    public void handleViewNextPatientDetails() {
        if (currentNextPatient != null && mainLayoutController != null) {
            PatientDetailController.setSelectedPatientId(currentNextPatient.getPatientId());
            mainLayoutController.loadView("/fxml/PatientDetail.fxml",
                    "Patient Clinical Profile - " + currentNextPatient.getPatientName(), null);
        }
    }

    private void handleCompleteTreatment(Patient patient) {
        User doctor = SessionManager.getInstance().getCurrentUser();
        int docId = doctor != null ? doctor.getUserId() : 2;

        try {
            mainLayoutController.getTreatmentService().completeTreatment(
                    patient.getPatientId(),
                    docId,
                    "Clinical emergency treatment successfully concluded."
            );
            NotificationManager.showInfo("Treatment Complete", "Patient " + patient.getFullName() + " marked as TREATED.");
            handleRefresh();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Update Error", e.getMessage());
        }
    }

    private void handleDischargePatient(Patient patient) {
        User doctor = SessionManager.getInstance().getCurrentUser();
        int docId = doctor != null ? doctor.getUserId() : 2;

        boolean confirm = AlertHelper.showConfirmation("Confirm Discharge",
                "Discharge patient " + patient.getFullName() + " (" + patient.getTokenNumber() + ")?");
        if (!confirm) return;

        try {
            mainLayoutController.getTreatmentService().dischargePatient(
                    patient.getPatientId(),
                    docId,
                    "Vitals stabilized. Patient discharged home."
            );
            NotificationManager.showInfo("Patient Discharged", "Patient " + patient.getFullName() + " discharged.");
            handleRefresh();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Discharge Error", e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        if (mainLayoutController != null) {
            mainLayoutController.getQueueService().refreshFromDatabase();
        }
        loadDashboardData();
    }

    @FXML
    public void handleViewFullQueue() {
        if (mainLayoutController != null) {
            mainLayoutController.handleNavQueue();
        }
    }
}
