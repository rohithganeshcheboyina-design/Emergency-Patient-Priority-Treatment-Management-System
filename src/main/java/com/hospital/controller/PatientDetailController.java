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
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

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
    @FXML private Button btnActionUpdateVitals;
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
    @FXML private Label lblAllergies;
    @FXML private Label lblEmergencyContact;

    // Triage Tab
    @FXML private Label lblSymptoms;
    @FXML private Label lblTriageLevel;
    @FXML private Label lblTriagePriority;
    @FXML private Label lblAssignedDoctor;
    @FXML private Label lblVitalBp;
    @FXML private Label lblVitalHeartRate;
    @FXML private Label lblVitalTemp;
    @FXML private Label lblVitalSpo2;
    @FXML private Label lblVitalRespRate;

    // Treatment Tab
    @FXML private TableView<Treatment> tblTreatments;
    @FXML private TableColumn<Treatment, Integer> colTrId;
    @FXML private TableColumn<Treatment, String> colTrDoctor;
    @FXML private TableColumn<Treatment, String> colTrStart;
    @FXML private TableColumn<Treatment, String> colTrEnd;
    @FXML private TableColumn<Treatment, String> colTrStatus;
    @FXML private TableColumn<Treatment, String> colTrDiagnosis;
    @FXML private TableColumn<Treatment, String> colTrMedications;
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
        colTrDiagnosis.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiagnosis() != null ? cellData.getValue().getDiagnosis() : "--"));
        colTrMedications.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMedications() != null ? cellData.getValue().getMedications() : "--"));
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
        lblAllergies.setText(patient.getAllergies() != null && !patient.getAllergies().isEmpty() ? patient.getAllergies() : "None Reported");
        lblEmergencyContact.setText(patient.getEmergencyContact() != null && !patient.getEmergencyContact().isEmpty() ? patient.getEmergencyContact() : "Not Provided");

        // Triage
        lblSymptoms.setText(patient.getSymptoms());
        lblTriageLevel.setText(patient.getEmergencyLevel().name() + " (" + patient.getEmergencyLevel().getDisplayName() + ")");
        lblTriagePriority.setText("Priority " + patient.getPriority());
        lblAssignedDoctor.setText(patient.getAssignedDoctorName() != null ? patient.getAssignedDoctorName() : "Unassigned");

        // Vitals
        lblVitalBp.setText(patient.getVitalBp() != null && !patient.getVitalBp().isEmpty() ? patient.getVitalBp() : "--/--");
        lblVitalHeartRate.setText(patient.getVitalHeartRate() != null ? patient.getVitalHeartRate() + " bpm" : "-- bpm");
        lblVitalTemp.setText(patient.getVitalTemperature() != null ? String.format("%.1f °F", patient.getVitalTemperature()) : "-- °F");
        lblVitalSpo2.setText(patient.getVitalSpo2() != null ? patient.getVitalSpo2() + "%" : "--%");
        lblVitalRespRate.setText(patient.getVitalRespiratoryRate() != null ? patient.getVitalRespiratoryRate() + " /min" : "-- /min");

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
        User currentUser = SessionManager.getInstance().getCurrentUser();
        Role role = currentUser != null ? currentUser.getRole() : Role.DOCTOR;

        // Admin is strictly read-only: hide clinical action buttons
        if (role == Role.ADMIN) {
            btnActionUpdateVitals.setVisible(false);
            btnActionUpdateVitals.setManaged(false);
            btnActionCall.setVisible(false);
            btnActionCall.setManaged(false);
            btnActionStartTreatment.setVisible(false);
            btnActionStartTreatment.setManaged(false);
            btnActionCompleteTreatment.setVisible(false);
            btnActionCompleteTreatment.setManaged(false);
            btnActionDischarge.setVisible(false);
            btnActionDischarge.setManaged(false);
            return;
        }

        // Vitals updating allowed for Nurse and Doctor on active patients
        boolean canUpdateVitals = (role == Role.NURSE || role == Role.DOCTOR) && status != PatientStatus.DISCHARGED;
        btnActionUpdateVitals.setVisible(canUpdateVitals);
        btnActionUpdateVitals.setManaged(canUpdateVitals);

        // Calling patient to bay: Nurse or Doctor when patient is waiting
        boolean canCall = (role == Role.NURSE || role == Role.DOCTOR) && status == PatientStatus.WAITING;
        btnActionCall.setVisible(canCall);
        btnActionCall.setManaged(canCall);

        // Clinical treatments & discharge: strictly Doctor only
        boolean isDoctor = (role == Role.DOCTOR);
        btnActionStartTreatment.setVisible(isDoctor && (status == PatientStatus.WAITING || status == PatientStatus.CALLED));
        btnActionStartTreatment.setManaged(isDoctor && (status == PatientStatus.WAITING || status == PatientStatus.CALLED));

        btnActionCompleteTreatment.setVisible(isDoctor && status == PatientStatus.IN_TREATMENT);
        btnActionCompleteTreatment.setManaged(isDoctor && status == PatientStatus.IN_TREATMENT);

        btnActionDischarge.setVisible(isDoctor && (status == PatientStatus.TREATED || status == PatientStatus.IN_TREATMENT));
        btnActionDischarge.setManaged(isDoctor && (status == PatientStatus.TREATED || status == PatientStatus.IN_TREATMENT));
    }

    @FXML
    public void handleUpdateVitals() {
        if (patient == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Clinical Vitals & Triage");
        dialog.setHeaderText("Update vital signs and triage notes for " + patient.getFullName() + " (" + patient.getTokenNumber() + ")");

        ButtonType saveButtonType = new ButtonType("Save Vitals", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField txtBp = new TextField(patient.getVitalBp() != null ? patient.getVitalBp() : "");
        txtBp.setPromptText("e.g. 120/80");

        TextField txtHr = new TextField(patient.getVitalHeartRate() != null ? String.valueOf(patient.getVitalHeartRate()) : "");
        txtHr.setPromptText("bpm (e.g. 75)");

        TextField txtTemp = new TextField(patient.getVitalTemperature() != null ? String.valueOf(patient.getVitalTemperature()) : "");
        txtTemp.setPromptText("°F (e.g. 98.6)");

        TextField txtSpo2 = new TextField(patient.getVitalSpo2() != null ? String.valueOf(patient.getVitalSpo2()) : "");
        txtSpo2.setPromptText("% (e.g. 98)");

        TextField txtRr = new TextField(patient.getVitalRespiratoryRate() != null ? String.valueOf(patient.getVitalRespiratoryRate()) : "");
        txtRr.setPromptText("breaths/min (e.g. 16)");

        ComboBox<EmergencyLevel> cmbLevel = new ComboBox<>(FXCollections.observableArrayList(EmergencyLevel.values()));
        cmbLevel.setValue(patient.getEmergencyLevel());

        TextArea txtSymptoms = new TextArea(patient.getSymptoms() != null ? patient.getSymptoms() : "");
        txtSymptoms.setPrefRowCount(3);

        grid.add(new Label("Blood Pressure:"), 0, 0);
        grid.add(txtBp, 1, 0);
        grid.add(new Label("Heart Rate (bpm):"), 0, 1);
        grid.add(txtHr, 1, 1);
        grid.add(new Label("Temperature (°F):"), 0, 2);
        grid.add(txtTemp, 1, 2);
        grid.add(new Label("SpO2 (%):"), 0, 3);
        grid.add(txtSpo2, 1, 3);
        grid.add(new Label("Respiratory Rate:"), 0, 4);
        grid.add(txtRr, 1, 4);
        grid.add(new Label("Triage Level:"), 0, 5);
        grid.add(cmbLevel, 1, 5);
        grid.add(new Label("Symptoms/Observations:"), 0, 6);
        grid.add(txtSymptoms, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == saveButtonType) {
            try {
                String bp = txtBp.getText().trim();
                Integer hr = txtHr.getText().trim().isEmpty() ? null : Integer.parseInt(txtHr.getText().trim());
                Double temp = txtTemp.getText().trim().isEmpty() ? null : Double.parseDouble(txtTemp.getText().trim());
                Integer spo2 = txtSpo2.getText().trim().isEmpty() ? null : Integer.parseInt(txtSpo2.getText().trim());
                Integer rr = txtRr.getText().trim().isEmpty() ? null : Integer.parseInt(txtRr.getText().trim());
                EmergencyLevel level = cmbLevel.getValue();
                String symptoms = txtSymptoms.getText().trim();

                User currentUser = SessionManager.getInstance().getCurrentUser();
                int staffId = currentUser != null ? currentUser.getUserId() : 4;

                mainLayoutController.getPatientService().updateVitals(
                        selectedPatientId, bp, hr, temp, spo2, rr, symptoms, level, staffId,
                        "Clinical vitals observation recorded by staff."
                );

                NotificationManager.showInfo("Vitals Updated", "Vital signs and observations saved successfully for " + patient.getFullName());
                loadPatientData();
            } catch (NumberFormatException nfe) {
                AlertHelper.showError("Invalid Number Format", "Please enter valid numbers for heart rate, temperature, SpO2, and respiratory rate.");
            } catch (InvalidPatientDataException ex) {
                AlertHelper.showError("Validation Error", ex.getMessage());
            }
        }
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
