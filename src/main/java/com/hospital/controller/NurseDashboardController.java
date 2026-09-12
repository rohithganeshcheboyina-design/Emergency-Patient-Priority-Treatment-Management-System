package com.hospital.controller;

import com.hospital.model.QueueItem;
import com.hospital.service.PriorityQueueService;
import com.hospital.service.ReportService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;

/**
 * Controller for Nurse Dashboard focusing on emergency triage registration and waiting queue status.
 */
public class NurseDashboardController extends BaseController {

    @FXML private Label lblWaitingPatients;
    @FXML private Label lblCriticalPatients;
    @FXML private Label lblHighPatients;
    @FXML private Label lblTotalPatientsToday;
    @FXML private Label lblQueueCountBadge;

    @FXML private TableView<QueueItem> tblQueuePreview;
    @FXML private TableColumn<QueueItem, String> colPos;
    @FXML private TableColumn<QueueItem, String> colToken;
    @FXML private TableColumn<QueueItem, String> colCode;
    @FXML private TableColumn<QueueItem, String> colName;
    @FXML private TableColumn<QueueItem, String> colAgeGender;
    @FXML private TableColumn<QueueItem, String> colEmergency;
    @FXML private TableColumn<QueueItem, String> colWaitTime;
    @FXML private TableColumn<QueueItem, String> colDoctor;
    @FXML private TableColumn<QueueItem, String> colStatus;

    @Override
    public void onAttached() {
        initTableColumns();
        loadDashboardData();
    }

    private void initTableColumns() {
        colPos.setCellValueFactory(cellData -> new SimpleStringProperty("#" + cellData.getValue().getQueuePosition()));
        colToken.setCellValueFactory(new PropertyValueFactory<>("tokenNumber"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("patientCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colAgeGender.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAge() + " / " + cellData.getValue().getGender()));
        colEmergency.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmergencyLevel().getDisplayName()));
        colWaitTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getWaitingTimeMinutes() + " min"));
        colDoctor.setCellValueFactory(cellData -> {
            String doc = cellData.getValue().getAssignedDoctorName();
            return new SimpleStringProperty(doc != null ? doc : "Unassigned");
        });
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        // Emergency badge styling
        colEmergency.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    QueueItem qi = getTableView().getItems().get(getIndex());
                    Label badge = new Label();
                    applyPriorityBadge(badge, qi.getEmergencyLevel());
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });
    }

    public void loadDashboardData() {
        if (mainLayoutController == null) return;

        PriorityQueueService queueService = mainLayoutController.getQueueService();
        ReportService reportService = mainLayoutController.getReportService();

        Map<String, Integer> summary = reportService.getDashboardSummary();
        lblWaitingPatients.setText(String.valueOf(summary.getOrDefault("waiting", 0) + summary.getOrDefault("called", 0)));
        lblCriticalPatients.setText(String.valueOf(summary.getOrDefault("critical", 0)));
        lblHighPatients.setText(String.valueOf(summary.getOrDefault("high", 0)));
        lblTotalPatientsToday.setText(String.valueOf(summary.getOrDefault("total", 0)));

        List<QueueItem> liveQueue = queueService.getLiveQueue();
        lblQueueCountBadge.setText(liveQueue.size() + " in Queue");
        tblQueuePreview.setItems(FXCollections.observableArrayList(liveQueue));
    }

    @FXML
    public void handleRegisterPatient() {
        if (mainLayoutController != null) {
            mainLayoutController.handleNavRegister();
        }
    }

    @FXML
    public void handleViewQueue() {
        if (mainLayoutController != null) {
            mainLayoutController.handleNavQueue();
        }
    }

    @FXML
    public void handleSearchPatient() {
        if (mainLayoutController != null) {
            mainLayoutController.handleNavPatients();
        }
    }

    @FXML
    public void handleRefresh() {
        if (mainLayoutController != null) {
            mainLayoutController.getQueueService().refreshFromDatabase();
        }
        loadDashboardData();
    }
}
