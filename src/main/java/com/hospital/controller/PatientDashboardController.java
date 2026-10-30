package com.hospital.controller;

import com.hospital.dao.PatientDAO;
import com.hospital.dao.TreatmentDAO;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.model.Permission;
import com.hospital.model.Treatment;
import com.hospital.model.User;
import com.hospital.security.SessionManager;
import com.hospital.service.AppointmentService;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller for Patient Portal self-service dashboard.
 */
public class PatientDashboardController extends BaseController {

    @FXML private Label lblPatientName;
    @FXML private Label lblPatientCode;
    @FXML private Label lblTokenNumber;
    @FXML private Label lblTriageStatus;
    @FXML private Label lblEmergencyPriority;
    @FXML private Label lblAssignedDoctor;
    @FXML private Label lblQueueStatusNotice;

    @FXML private TableView<Appointment> tblMyAppointments;
    @FXML private TableColumn<Appointment, String> colApptTime;
    @FXML private TableColumn<Appointment, String> colApptDoctor;
    @FXML private TableColumn<Appointment, String> colApptSpecialty;
    @FXML private TableColumn<Appointment, String> colApptStatus;
    @FXML private TableColumn<Appointment, String> colApptNotes;
    @FXML private TableColumn<Appointment, Void> colApptAction;

    @FXML private TableView<Treatment> tblMyTreatments;
    @FXML private TableColumn<Treatment, String> colTrDate;
    @FXML private TableColumn<Treatment, String> colTrDoctor;
    @FXML private TableColumn<Treatment, String> colTrDiagnosis;
    @FXML private TableColumn<Treatment, String> colTrMedications;
    @FXML private TableColumn<Treatment, String> colTrSummary;

    private final PatientDAO patientDAO = new PatientDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private AppointmentService appointmentService;

    @Override
    public void onAttached() {
        SessionManager.getInstance().requirePermission(Permission.VIEW_PATIENT_DASHBOARD);

        if (mainLayoutController != null) {
            this.appointmentService = mainLayoutController.getAppointmentService();
        } else {
            this.appointmentService = new AppointmentService();
        }

        initTables();
        loadPatientData();
    }

    private void initTables() {
        colApptTime.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getAppointmentTime())));
        colApptDoctor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDoctorName()));
        colApptSpecialty.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSpecialty()));
        colApptStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        colApptNotes.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNotes()));

        colApptAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnCancel = new Button("Cancel");
            {
                btnCancel.getStyleClass().add("btn-secondary");
                btnCancel.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px; -fx-text-fill: #DC2626;");
                btnCancel.setOnAction(e -> {
                    Appointment appt = getTableView().getItems().get(getIndex());
                    handleCancelAppointment(appt);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appt = getTableView().getItems().get(getIndex());
                    if (appt.getStatus() == AppointmentStatus.SCHEDULED) {
                        setGraphic(btnCancel);
                        setAlignment(Pos.CENTER);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        colTrDate.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getStartTime())));
        colTrDoctor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDoctorName()));
        colTrDiagnosis.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDiagnosis() != null ? cellData.getValue().getDiagnosis() : "Standard Clinical Review"));
        colTrMedications.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMedications() != null ? cellData.getValue().getMedications() : "None Recorded"));
        colTrSummary.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNotes()));
    }

    private void loadPatientData() {
        User user = SessionManager.getInstance().getCurrentUser();
        // Match patient record (e.g. by full name or default to demo patient 1)
        Patient patient = patientDAO.findById(1);
        if (patient == null) return;

        lblPatientName.setText(patient.getFullName());
        lblPatientCode.setText("Patient ID: " + patient.getPatientCode());
        lblTokenNumber.setText("Emergency Token: " + patient.getTokenNumber());
        applyStatusBadge(lblTriageStatus, patient.getCurrentStatus());
        applyPriorityBadge(lblEmergencyPriority, patient.getEmergencyLevel());
        lblAssignedDoctor.setText(patient.getAssignedDoctorName() != null ? patient.getAssignedDoctorName() : "Triage Team");

        if (patient.getCurrentStatus() == PatientStatus.WAITING) {
            lblQueueStatusNotice.setText("You are currently active in the Emergency Waiting Queue. Our triage team will summon your token " + patient.getTokenNumber() + " shortly.");
        } else if (patient.getCurrentStatus() == PatientStatus.IN_TREATMENT) {
            lblQueueStatusNotice.setText("You are currently receiving clinical emergency care under Dr. " + patient.getAssignedDoctorName() + ".");
        } else if (patient.getCurrentStatus() == PatientStatus.DISCHARGED) {
            lblQueueStatusNotice.setText("You have been discharged from the emergency department. Please review discharge summaries and medications below.");
        } else {
            lblQueueStatusNotice.setText("Status: " + patient.getCurrentStatus().getDisplayName());
        }

        if (appointmentService != null) {
            List<Appointment> appts = appointmentService.getAppointmentsByPatient(patient.getPatientId());
            tblMyAppointments.setItems(FXCollections.observableArrayList(appts));
        }

        List<Treatment> treatments = treatmentDAO.getTreatmentsByPatientId(patient.getPatientId());
        tblMyTreatments.setItems(FXCollections.observableArrayList(treatments));
    }

    private void handleCancelAppointment(Appointment appt) {
        if (appt == null) return;
        boolean confirmed = AlertHelper.showConfirmation("Cancel Consultation",
                "Are you sure you want to cancel your consultation with " + appt.getDoctorName() +
                " scheduled for " + formatDateTime(appt.getAppointmentTime()) + "?");
        if (!confirmed) return;

        if (appointmentService != null) {
            boolean success = appointmentService.cancelAppointment(appt.getAppointmentId(), "Cancelled by patient via portal.");
            if (success) {
                NotificationManager.showInfo("Appointment Cancelled", "Your appointment has been cancelled.");
                loadPatientData();
            } else {
                AlertHelper.showError("Cancellation Error", "Could not cancel appointment.");
            }
        }
    }

    @FXML
    public void handleBookAppointment() {
        if (mainLayoutController != null) {
            mainLayoutController.handleNavAppointments();
        }
    }

    @FXML
    public void handleRefresh() {
        loadPatientData();
    }
}
