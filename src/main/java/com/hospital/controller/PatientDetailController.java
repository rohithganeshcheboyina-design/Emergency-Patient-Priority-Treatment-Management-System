package com.hospital.controller;

import com.hospital.dao.EmergencyAssessmentDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.PatientStatusHistoryDAO;
import com.hospital.dao.TreatmentDAO;
import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.*;
import com.hospital.security.SessionManager;
import com.hospital.service.PatientService;
import com.hospital.service.TreatmentService;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for the detailed Patient Profile screen with demographics, triage audit,
 * treatments, and status transition timeline.
 */
public class PatientDetailController extends BaseController {

    private static int selectedPatientId;

    public static void setSelectedPatientId(int id) {
        selectedPatientId = id;
    }

    // Header Controls
    @FXML private Label lblHeaderName;
    @FXML private Label lblHeaderToken;
    @FXML private Label lblHeaderPriority;
    @FXML private Label lblHeaderStatus;
    @FXML private Label lblHeaderSub;

    // Action Buttons
    @FXML private Button btnActionCall;
    @FXML private Button btnActionStartTreatment;
    @FXML private Button btnActionCompleteTreatment;
    @FXML private Button btnActionDischarge;

    // Demographics Tab
    @FXML private Label lblPatientCode;
    @FXML private Label lblTokenNumber;
    @FXML private Label lblArrivalTime;
    @FXML private Label lblWaitTime;
    @FXML private Label lblName;
    @FXML private Label lblAgeGender;
    @FXML private Label lblPhone;
    @FXML private Label lblBloodGroup;
    @FXML private Label lblAddress;

    // Triage Tab
    @FXML private Label lblSymptoms;
    @FXML private Label lblTriageLevel;
    @FXML private Label lblTriagePriority;
    @FXML private Label lblAssignedDoctor;

    // Treatment Tab
    @FXML private TableView<Treatment> tblTreatments;
    @FXML private TableColumn<Treatment, Integer> colTrId;
    @FXML private TableColumn<Treatment, String> colTrDoctor;
    @FXML private TableColumn<Treatment, String> colTrStart;
    @FXML private TableColumn<Treatment, String> colTrEnd;
    @FXML private TableColumn<Treatment, String> colTrStatus;
    @FXML private TableColumn<Treatment, String> colTrNotes;

    // History Tab
    @FXML private TableView<PatientStatusHistory> tblStatusHistory;
    @FXML private TableColumn<PatientStatusHistory, Integer> colHistId;
    @FXML private TableColumn<PatientStatusHistory, String> colHistOld;
    @FXML private TableColumn<PatientStatusHistory, String> colHistNew;
    @FXML private TableColumn<PatientStatusHistory, String> colHistBy;
    @FXML private TableColumn<PatientStatusHistory, String> colHistTime;
    @FXML private TableColumn<PatientStatusHistory, String> colHistRemarks;

    private final PatientDAO patientDAO = new PatientDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final PatientStatusHistoryDAO historyDAO = new PatientStatusHistoryDAO();

    private Patient patient;

    @Override
    public void onAttached() {
        initTables();
        loadPatientData();
    }

    private void initTables() {
        // Treatment Table
        colTrId.setCellValueFactory(new PropertyValueFactory<>("treatmentId"));
        colTrDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colTrStart.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getStartTime())));
        colTrEnd.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getEndTime())));
        colTrStatus.setCellValueFactory(new PropertyValueFactory<>("treatmentStatus"));
        colTrNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // History Table
        colHistId.setCellValueFactory(new PropertyValueFactory<>("historyId"));
        colHistOld.setCellValueFactory(cellData -> {
            PatientStatus old = cellData.getValue().getOldStatus();
            return new SimpleStringProperty(old != null ? old.getDisplayName() : "Initial Intake");
        });
        colHistNew.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNewStatus().getDisplayName()));
        colHistBy.setCellValueFactory(new PropertyValueFactory<>("changedByName"));
        colHistTime.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getChangedAt())));
        colHistRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));
    }

    public void loadPatientData() {
        if (selectedPatientId <= 0) return;
        patient = patientDAO.findById(selectedPatientId);
        if (patient == null) return;

        // Header
        lblHeaderName.setText(patient.getFullName());
        lblHeaderToken.setText(patient.getTokenNumber());
        applyPriorityBadge(lblHeaderPriority, patient.getEmergencyLevel());
        applyStatusBadge(lblHeaderStatus, patient.getCurrentStatus());
        lblHeaderSub.setText("Code: " + patient.getPatientCode() + " | Registered: " + formatDateTime(patient.getCreatedAt()));

        // Demographics
        lblPatientCode.setText(patient.getPatientCode());
        lblTokenNumber.setText(patient.getTokenNumber());
        lblArrivalTime.setText(formatDateTime(patient.getArrivalTime()));
        lblWaitTime.setText(patient.getWaitingTimeMinutes() + " minutes");
        lblName.setText(patient.getFullName());
        lblAgeGender.setText(patient.getAge() + " / " + patient.getGender());
        lblPhone.setText(patient.getPhone());
        lblBloodGroup.setText(patient.getBloodGroup() != null ? patient.getBloodGroup() : "Unknown");
        lblAddress.setText(patient.getAddress() != null && !patient.getAddress().isEmpty() ? patient.getAddress() : "Not Provided");

        // Triage
        lblSymptoms.setText(patient.getSymptoms());
        lblTriageLevel.setText(patient.getEmergencyLevel().name() + " (" + patient.getEmergencyLevel().getDisplayName() + ")");
        lblTriagePriority.setText("Priority " + patient.getPriority());
        lblAssignedDoctor.setText(patient.getAssignedDoctorName() != null ? patient.getAssignedDoctorName() : "Unassigned");

        // Treatments
        List<Treatment> treatments = treatmentDAO.getTreatmentsByPatientId(selectedPatientId);
        tblTreatments.setItems(FXCollections.observableArrayList(treatments));

        // History
        List<PatientStatusHistory> histories = historyDAO.getHistoryByPatientId(selectedPatientId);
        tblStatusHistory.setItems(FXCollections.observableArrayList(histories));

        updateActionButtonsState();
    }

    private void updateActionButtonsState() {
        if (patient == null) return;
        PatientStatus status = patient.getCurrentStatus();

        btnActionCall.setVisible(status == PatientStatus.WAITING);
        btnActionCall.setManaged(status == PatientStatus.WAITING);

        btnActionStartTreatment.setVisible(status == PatientStatus.WAITING || status == PatientStatus.CALLED);
        btnActionStartTreatment.setManaged(status == PatientStatus.WAITING || status == PatientStatus.CALLED);

        btnActionCompleteTreatment.setVisible(status == PatientStatus.IN_TREATMENT);
        btnActionCompleteTreatment.setManaged(status == PatientStatus.IN_TREATMENT);

        btnActionDischarge.setVisible(status == PatientStatus.TREATED || status == PatientStatus.IN_TREATMENT);
        btnActionDischarge.setManaged(status == PatientStatus.TREATED || status == PatientStatus.IN_TREATMENT);
    }

    @FXML
    public void handleCallPatient() {
        User staff = SessionManager.getInstance().getCurrentUser();
        int staffId = staff != null ? staff.getUserId() : 4;
        try {
            mainLayoutController.getPatientService().updateStatus(
                    selectedPatientId,
                    PatientStatus.CALLED,
                    null,
                    staffId,
                    "Patient called to Triage Bay."
            );
            NotificationManager.showInfo("Patient Called", patient.getFullName() + " called.");
            loadPatientData();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void handleStartTreatment() {
        User doctor = SessionManager.getInstance().getCurrentUser();
        int docId = doctor != null ? doctor.getUserId() : 2;
        try {
            mainLayoutController.getTreatmentService().startTreatment(
                    selectedPatientId,
                    docId,
                    "Emergency care started from patient profile."
            );
            NotificationManager.showInfo("Treatment Commenced", "Started treatment for " + patient.getFullName());
            loadPatientData();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void handleCompleteTreatment() {
        User doctor = SessionManager.getInstance().getCurrentUser();
        int docId = doctor != null ? doctor.getUserId() : 2;
        try {
            mainLayoutController.getTreatmentService().completeTreatment(
                    selectedPatientId,
                    docId,
                    "Treatment finalized and patient stabilized."
            );
            NotificationManager.showInfo("Treatment Complete", "Patient " + patient.getFullName() + " marked as TREATED.");
            loadPatientData();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void handleDischarge() {
        User doctor = SessionManager.getInstance().getCurrentUser();
        int docId = doctor != null ? doctor.getUserId() : 2;

        boolean confirm = AlertHelper.showConfirmation("Confirm Discharge", "Discharge patient " + patient.getFullName() + "?");
        if (!confirm) return;

        try {
            mainLayoutController.getTreatmentService().dischargePatient(
                    selectedPatientId,
                    docId,
                    "Patient discharged home with outpatient follow-up instructions."
            );
            NotificationManager.showInfo("Patient Discharged", patient.getFullName() + " discharged.");
            loadPatientData();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void handleBack() {
        if (mainLayoutController != null) {
            mainLayoutController.handleNavPatients();
        }
    }
}
