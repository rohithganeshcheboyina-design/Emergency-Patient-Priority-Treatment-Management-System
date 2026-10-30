package com.hospital.controller;

import com.hospital.exception.AuthenticationException;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.service.AuthenticationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the staff authentication login screen.
 */
public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private final AuthenticationService authService = new AuthenticationService();

    @FXML
    public void initialize() {
        cmbRole.setItems(FXCollections.observableArrayList(
                "Any / Auto-Detect",
                Role.ADMIN.getDisplayName(),
                Role.DOCTOR.getDisplayName(),
                Role.NURSE.getDisplayName(),
                Role.PATIENT.getDisplayName()
        ));
        cmbRole.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleLogin() {
        lblError.setVisible(false);
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String selectedRoleStr = cmbRole.getSelectionModel().getSelectedItem();

        Role expectedRole = null;
        if (selectedRoleStr != null && !selectedRoleStr.contains("Any")) {
            expectedRole = Role.fromString(selectedRoleStr);
        }

        try {
            User authenticatedUser = authService.login(username, password, expectedRole);

            // Redirect to MainLayout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 780));
            stage.setTitle("HE-PPMS - Hospital Emergency Department [" + authenticatedUser.getRole().getDisplayName() + "]");
            stage.centerOnScreen();

        } catch (AuthenticationException e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Error loading main layout: " + e.getMessage());
            lblError.setVisible(true);
        }
    }

    @FXML
    public void fillAdminDemo() {
        txtUsername.setText("admin");
        txtPassword.setText("admin123");
        cmbRole.getSelectionModel().select(Role.ADMIN.getDisplayName());
        lblError.setVisible(false);
    }

    @FXML
    public void fillDoctorDemo() {
        txtUsername.setText("doctor");
        txtPassword.setText("doctor123");
        cmbRole.getSelectionModel().select(Role.DOCTOR.getDisplayName());
        lblError.setVisible(false);
    }

    @FXML
    public void fillNurseDemo() {
        txtUsername.setText("nurse");
        txtPassword.setText("nurse123");
        cmbRole.getSelectionModel().select(Role.NURSE.getDisplayName());
        lblError.setVisible(false);
    }

    @FXML
    public void fillPatientDemo() {
        txtUsername.setText("patient");
        txtPassword.setText("doctor123");
        cmbRole.getSelectionModel().select(Role.PATIENT.getDisplayName());
        lblError.setVisible(false);
    }
}
