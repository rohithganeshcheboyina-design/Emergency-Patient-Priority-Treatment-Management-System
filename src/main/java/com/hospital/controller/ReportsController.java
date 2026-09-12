package com.hospital.controller;

import com.hospital.model.EmergencyLevel;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.service.PatientService;
import com.hospital.service.ReportService;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for generating hospital emergency departmental reports and CSV export.
 */
public class ReportsController extends BaseController {

    @FXML private Label lblReportTotal;
    @FXML private Label lblCriticalRate;
    @FXML private Label lblReportAvgWait;
    @FXML private Label lblDischargedTotal;

    @FXML private ComboBox<String> cmbReportType;
    @FXML private ComboBox<String> cmbSeverityFilter;
    @FXML private ComboBox<String> cmbStatusFilter;

    @FXML private TableView<Patient> tblReportData;
    @FXML private TableColumn<Patient, String> colCode;
    @FXML private TableColumn<Patient, String> colToken;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, String> colAgeGender;
    @FXML private TableColumn<Patient, String> colEmergency;
    @FXML private TableColumn<Patient, Integer> colPriority;
    @FXML private TableColumn<Patient, String> colArrival;
    @FXML private TableColumn<Patient, String> colWait;
    @FXML private TableColumn<Patient, String> colDoctor;
    @FXML private TableColumn<Patient, String> colStatus;

    private List<Patient> allPatients = new ArrayList<>();

    @Override
    public void onAttached() {
        initDropdowns();
        initTable();
        loadData();
    }

    private void initDropdowns() {
        cmbReportType.setItems(FXCollections.observableArrayList(
                "1. All Patients Master Report",
                "2. Daily Emergency Intake Report",
                "3. Emergency Priority Severity Report",
                "4. Patient Status Workflow Report",
                "5. Waiting Time Analysis Report"
        ));
        cmbReportType.getSelectionModel().selectFirst();
        cmbReportType.setOnAction(e -> applyFilters());

        cmbSeverityFilter.setItems(FXCollections.observableArrayList(
                "All Severities",
                EmergencyLevel.CRITICAL.getDisplayName(),
                EmergencyLevel.HIGH.getDisplayName(),
                EmergencyLevel.MEDIUM.getDisplayName(),
                EmergencyLevel.LOW.getDisplayName()
        ));
        cmbSeverityFilter.getSelectionModel().selectFirst();
        cmbSeverityFilter.setOnAction(e -> applyFilters());

        cmbStatusFilter.setItems(FXCollections.observableArrayList(
                "All Statuses",
                PatientStatus.WAITING.getDisplayName(),
                PatientStatus.CALLED.getDisplayName(),
                PatientStatus.IN_TREATMENT.getDisplayName(),
                PatientStatus.TREATED.getDisplayName(),
                PatientStatus.DISCHARGED.getDisplayName()
        ));
        cmbStatusFilter.getSelectionModel().selectFirst();
        cmbStatusFilter.setOnAction(e -> applyFilters());
    }

    private void initTable() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("patientCode"));
        colToken.setCellValueFactory(new PropertyValueFactory<>("tokenNumber"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colAgeGender.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAge() + " / " + c.getValue().getGender()));
        colEmergency.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmergencyLevel().getDisplayName()));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colArrival.setCellValueFactory(c -> new SimpleStringProperty(formatDateTime(c.getValue().getArrivalTime())));
        colWait.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWaitingTimeMinutes() + " min"));
        colDoctor.setCellValueFactory(c -> {
            String doc = c.getValue().getAssignedDoctorName();
            return new SimpleStringProperty(doc != null ? doc : "Unassigned");
        });
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCurrentStatus().getDisplayName()));

        colEmergency.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Patient p = getTableView().getItems().get(getIndex());
                    Label badge = new Label();
                    applyPriorityBadge(badge, p.getEmergencyLevel());
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Patient p = getTableView().getItems().get(getIndex());
                    Label badge = new Label();
                    applyStatusBadge(badge, p.getCurrentStatus());
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });
    }

    public void loadData() {
        if (mainLayoutController == null) return;
        PatientService service = mainLayoutController.getPatientService();
        allPatients = service.getAllPatients();
        applyFilters();
    }

    private void applyFilters() {
        String type = cmbReportType.getSelectionModel().getSelectedItem();
        String sevText = cmbSeverityFilter.getSelectionModel().getSelectedItem();
        EmergencyLevel filterSev = (sevText != null && !sevText.contains("All")) ? EmergencyLevel.fromString(sevText) : null;

        String statText = cmbStatusFilter.getSelectionModel().getSelectedItem();
        PatientStatus filterStat = (statText != null && !statText.contains("All")) ? PatientStatus.fromString(statText) : null;

        List<Patient> filtered = allPatients.stream()
                .filter(p -> filterSev == null || p.getEmergencyLevel() == filterSev)
                .filter(p -> filterStat == null || p.getCurrentStatus() == filterStat)
                .filter(p -> {
                    if (type != null && type.contains("Daily")) {
                        return p.getArrivalTime() != null &&
                               p.getArrivalTime().toLocalDate().isEqual(LocalDate.now());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        tblReportData.setItems(FXCollections.observableArrayList(filtered));

        // Update KPIs
        lblReportTotal.setText(String.valueOf(filtered.size()));
        long critCount = filtered.stream().filter(p -> p.getEmergencyLevel() == EmergencyLevel.CRITICAL).count();
        double critRate = filtered.isEmpty() ? 0.0 : (critCount * 100.0) / filtered.size();
        lblCriticalRate.setText(Math.round(critRate * 10.0) / 10.0 + "%");

        double avgWait = filtered.stream().mapToLong(Patient::getWaitingTimeMinutes).average().orElse(0.0);
        lblReportAvgWait.setText(Math.round(avgWait * 10.0) / 10.0 + " min");

        long discharged = filtered.stream().filter(p -> p.getCurrentStatus() == PatientStatus.DISCHARGED).count();
        lblDischargedTotal.setText(String.valueOf(discharged));
    }

    @FXML
    public void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Emergency Report CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("Emergency_Department_Report_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(tblReportData.getScene().getWindow());
        if (file != null) {
            try {
                mainLayoutController.getReportService().exportPatientsToCSV(file);
                NotificationManager.showInfo("Export Complete", "Report saved to " + file.getName());
                AlertHelper.showInfo("Report Exported", "The emergency report was exported successfully to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                AlertHelper.showError("Export Error", "Failed to write CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleResetFilters() {
        cmbReportType.getSelectionModel().selectFirst();
        cmbSeverityFilter.getSelectionModel().selectFirst();
        cmbStatusFilter.getSelectionModel().selectFirst();
        applyFilters();
    }

    @FXML
    public void handleRefresh() {
        loadData();
    }
}
