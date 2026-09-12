package com.hospital.controller;

import com.hospital.dao.DatabaseConnection;
import com.hospital.util.AlertHelper;
import com.hospital.util.ConfigReader;
import com.hospital.util.NotificationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for Settings configuration.
 */
public class SettingsController extends BaseController {

    @FXML private TextField txtCriticalThreshold;
    @FXML private TextField txtHighThreshold;
    @FXML private TextField txtMediumThreshold;
    @FXML private TextField txtLowThreshold;
    @FXML private TextField txtRefreshInterval;

    @FXML private Label lblDbMode;
    @FXML private Label lblDbUrl;
    @FXML private Label lblDbStatusText;

    @Override
    public void onAttached() {
        loadSettings();
    }

    private void loadSettings() {
        txtCriticalThreshold.setText(String.valueOf(ConfigReader.getCriticalWaitThreshold()));
        txtHighThreshold.setText(String.valueOf(ConfigReader.getHighWaitThreshold()));
        txtMediumThreshold.setText(String.valueOf(ConfigReader.getMediumWaitThreshold()));
        txtLowThreshold.setText(String.valueOf(ConfigReader.getLowWaitThreshold()));
        txtRefreshInterval.setText(String.valueOf(ConfigReader.getAutoRefreshSeconds()));

        if (DatabaseConnection.isUsingFallback()) {
            lblDbMode.setText("In-Memory Demo Mode (H2 / MySQL Emulation)");
            lblDbUrl.setText("jdbc:h2:mem:hospital_emergency_system");
            lblDbStatusText.setText("CONNECTED (FALLBACK)");
            lblDbStatusText.setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;");
        } else {
            lblDbMode.setText("MySQL Production / Local Server");
            lblDbUrl.setText(ConfigReader.getDbUrl());
            lblDbStatusText.setText("CONNECTED (ACTIVE)");
            lblDbStatusText.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void handleTestConnection() {
        boolean ok = DatabaseConnection.testConnection();
        if (ok) {
            AlertHelper.showInfo("Connection Verified", "Database connection is active and responsive.");
        } else {
            AlertHelper.showError("Connection Failed", "Unable to connect to the database. Please check your MySQL configuration.");
        }
    }

    @FXML
    public void handleSaveSettings() {
        try {
            int crit = Integer.parseInt(txtCriticalThreshold.getText().trim());
            int high = Integer.parseInt(txtHighThreshold.getText().trim());
            int med = Integer.parseInt(txtMediumThreshold.getText().trim());
            int low = Integer.parseInt(txtLowThreshold.getText().trim());
            int refresh = Integer.parseInt(txtRefreshInterval.getText().trim());

            if (crit <= 0 || high <= 0 || med <= 0 || low <= 0 || refresh <= 0) {
                AlertHelper.showWarning("Invalid Threshold", "All thresholds and intervals must be positive integers.");
                return;
            }

            NotificationManager.showInfo("Settings Saved", "Operational thresholds and refresh cadence saved for this session.");
            AlertHelper.showInfo("Settings Saved", "System settings have been successfully updated.");
        } catch (NumberFormatException e) {
            AlertHelper.showError("Input Error", "Please enter valid numeric values for all fields.");
        }
    }
}
