package com.hospital.controller;

import com.hospital.model.Permission;
import com.hospital.model.Role;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for the Role-Based Access Control (RBAC) Matrix screen.
 * Displays exhaustive allowed and restricted permissions for Admin, Doctor, Nurse, and Patient roles
 * with high-visibility green checkmarks and red cross marks.
 */
public class RoleAuthorizationController extends BaseController {

    @FXML private TableView<PermissionMatrixRow> tblPermissions;
    @FXML private TableColumn<PermissionMatrixRow, String> colCategory;
    @FXML private TableColumn<PermissionMatrixRow, String> colPermission;
    @FXML private TableColumn<PermissionMatrixRow, String> colDescription;
    @FXML private TableColumn<PermissionMatrixRow, String> colAdmin;
    @FXML private TableColumn<PermissionMatrixRow, String> colDoctor;
    @FXML private TableColumn<PermissionMatrixRow, String> colNurse;
    @FXML private TableColumn<PermissionMatrixRow, String> colPatient;

    @Override
    public void onAttached() {
        initTable();
        loadMatrix();
    }

    private void initTable() {
        colCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().category));
        colPermission.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().permissionName));
        colDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().description));

        colAdmin.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().adminAllowed ? "✔ ALLOWED" : "✖ RESTRICTED"));
        colDoctor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().doctorAllowed ? "✔ ALLOWED" : "✖ RESTRICTED"));
        colNurse.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().nurseAllowed ? "✔ ALLOWED" : "✖ RESTRICTED"));
        colPatient.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().patientAllowed ? "✔ ALLOWED" : "✖ RESTRICTED"));

        setupCheckmarkCellFactory(colAdmin);
        setupCheckmarkCellFactory(colDoctor);
        setupCheckmarkCellFactory(colNurse);
        setupCheckmarkCellFactory(colPatient);
    }

    private void setupCheckmarkCellFactory(TableColumn<PermissionMatrixRow, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.setAlignment(Pos.CENTER);
                    if (item.contains("ALLOWED")) {
                        badge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;");
                    } else {
                        badge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-font-weight: bold; -fx-padding: 3px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void loadMatrix() {
        ObservableList<PermissionMatrixRow> rows = FXCollections.observableArrayList(
                // 1. Dashboards & System Access
                new PermissionMatrixRow("Dashboards", "Admin Dashboard", "Access administrative oversight & department statistics", true, false, false, false),
                new PermissionMatrixRow("Dashboards", "Doctor Dashboard", "Access clinical workstation & NEXT PATIENT card", false, true, false, false),
                new PermissionMatrixRow("Dashboards", "Nurse Dashboard", "Access triage intake workstation & queue monitor", false, false, true, false),
                new PermissionMatrixRow("Dashboards", "Patient Dashboard", "Access self-service appointments & medical summary", false, false, false, true),

                // 2. Patient Registration & Triage
                new PermissionMatrixRow("Patient Intake", "Register Patients", "Emergency intake & registration of incoming patients", false, false, true, false),
                new PermissionMatrixRow("Patient Intake", "Emergency Queue View", "Real-time monitoring of prioritized emergency queue", true, true, true, false),
                new PermissionMatrixRow("Patient Intake", "View Patient Details & Symptoms", "Access demographics, symptoms, and vital signs", true, true, true, false),
                new PermissionMatrixRow("Patient Intake", "Update Vitals & Triage Observations", "Record nursing notes, BP, SpO2, and vital updates", false, false, true, false),
                new PermissionMatrixRow("Patient Intake", "Call Patient to Triage Bay", "Summon patient to examination counter", false, true, true, false),

                // 3. Clinical Treatment & Discharge
                new PermissionMatrixRow("Clinical Care", "Start Clinical Treatment", "Initiate physician emergency clinical care", false, true, false, false),
                new PermissionMatrixRow("Clinical Care", "Update Treatment Status & Notes", "Record clinical progress, diagnosis, and medications", false, true, false, false),
                new PermissionMatrixRow("Clinical Care", "Complete Treatment", "Finalize clinical treatment course", false, true, false, false),
                new PermissionMatrixRow("Clinical Care", "Discharge Patient", "Discharge patient with clinical discharge summary", false, true, false, false),
                new PermissionMatrixRow("Clinical Care", "View Patient Medical History", "Inspect comprehensive multi-encounter timeline", true, true, true, false),

                // 4. Administration & Governance
                new PermissionMatrixRow("Administration", "Staff & User Management", "Create, edit, and deactivate hospital user accounts", true, false, false, false),
                new PermissionMatrixRow("Administration", "Manage Doctors & Nurses", "Assign hospital department staff and specializations", true, false, false, false),
                new PermissionMatrixRow("Administration", "View Reports & Department Analytics", "Generate and export triage statistical reports", true, false, false, false),
                new PermissionMatrixRow("Administration", "System Activity & Audit Logs", "Inspect immutable audit log transitions", true, false, false, false),
                new PermissionMatrixRow("Administration", "System Settings & Thresholds", "Configure overdue wait alert limits & refresh times", true, false, false, false),

                // 5. Bed Management
                new PermissionMatrixRow("Hospital Beds", "Manage Bed Inventory", "Add and configure emergency, ICU, and ward beds", true, false, false, false),
                new PermissionMatrixRow("Hospital Beds", "View Bed Availability Status", "Inspect real-time bed capacity and occupancy", true, true, true, false),
                new PermissionMatrixRow("Hospital Beds", "Assign & Release Beds", "Allocate beds to patients and free on discharge", true, true, true, false),

                // 6. Consultations & Patient Self-Service
                new PermissionMatrixRow("Appointments", "Schedule Non-Emergency Consultations", "Manage outpatient scheduled clinic visits", true, true, false, false),
                new PermissionMatrixRow("Patient Portal", "View Own Profile & Records", "Patient self-service profile & consultation history", false, false, false, true),
                new PermissionMatrixRow("Patient Portal", "Book / Cancel Own Appointments", "Patient self-booking for non-emergency visits", false, false, false, true)
        );

        tblPermissions.setItems(rows);
    }

    public static class PermissionMatrixRow {
        public final String category;
        public final String permissionName;
        public final String description;
        public final boolean adminAllowed;
        public final boolean doctorAllowed;
        public final boolean nurseAllowed;
        public final boolean patientAllowed;

        public PermissionMatrixRow(String category, String permissionName, String description,
                                   boolean adminAllowed, boolean doctorAllowed, boolean nurseAllowed, boolean patientAllowed) {
            this.category = category;
            this.permissionName = permissionName;
            this.description = description;
            this.adminAllowed = adminAllowed;
            this.doctorAllowed = doctorAllowed;
            this.nurseAllowed = nurseAllowed;
            this.patientAllowed = patientAllowed;
        }
    }
}
