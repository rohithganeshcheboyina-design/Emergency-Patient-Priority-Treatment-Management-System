package com.hospital.controller;

import com.hospital.model.EmergencyLevel;
import com.hospital.service.PriorityQueueService;
import com.hospital.service.ReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.Map;

/**
 * Controller for the Admin Dashboard presenting dynamic hospital statistics and JavaFX charts.
 */
public class AdminDashboardController extends BaseController {

    @FXML private Label lblTotalPatients;
    @FXML private Label lblCriticalCount;
    @FXML private Label lblHighCount;
    @FXML private Label lblMediumCount;
    @FXML private Label lblLowCount;

    @FXML private Label lblWaitingCount;
    @FXML private Label lblCalledCount;
    @FXML private Label lblInTreatmentCount;
    @FXML private Label lblTreatedCount;
    @FXML private Label lblDischargedCount;

    @FXML private PieChart chartEmergencyLevel;
    @FXML private BarChart<String, Number> chartPatientStatus;
    @FXML private BarChart<String, Number> chartWaitingTime;

    @Override
    public void onAttached() {
        loadDashboardData();
    }

    @FXML
    public void handleRefresh() {
        if (mainLayoutController != null && mainLayoutController.getQueueService() != null) {
            mainLayoutController.getQueueService().refreshFromDatabase();
        }
        loadDashboardData();
    }

    @FXML
    public void handleViewQueue() {
        if (mainLayoutController != null) {
            mainLayoutController.handleNavQueue();
        }
    }

    private void loadDashboardData() {
        if (mainLayoutController == null) return;

        ReportService reportService = mainLayoutController.getReportService();
        PriorityQueueService queueService = mainLayoutController.getQueueService();

        // 1. Load Summary Numbers
        Map<String, Integer> summary = reportService.getDashboardSummary();
        lblTotalPatients.setText(String.valueOf(summary.getOrDefault("total", 0)));
        lblCriticalCount.setText(String.valueOf(summary.getOrDefault("critical", 0)));
        lblHighCount.setText(String.valueOf(summary.getOrDefault("high", 0)));
        lblMediumCount.setText(String.valueOf(summary.getOrDefault("medium", 0)));
        lblLowCount.setText(String.valueOf(summary.getOrDefault("low", 0)));

        lblWaitingCount.setText(String.valueOf(summary.getOrDefault("waiting", 0)));
        lblCalledCount.setText(String.valueOf(summary.getOrDefault("called", 0)));
        lblInTreatmentCount.setText(String.valueOf(summary.getOrDefault("in_treatment", 0)));
        lblTreatedCount.setText(String.valueOf(summary.getOrDefault("treated", 0)));
        lblDischargedCount.setText(String.valueOf(summary.getOrDefault("discharged", 0)));

        // 2. Load Emergency Level Pie Chart
        Map<String, Integer> emergencyDist = reportService.getEmergencyDistribution();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : emergencyDist.entrySet()) {
            if (entry.getValue() > 0) {
                pieData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
            }
        }
        chartEmergencyLevel.setData(pieData);

        // 3. Load Patient Status Bar Chart
        Map<String, Integer> statusDist = reportService.getStatusDistribution();
        XYChart.Series<String, Number> statusSeries = new XYChart.Series<>();
        statusSeries.setName("Patients");
        for (Map.Entry<String, Integer> entry : statusDist.entrySet()) {
            statusSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        chartPatientStatus.getData().clear();
        chartPatientStatus.getData().add(statusSeries);

        // 4. Load Waiting Time Bar Chart
        XYChart.Series<String, Number> waitSeries = new XYChart.Series<>();
        waitSeries.setName("Avg Wait Time (Min)");
        if (queueService != null && queueService.getQueueManager() != null) {
            for (EmergencyLevel level : EmergencyLevel.values()) {
                double avgWait = queueService.getQueueManager().getAverageWaitingTimeByLevel(level);
                waitSeries.getData().add(new XYChart.Data<>(level.getDisplayName(), Math.round(avgWait * 10.0) / 10.0));
            }
        }
        chartWaitingTime.getData().clear();
        chartWaitingTime.getData().add(waitSeries);
    }
}
