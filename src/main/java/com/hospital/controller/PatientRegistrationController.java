package com.hospital.controller;

import com.hospital.dao.PatientDAO;
import com.hospital.dao.UserDAO;
import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.*;
import com.hospital.security.SessionManager;
import com.hospital.service.PatientService;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for emergency intake, registration, and triage assessment.
 */
public class PatientRegistrationController extends BaseController {

    @FXML private TextField txtPatientCode;
    @FXML private TextField txtTokenNumber;
    @FXML private TextField txtArrivalTime;

    @FXML private TextField txtFullName;
    @FXML private TextField txtAge;
    @FXML private ComboBox<String> cmbGender;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cmbBloodGroup;
    @FXML private ComboBox<Doctor> cmbDoctor;
    @FXML private TextField txtAddress;

    @FXML private TextArea txtSymptoms;
    @FXML private ComboBox<EmergencyLevel> cmbEmergencyLevel;
    @FXML private VBox boxPriorityIndicator;
    @FXML private Label lblPriorityValue;
    @FXML private Label lblPriorityDesc;

    private final PatientDAO patientDAO = new PatientDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void onAttached() {
        initDropdowns();
        resetForm();
    }

    private void initDropdowns() {
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

        cmbBloodGroup.setItems(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"
        ));

        cmbEmergencyLevel.setItems(FXCollections.observableArrayList(EmergencyLevel.values()));
        cmbEmergencyLevel.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EmergencyLevel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name() + " (Priority " + item.getPriority() + ") - " + item.getDisplayName());
                }
            }
        });
        cmbEmergencyLevel.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(EmergencyLevel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.name() + " (Priority " + item.getPriority() + ")");
                }
            }
        });

        // Populate doctors
        List<Doctor> doctors = userDAO.getAvailableDoctors();
        cmbDoctor.setItems(FXCollections.observableArrayList(doctors));
        cmbDoctor.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName() + " (" + item.getSpecialization() + ")");
            }
        });
        cmbDoctor.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });
    }

    private void resetForm() {
        txtPatientCode.setText(patientDAO.getNextPatientCode());
        txtTokenNumber.setText(patientDAO.getNextTokenNumber());
        txtArrivalTime.setText(formatDateTime(LocalDateTime.now()));

        txtFullName.clear();
        txtAge.clear();
        cmbGender.getSelectionModel().clearSelection();
        txtPhone.clear();
        cmbBloodGroup.getSelectionModel().clearSelection();
        if (!cmbDoctor.getItems().isEmpty()) {
            cmbDoctor.getSelectionModel().clearSelection();
        }
        txtAddress.clear();
        txtSymptoms.clear();

        // Default to MEDIUM
        cmbEmergencyLevel.getSelectionModel().select(EmergencyLevel.MEDIUM);
        handleLevelSelectionChange();
    }

    @FXML
    public void handleLevelSelectionChange() {
        EmergencyLevel level = cmbEmergencyLevel.getSelectionModel().getSelectedItem();
        if (level == null) return;

        lblPriorityValue.setText("Priority " + level.getPriority() + " : " + level.getDisplayName().toUpperCase());
        lblPriorityDesc.setText(level.getDescription());

        // Update style color
        boxPriorityIndicator.getStyleClass().removeAll("card-critical", "card-high", "card-medium", "card-low");
        switch (level) {
            case CRITICAL:
                boxPriorityIndicator.getStyleClass().add("card-critical");
                lblPriorityValue.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 16px;");
                break;
            case HIGH:
                boxPriorityIndicator.getStyleClass().add("card-high");
                lblPriorityValue.setStyle("-fx-text-fill: #EA580C; -fx-font-weight: bold; -fx-font-size: 16px;");
                break;
            case MEDIUM:
                boxPriorityIndicator.getStyleClass().add("card-medium");
                lblPriorityValue.setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold; -fx-font-size: 16px;");
                break;
            case LOW:
                boxPriorityIndicator.getStyleClass().add("card-low");
                lblPriorityValue.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold; -fx-font-size: 16px;");
                break;
        }
    }

    @FXML
    public void handleRegisterPatient() {
        String name = txtFullName.getText() != null ? txtFullName.getText().trim() : "";
        String ageText = txtAge.getText() != null ? txtAge.getText().trim() : "";
        String gender = cmbGender.getSelectionModel().getSelectedItem();
        String phone = txtPhone.getText() != null ? txtPhone.getText().trim() : "";
        String blood = cmbBloodGroup.getSelectionModel().getSelectedItem();
        Doctor assignedDoc = cmbDoctor.getSelectionModel().getSelectedItem();
        String address = txtAddress.getText() != null ? txtAddress.getText().trim() : "";
        String symptoms = txtSymptoms.getText() != null ? txtSymptoms.getText().trim() : "";
        EmergencyLevel level = cmbEmergencyLevel.getSelectionModel().getSelectedItem();

        // Validation
        if (name.isEmpty()) {
            AlertHelper.showWarning("Missing Information", "Please enter the patient's full name.");
            txtFullName.requestFocus();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age < 0 || age > 130) {
                AlertHelper.showWarning("Invalid Age", "Age must be between 0 and 130.");
                txtAge.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Invalid Age", "Please enter a valid numeric age.");
            txtAge.requestFocus();
            return;
        }

        if (gender == null) {
            AlertHelper.showWarning("Missing Information", "Please select the patient's gender.");
            cmbGender.requestFocus();
            return;
        }

        if (phone.isEmpty() || phone.length() < 7) {
            AlertHelper.showWarning("Invalid Contact", "Please provide a valid phone number (at least 7 digits).");
            txtPhone.requestFocus();
            return;
        }

        if (symptoms.isEmpty()) {
            AlertHelper.showWarning("Missing Symptoms", "Please record the symptoms or reason for emergency visit.");
            txtSymptoms.requestFocus();
            return;
        }

        if (level == null) {
            AlertHelper.showWarning("Missing Severity", "Please select an emergency triage level.");
            cmbEmergencyLevel.requestFocus();
            return;
        }

        // Build Patient
        Patient patient = new Patient();
        patient.setPatientCode(txtPatientCode.getText());
        patient.setTokenNumber(txtTokenNumber.getText());
        patient.setFullName(name);
        patient.setAge(age);
        patient.setGender(gender);
        patient.setPhone(phone);
        patient.setAddress(address);
        patient.setBloodGroup(blood);
        patient.setSymptoms(symptoms);
        patient.setEmergencyLevel(level);
        patient.setPriority(level.getPriority());
        patient.setArrivalTime(LocalDateTime.now());
        if (assignedDoc != null) {
            patient.setAssignedDoctorId(assignedDoc.getUserId());
            patient.setAssignedDoctorName(assignedDoc.getFullName());
        }
        patient.setCurrentStatus(PatientStatus.WAITING);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        int staffUserId = currentUser != null ? currentUser.getUserId() : 4; // default nurse

        try {
            PatientService patientService = mainLayoutController.getPatientService();
            patientService.registerPatient(patient, staffUserId);

            // Non-blocking notification if Critical
            if (level == EmergencyLevel.CRITICAL) {
                NotificationManager.showCriticalAlert("Immediate triage attention required: " +
                        patient.getFullName() + " (" + patient.getTokenNumber() + ")");
            } else {
                NotificationManager.showInfo("Patient Registered",
                        patient.getFullName() + " queued with Token " + patient.getTokenNumber());
            }

            boolean goToQueue = AlertHelper.showConfirmation("Patient Registered Successfully",
                    "Patient " + patient.getFullName() + " has been assigned Token [" + patient.getTokenNumber() + "]\n" +
                    "Emergency Severity: " + level.getDisplayName() + " (Priority " + level.getPriority() + ")\n" +
                    "Status: WAITING in live priority queue.\n\n" +
                    "Do you want to view the Live Emergency Queue now?");

            if (goToQueue) {
                mainLayoutController.handleNavQueue();
            } else {
                resetForm();
            }

        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Registration Failed", e.getMessage());
        }
    }

    @FXML
    public void handleClearForm() {
        resetForm();
    }

    @FXML
    public void handleViewQueue() {
        if (mainLayoutController != null) {
            mainLayoutController.handleNavQueue();
        }
    }
}
