package com.hospital.controller;

import com.hospital.dao.UserDAO;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.model.Permission;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.security.SessionManager;
import com.hospital.service.AppointmentService;
import com.hospital.service.PatientService;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for Scheduled Outpatient Consultations & Appointments.
 * Strictly maintains protocol: Emergency triage priority queue is never delayed by appointments.
 */
public class AppointmentManagementController extends BaseController {

    @FXML private ComboBox<Patient> cmbPatient;
    @FXML private ComboBox<Doctor> cmbDoctor;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<String> cmbTime;
    @FXML private TextField txtSpecialty;
    @FXML private TextField txtReason;
    @FXML private TextArea txtNotes;

    @FXML private TableView<Appointment> tblAppointments;
    @FXML private TableColumn<Appointment, String> colTime;
    @FXML private TableColumn<Appointment, String> colPatient;
    @FXML private TableColumn<Appointment, String> colDoctor;
    @FXML private TableColumn<Appointment, String> colSpecialty;
    @FXML private TableColumn<Appointment, String> colReason;
    @FXML private TableColumn<Appointment, String> colStatus;
    @FXML private TableColumn<Appointment, Void> colAction;

    private AppointmentService appointmentService;
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void onAttached() {
        if (mainLayoutController != null) {
            this.appointmentService = mainLayoutController.getAppointmentService();
        } else {
            this.appointmentService = new AppointmentService();
        }

        initForm();
        initTable();
        loadAppointments();
    }

    private void initForm() {
        if (mainLayoutController != null) {
            List<Patient> patients = mainLayoutController.getPatientService().getAllPatients();
            cmbPatient.setItems(FXCollections.observableArrayList(patients));
            cmbPatient.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Patient p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.getFullName() + " (" + p.getPatientCode() + ")");
                }
            });
            cmbPatient.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Patient p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.getFullName() + " (" + p.getPatientCode() + ")");
                }
            });
        }

        List<Doctor> doctors = userDAO.getAvailableDoctors();
        cmbDoctor.setItems(FXCollections.observableArrayList(doctors));
        cmbDoctor.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Doctor d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.getFullName() + " (" + d.getSpecialization() + ")");
            }
        });
        cmbDoctor.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Doctor d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.getFullName());
            }
        });

        dpDate.setValue(LocalDate.now().plusDays(1));
        cmbTime.setItems(FXCollections.observableArrayList(
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"
        ));
        cmbTime.getSelectionModel().select(0);

        cmbDoctor.setOnAction(e -> {
            Doctor doc = cmbDoctor.getSelectionModel().getSelectedItem();
            if (doc != null && (txtSpecialty.getText() == null || txtSpecialty.getText().isEmpty())) {
                txtSpecialty.setText(doc.getSpecialization());
            }
        });
    }

    private void initTable() {
        colTime.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getAppointmentTime())));
        colPatient.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPatientName() + " (" + cellData.getValue().getPatientCode() + ")"));
        colDoctor.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colSpecialty.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        // Status badge
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("badge-status");
                    if ("Confirmed".equalsIgnoreCase(status) || "Scheduled".equalsIgnoreCase(status)) {
                        badge.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-weight: bold;");
                    } else if ("Completed".equalsIgnoreCase(status)) {
                        badge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #64748B;");
                    }
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        // Action: Cancel
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnCancel = new Button("Cancel");
            {
                btnCancel.getStyleClass().add("btn-secondary");
                btnCancel.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                btnCancel.setOnAction(e -> {
                    Appointment a = getTableView().getItems().get(getIndex());
                    handleCancelAppointment(a);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment a = getTableView().getItems().get(getIndex());
                    btnCancel.setDisable(a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.COMPLETED);
                    setGraphic(btnCancel);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public void loadAppointments() {
        if (appointmentService == null) return;
        List<Appointment> list = appointmentService.getAllAppointments();
        tblAppointments.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void handleBookAppointment() {
        Patient patient = cmbPatient.getSelectionModel().getSelectedItem();
        Doctor doctor = cmbDoctor.getSelectionModel().getSelectedItem();
        LocalDate date = dpDate.getValue();
        String timeStr = cmbTime.getSelectionModel().getSelectedItem();
        String specialty = txtSpecialty.getText() != null ? txtSpecialty.getText().trim() : "";
        String reason = txtReason.getText() != null ? txtReason.getText().trim() : "";
        String notes = txtNotes.getText() != null ? txtNotes.getText().trim() : "";

        if (patient == null) {
            AlertHelper.showWarning("Patient Required", "Please select a patient.");
            return;
        }
        if (doctor == null) {
            AlertHelper.showWarning("Doctor Required", "Please select a consulting doctor.");
            return;
        }
        if (date == null) {
            AlertHelper.showWarning("Date Required", "Please select a consultation date.");
            return;
        }

        LocalTime time = LocalTime.parse(timeStr != null ? timeStr : "09:00");
        LocalDateTime apptDateTime = LocalDateTime.of(date, time);

        Appointment a = new Appointment();
        a.setPatientId(patient.getPatientId());
        a.setDoctorId(doctor.getUserId());
        a.setAppointmentTime(apptDateTime);
        a.setSpecialty(specialty.isEmpty() ? doctor.getSpecialization() : specialty);
        a.setReason(reason.isEmpty() ? "Follow-up consultation" : reason);
        a.setNotes(notes);
        a.setStatus(AppointmentStatus.SCHEDULED);

        try {
            appointmentService.scheduleAppointment(a);
            NotificationManager.showInfo("Appointment Booked", "Consultation scheduled for " + patient.getFullName() + " on " + formatDateTime(apptDateTime));
            txtReason.clear();
            txtNotes.clear();
            loadAppointments();
        } catch (Exception e) {
            AlertHelper.showError("Booking Error", e.getMessage());
        }
    }

    private void handleCancelAppointment(Appointment a) {
        if (a == null) return;
        boolean confirmed = AlertHelper.showConfirmation("Cancel Consultation", "Cancel scheduled appointment on " + formatDateTime(a.getAppointmentTime()) + "?");
        if (confirmed) {
            appointmentService.cancelAppointment(a.getAppointmentId(), "Cancelled via scheduling desk");
            NotificationManager.showInfo("Appointment Cancelled", "Appointment marked as cancelled.");
            loadAppointments();
        }
    }

    @FXML
    public void handleRefresh() {
        loadAppointments();
    }
}
