package com.hospital.controller;

import com.hospital.dao.PatientDAO;
import com.hospital.dao.TreatmentDAO;
import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.EmergencyLevel;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.model.Treatment;
import com.hospital.model.User;
import com.hospital.security.SessionManager;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Doctor Treatment Management Station.
 */
public class TreatmentController extends BaseController {

    @FXML private ComboBox<String> cmbStatusFilter;
    @FXML private Label lblTreatmentCountBadge;

    @FXML private TableView<TreatmentRow> tblTreatments;
    @FXML private TableColumn<TreatmentRow, String> colToken;
    @FXML private TableColumn<TreatmentRow, String> colCode;
    @FXML private TableColumn<TreatmentRow, String> colPatientName;
    @FXML private TableColumn<TreatmentRow, String> colEmergency;
    @FXML private TableColumn<TreatmentRow, String> colDoctor;
    @FXML private TableColumn<TreatmentRow, String> colStartTime;
    @FXML private TableColumn<TreatmentRow, String> colStatus;
    @FXML private TableColumn<TreatmentRow, String> colNotes;

    @FXML private VBox boxTreatmentActions;
    @FXML private Label lblSelectedPatientInfo;
    @FXML private TextArea txtClinicalNotes;
    @FXML private Button btnComplete;
    @FXML private Button btnDischarge;

    private final PatientDAO patientDAO = new PatientDAO();
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private List<TreatmentRow> allRows = new ArrayList<>();
    private TreatmentRow selectedRow;

    @Override
    public void onAttached() {
        initFilters();
        initTable();
        loadTreatments();
    }

    private void initFilters() {
        cmbStatusFilter.setItems(FXCollections.observableArrayList("All Treatments", "In Progress", "Completed"));
        cmbStatusFilter.getSelectionModel().selectFirst();
        cmbStatusFilter.setOnAction(e -> applyFilter());
    }

    private void initTable() {
        colToken.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().patient.getTokenNumber()));
        colCode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().patient.getPatientCode()));
        colPatientName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().patient.getFullName()));
        colEmergency.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().patient.getEmergencyLevel().getDisplayName()));
        colDoctor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().treatment.getDoctorName()));
        colStartTime.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().treatment.getStartTime())));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().treatment.getTreatmentStatus()));
        colNotes.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().treatment.getNotes()));

        colEmergency.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    TreatmentRow row = getTableView().getItems().get(getIndex());
                    Label badge = new Label();
                    applyPriorityBadge(badge, row.patient.getEmergencyLevel());
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        tblTreatments.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedRow = newVal;
            updateSelectedPanel();
        });
    }

    public void loadTreatments() {
        allRows.clear();
        List<Patient> patients = patientDAO.getAllPatients();
        for (Patient p : patients) {
            List<Treatment> trList = treatmentDAO.getTreatmentsByPatientId(p.getPatientId());
            for (Treatment t : trList) {
                allRows.add(new TreatmentRow(p, t));
            }
        }
        applyFilter();
    }

    private void applyFilter() {
        String filter = cmbStatusFilter.getSelectionModel().getSelectedItem();
        List<TreatmentRow> filtered = allRows.stream().filter(row -> {
            if ("In Progress".equalsIgnoreCase(filter)) {
                return "IN_PROGRESS".equalsIgnoreCase(row.treatment.getTreatmentStatus());
            } else if ("Completed".equalsIgnoreCase(filter)) {
                return "COMPLETED".equalsIgnoreCase(row.treatment.getTreatmentStatus());
            }
            return true;
        }).collect(Collectors.toList());

        tblTreatments.setItems(FXCollections.observableArrayList(filtered));
        lblTreatmentCountBadge.setText(filtered.size() + " Case(s)");

        if (!filtered.isEmpty()) {
            tblTreatments.getSelectionModel().selectFirst();
        } else {
            selectedRow = null;
            updateSelectedPanel();
        }
    }

    private void updateSelectedPanel() {
        if (selectedRow == null) {
            lblSelectedPatientInfo.setText("No patient selected.");
            txtClinicalNotes.clear();
            btnComplete.setDisable(true);
            btnDischarge.setDisable(true);
            return;
        }

        Patient p = selectedRow.patient;
        Treatment t = selectedRow.treatment;
        lblSelectedPatientInfo.setText(p.getFullName() + " [" + p.getTokenNumber() + "] (" +
                p.getEmergencyLevel().getDisplayName() + ") - Status: " + t.getTreatmentStatus());
        txtClinicalNotes.setText(t.getNotes() != null ? t.getNotes() : "");

        boolean inProgress = "IN_PROGRESS".equalsIgnoreCase(t.getTreatmentStatus());
        btnComplete.setDisable(!inProgress);
        btnDischarge.setDisable(p.getCurrentStatus() == PatientStatus.DISCHARGED);
    }

    @FXML
    public void handleSaveNotes() {
        if (selectedRow == null) return;
        selectedRow.treatment.setNotes(txtClinicalNotes.getText());
        treatmentDAO.updateTreatment(selectedRow.treatment);
        NotificationManager.showInfo("Saved", "Clinical treatment notes updated.");
        loadTreatments();
    }

    @FXML
    public void handleComplete() {
        if (selectedRow == null) return;
        User doctor = SessionManager.getInstance().getCurrentUser();
        int docId = doctor != null ? doctor.getUserId() : selectedRow.treatment.getDoctorId();

        try {
            mainLayoutController.getTreatmentService().completeTreatment(
                    selectedRow.patient.getPatientId(),
                    docId,
                    txtClinicalNotes.getText()
            );
            NotificationManager.showInfo("Treatment Concluded", selectedRow.patient.getFullName() + " marked as TREATED.");
            loadTreatments();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void handleDischarge() {
        if (selectedRow == null) return;
        User doctor = SessionManager.getInstance().getCurrentUser();
        int docId = doctor != null ? doctor.getUserId() : selectedRow.treatment.getDoctorId();

        boolean confirmed = AlertHelper.showConfirmation("Confirm Discharge",
                "Discharge patient " + selectedRow.patient.getFullName() + " (" + selectedRow.patient.getTokenNumber() + ")?");
        if (!confirmed) return;

        try {
            mainLayoutController.getTreatmentService().dischargePatient(
                    selectedRow.patient.getPatientId(),
                    docId,
                    txtClinicalNotes.getText()
            );
            NotificationManager.showInfo("Discharged", selectedRow.patient.getFullName() + " discharged.");
            loadTreatments();
        } catch (InvalidPatientDataException e) {
            AlertHelper.showError("Error", e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        loadTreatments();
    }

    public static class TreatmentRow {
        public final Patient patient;
        public final Treatment treatment;

        public TreatmentRow(Patient patient, Treatment treatment) {
            this.patient = patient;
            this.treatment = treatment;
        }
    }
}
