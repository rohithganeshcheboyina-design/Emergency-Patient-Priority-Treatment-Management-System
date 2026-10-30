package com.hospital.controller;

import com.hospital.model.EmergencyLevel;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.service.PatientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Patient Management Directory supporting search, filters, and profile view.
 */
public class PatientManagementController extends BaseController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterLevel;
    @FXML private ComboBox<String> cmbFilterStatus;
    @FXML private Label lblTotalCountBadge;
    @FXML private Button btnRegisterPatient;

    @FXML private TableView<Patient> tblPatients;
    @FXML private TableColumn<Patient, String> colCode;
    @FXML private TableColumn<Patient, String> colToken;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, String> colAgeGender;
    @FXML private TableColumn<Patient, String> colPhone;
    @FXML private TableColumn<Patient, String> colEmergency;
    @FXML private TableColumn<Patient, String> colArrival;
    @FXML private TableColumn<Patient, String> colDoctor;
    @FXML private TableColumn<Patient, String> colStatus;
    @FXML private TableColumn<Patient, Void> colAction;

    private List<Patient> allPatients = new ArrayList<>();

    @Override
    public void onAttached() {
        boolean canRegister = com.hospital.security.SessionManager.getInstance().hasPermission(com.hospital.model.Permission.REGISTER_PATIENTS);
        if (btnRegisterPatient != null) {
            btnRegisterPatient.setVisible(canRegister);
            btnRegisterPatient.setManaged(canRegister);
        }
        initFilters();
        initTable();
        loadPatients();
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
        cmbFilterLevel.setOnAction(e -> applyFilter());

        cmbFilterStatus.setItems(FXCollections.observableArrayList(
                "All Statuses",
                PatientStatus.WAITING.getDisplayName(),
                PatientStatus.CALLED.getDisplayName(),
                PatientStatus.IN_TREATMENT.getDisplayName(),
                PatientStatus.TREATED.getDisplayName(),
                PatientStatus.DISCHARGED.getDisplayName(),
                PatientStatus.CANCELLED.getDisplayName()
        ));
        cmbFilterStatus.getSelectionModel().selectFirst();
        cmbFilterStatus.setOnAction(e -> applyFilter());

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    private void initTable() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("patientCode"));
        colToken.setCellValueFactory(new PropertyValueFactory<>("tokenNumber"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colAgeGender.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAge() + " / " + cellData.getValue().getGender()));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmergency.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmergencyLevel().getDisplayName()));
        colArrival.setCellValueFactory(cellData -> new SimpleStringProperty(formatDateTime(cellData.getValue().getArrivalTime())));
        colDoctor.setCellValueFactory(cellData -> {
            String doc = cellData.getValue().getAssignedDoctorName();
            return new SimpleStringProperty(doc != null ? doc : "Unassigned");
        });
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCurrentStatus().getDisplayName()));

        // Emergency badge styling
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

        // Status badge styling
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

        // Profile Action Button
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnView = new Button("View");

            {
                btnView.getStyleClass().add("btn-secondary");
                btnView.setStyle("-fx-font-size: 11px; -fx-padding: 3px 10px;");
                btnView.setOnAction(e -> {
                    Patient p = getTableView().getItems().get(getIndex());
                    openPatientDetail(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnView);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Double click row opens details
        tblPatients.setRowFactory(tv -> {
            TableRow<Patient> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    openPatientDetail(row.getItem());
                }
            });
            return row;
        });
    }

    public void loadPatients() {
        if (mainLayoutController == null) return;
        PatientService service = mainLayoutController.getPatientService();
        allPatients = service.getAllPatients();
        applyFilter();
    }

    private void applyFilter() {
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

        String query = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";

        EmergencyLevel finalLevel = level;
        PatientStatus finalStatus = status;

        List<Patient> filtered = allPatients.stream()
                .filter(p -> finalLevel == null || p.getEmergencyLevel() == finalLevel)
                .filter(p -> finalStatus == null || p.getCurrentStatus() == finalStatus)
                .filter(p -> {
                    if (query.isEmpty()) return true;
                    return (p.getFullName() != null && p.getFullName().toLowerCase().contains(query)) ||
                           (p.getPatientCode() != null && p.getPatientCode().toLowerCase().contains(query)) ||
                           (p.getTokenNumber() != null && p.getTokenNumber().toLowerCase().contains(query)) ||
                           (p.getPhone() != null && p.getPhone().contains(query));
                })
                .collect(Collectors.toList());

        tblPatients.setItems(FXCollections.observableArrayList(filtered));
        lblTotalCountBadge.setText("Total: " + filtered.size() + " Patients");
    }

    private void openPatientDetail(Patient patient) {
        if (patient == null || mainLayoutController == null) return;
        PatientDetailController.setSelectedPatientId(patient.getPatientId());
        mainLayoutController.loadView("/fxml/PatientDetail.fxml", "Patient Profile: " + patient.getFullName(), null);
    }

    @FXML
    public void handleResetFilters() {
        txtSearch.clear();
        cmbFilterLevel.getSelectionModel().selectFirst();
        cmbFilterStatus.getSelectionModel().selectFirst();
        applyFilter();
    }

    @FXML
    public void handleRefresh() {
        loadPatients();
    }

    @FXML
    public void handleNewRegistration() {
        if (!com.hospital.security.SessionManager.getInstance().hasPermission(com.hospital.model.Permission.REGISTER_PATIENTS)) {
            com.hospital.util.AlertHelper.showError("Access Denied", "Only triage nurses are authorized to register incoming emergency patients.");
            return;
        }
        if (mainLayoutController != null) {
            mainLayoutController.handleNavRegister();
        }
    }
}
