package com.hospital.controller;

import com.hospital.model.Bed;
import com.hospital.model.BedStatus;
import com.hospital.model.BedType;
import com.hospital.model.Patient;
import com.hospital.model.Permission;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.security.SessionManager;
import com.hospital.service.BedService;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for Hospital Emergency Bed Management and Ward Allocations.
 */
public class BedManagementController extends BaseController {

    @FXML private Label lblTotalBeds;
    @FXML private Label lblAvailableBeds;
    @FXML private Label lblOccupiedBeds;
    @FXML private Label lblEmergencyBeds;
    @FXML private Label lblIcuBeds;

    @FXML private ComboBox<String> cmbTypeFilter;
    @FXML private ComboBox<String> cmbStatusFilter;

    @FXML private TableView<Bed> tblBeds;
    @FXML private TableColumn<Bed, Integer> colBedId;
    @FXML private TableColumn<Bed, String> colBedNumber;
    @FXML private TableColumn<Bed, String> colBedType;
    @FXML private TableColumn<Bed, String> colStatus;
    @FXML private TableColumn<Bed, String> colPatient;
    @FXML private TableColumn<Bed, String> colAssignedAt;
    @FXML private TableColumn<Bed, String> colNotes;
    @FXML private TableColumn<Bed, Void> colAction;

    private BedService bedService;
    private List<Bed> allBeds = new ArrayList<>();

    @Override
    public void onAttached() {
        SessionManager.getInstance().requirePermission(Permission.VIEW_BEDS);

        if (mainLayoutController != null) {
            this.bedService = mainLayoutController.getBedService();
        } else {
            this.bedService = new BedService();
        }

        initFilters();
        initTable();
        loadBeds();
    }

    private void initFilters() {
        cmbTypeFilter.setItems(FXCollections.observableArrayList(
                "All Bed Types",
                BedType.EMERGENCY.getDisplayName(),
                BedType.ICU.getDisplayName(),
                BedType.TRAUMA_BAY.getDisplayName(),
                BedType.OBSERVATION.getDisplayName(),
                BedType.GENERAL_WARD.getDisplayName()
        ));
        cmbTypeFilter.getSelectionModel().selectFirst();
        cmbTypeFilter.setOnAction(e -> applyFilter());

        cmbStatusFilter.setItems(FXCollections.observableArrayList(
                "All Statuses",
                BedStatus.AVAILABLE.getDisplayName(),
                BedStatus.OCCUPIED.getDisplayName(),
                BedStatus.RESERVED.getDisplayName(),
                BedStatus.MAINTENANCE.getDisplayName()
        ));
        cmbStatusFilter.getSelectionModel().selectFirst();
        cmbStatusFilter.setOnAction(e -> applyFilter());
    }

    private void initTable() {
        colBedId.setCellValueFactory(new PropertyValueFactory<>("bedId"));
        colBedNumber.setCellValueFactory(new PropertyValueFactory<>("bedNumber"));
        colBedType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBedType().getDisplayName()));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        colPatient.setCellValueFactory(cellData -> {
            Bed b = cellData.getValue();
            if (b.getAssignedPatientName() != null) {
                return new SimpleStringProperty(b.getAssignedPatientName() + " (" + b.getAssignedPatientCode() + ")");
            }
            return new SimpleStringProperty("—");
        });

        colAssignedAt.setCellValueFactory(cellData -> {
            Bed b = cellData.getValue();
            return new SimpleStringProperty(b.getAssignedAt() != null ? formatDateTime(b.getAssignedAt()) : "—");
        });

        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Status badge factory
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
                    if ("Available".equalsIgnoreCase(status)) {
                        badge.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #15803D; -fx-font-weight: bold;");
                    } else if ("Occupied".equalsIgnoreCase(status)) {
                        badge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #B45309; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        // Action column: Assign / Release Bed
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnAssign = new Button("Assign");
            private final Button btnRelease = new Button("Release");
            private final HBox pane = new HBox(6, btnAssign, btnRelease);

            {
                btnAssign.getStyleClass().add("btn-primary");
                btnAssign.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                btnRelease.getStyleClass().add("btn-secondary");
                btnRelease.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                pane.setAlignment(Pos.CENTER);

                btnAssign.setOnAction(e -> {
                    Bed b = getTableView().getItems().get(getIndex());
                    handleAssignBed(b);
                });

                btnRelease.setOnAction(e -> {
                    Bed b = getTableView().getItems().get(getIndex());
                    handleReleaseBed(b);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Bed b = getTableView().getItems().get(getIndex());
                    boolean canAssignRelease = SessionManager.getInstance().hasPermission(Permission.ASSIGN_BEDS);
                    if (!canAssignRelease) {
                        setGraphic(new Label("Read-only"));
                    } else {
                        btnAssign.setVisible(b.getStatus() == BedStatus.AVAILABLE);
                        btnAssign.setManaged(b.getStatus() == BedStatus.AVAILABLE);
                        btnRelease.setVisible(b.getStatus() == BedStatus.OCCUPIED);
                        btnRelease.setManaged(b.getStatus() == BedStatus.OCCUPIED);
                        setGraphic(pane);
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public void loadBeds() {
        if (bedService == null) return;
        allBeds = bedService.getAllBeds();
        applyFilter();
        loadStatistics();
    }

    private void loadStatistics() {
        if (bedService == null) return;
        Map<String, Integer> stats = bedService.getBedStatistics();
        lblTotalBeds.setText(String.valueOf(stats.getOrDefault("total", 0)));
        lblAvailableBeds.setText(String.valueOf(stats.getOrDefault("available", 0)));
        lblOccupiedBeds.setText(String.valueOf(stats.getOrDefault("occupied", 0)));
        lblEmergencyBeds.setText(String.valueOf(stats.getOrDefault("emergency", 0)));
        lblIcuBeds.setText(String.valueOf(stats.getOrDefault("icu", 0)));
    }

    private void applyFilter() {
        String typeFilter = cmbTypeFilter.getSelectionModel().getSelectedItem();
        String statusFilter = cmbStatusFilter.getSelectionModel().getSelectedItem();

        List<Bed> filtered = allBeds.stream()
                .filter(b -> typeFilter == null || typeFilter.contains("All") || b.getBedType().getDisplayName().equalsIgnoreCase(typeFilter))
                .filter(b -> statusFilter == null || statusFilter.contains("All") || b.getStatus().getDisplayName().equalsIgnoreCase(statusFilter))
                .collect(Collectors.toList());

        tblBeds.setItems(FXCollections.observableArrayList(filtered));
    }

    private void handleAssignBed(Bed bed) {
        if (bed == null || mainLayoutController == null) return;

        List<Patient> activePatients = mainLayoutController.getPatientService().getAllPatients().stream()
                .filter(p -> p.getCurrentStatus().isActiveInQueue() || p.getCurrentStatus() == com.hospital.model.PatientStatus.IN_TREATMENT)
                .collect(Collectors.toList());

        if (activePatients.isEmpty()) {
            AlertHelper.showWarning("No Active Patients", "There are currently no waiting or in-treatment patients to assign.");
            return;
        }

        ChoiceDialog<Patient> dialog = new ChoiceDialog<>(activePatients.get(0), activePatients);
        dialog.setTitle("Assign Bed " + bed.getBedNumber());
        dialog.setHeaderText("Select an active patient to allocate to Bed " + bed.getBedNumber() + " (" + bed.getBedType().getDisplayName() + ")");
        dialog.setContentText("Patient:");

        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            try {
                bedService.assignBed(bed.getBedId(), patient.getPatientId(), "Allocated at triage/treatment station");
                NotificationManager.showInfo("Bed Allocated", "Bed " + bed.getBedNumber() + " assigned to " + patient.getFullName());
                loadBeds();
            } catch (Exception e) {
                AlertHelper.showError("Allocation Error", e.getMessage());
            }
        });
    }

    private void handleReleaseBed(Bed bed) {
        if (bed == null) return;
        boolean confirm = AlertHelper.showConfirmation("Release Bed", "Release Bed " + bed.getBedNumber() + " back to the available pool?");
        if (confirm) {
            bedService.releaseBed(bed.getBedId());
            NotificationManager.showInfo("Bed Released", "Bed " + bed.getBedNumber() + " is now sanitized and available.");
            loadBeds();
        }
    }

    @FXML
    public void handleRefresh() {
        loadBeds();
    }
}
