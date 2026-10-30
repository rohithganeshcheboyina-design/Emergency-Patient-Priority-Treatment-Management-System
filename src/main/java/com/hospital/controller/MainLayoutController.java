package com.hospital.controller;

import com.hospital.dao.DatabaseConnection;
import com.hospital.dao.UserDAO;
import com.hospital.model.Permission;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.security.SessionManager;
import com.hospital.service.*;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the main application shell with role-adaptive sidebar navigation,
 * header, live clock, and strict RBAC permission validation.
 */
public class MainLayoutController {

    @FXML private Label lblUserFullName;
    @FXML private Label lblUserRole;
    @FXML private Label lblUsername;
    @FXML private Label lblHeaderTitle;
    @FXML private Label lblLiveClock;
    @FXML private Label lblDbStatus;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavQueue;
    @FXML private Button btnNavRegister;
    @FXML private Button btnNavPatients;
    @FXML private Button btnNavTreatment;
    @FXML private Button btnNavBeds;
    @FXML private Button btnNavAppointments;
    @FXML private Button btnNavReports;
    @FXML private Button btnNavLogs;
    @FXML private Button btnNavUsers;
    @FXML private Button btnNavRoleAuth;
    @FXML private Button btnNavSettings;
    @FXML private Button btnNavAbout;
    @FXML private Button btnLogout;

    @FXML private StackPane contentStackPane;
    @FXML private VBox notificationOverlay;

    private final List<Button> navButtons = new ArrayList<>();
    private Timeline clockTimeline;

    // Shared Central Services
    private PriorityQueueManager queueManager;
    private PriorityQueueService queueService;
    private PatientService patientService;
    private TreatmentService treatmentService;
    private BedService bedService;
    private AppointmentService appointmentService;
    private ReportService reportService;
    private ActivityLogService activityLogService;
    private AuthenticationService authenticationService;
    private UserDAO userDAO;

    @FXML
    public void initialize() {
        initServices();
        initClock();
        initNavButtons();
        NotificationManager.setContainer(notificationOverlay);

        updateDatabaseStatusLabel();
        setupUserSessionAndPermissions();

        // Default to role-specific dashboard
        handleNavDashboard();
    }

    private void initServices() {
        this.queueManager = new PriorityQueueManager();
        this.queueService = new PriorityQueueService(queueManager);
        this.patientService = new PatientService(queueManager);
        this.treatmentService = new TreatmentService(queueManager);
        this.bedService = new BedService();
        this.appointmentService = new AppointmentService();
        this.reportService = new ReportService();
        this.activityLogService = new ActivityLogService();
        this.authenticationService = new AuthenticationService();
        this.userDAO = new UserDAO();
    }

    private void initClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss");
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            lblLiveClock.setText(LocalDateTime.now().format(dtf));
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
        lblLiveClock.setText(LocalDateTime.now().format(dtf));
    }

    private void initNavButtons() {
        navButtons.add(btnNavDashboard);
        navButtons.add(btnNavQueue);
        navButtons.add(btnNavRegister);
        navButtons.add(btnNavPatients);
        navButtons.add(btnNavTreatment);
        navButtons.add(btnNavBeds);
        navButtons.add(btnNavAppointments);
        navButtons.add(btnNavReports);
        navButtons.add(btnNavLogs);
        navButtons.add(btnNavUsers);
        navButtons.add(btnNavRoleAuth);
        navButtons.add(btnNavSettings);
        navButtons.add(btnNavAbout);
    }

    private void updateDatabaseStatusLabel() {
        if (DatabaseConnection.isUsingFallback()) {
            lblDbStatus.setText("Demo In-Memory Mode");
            lblDbStatus.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-border-color: #FCD34D;");
        } else {
            lblDbStatus.setText("MySQL Connected");
            lblDbStatus.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-border-color: #BFDBFE;");
        }
    }

    public void setupUserSessionAndPermissions() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            lblUserFullName.setText("Guest User");
            lblUserRole.setText("GUEST");
            lblUsername.setText("");
            return;
        }

        lblUserFullName.setText(user.getFullName());
        lblUserRole.setText(user.getRole().getDisplayName().toUpperCase());
        lblUsername.setText("@" + user.getUsername());

        // Strict Role-Based Access Control (RBAC) visibility
        Role role = user.getRole();
        if (role == Role.ADMIN) {
            // Admin should NOT have access to: Register Patients, Start/Complete Treatment
            btnNavRegister.setVisible(false);
            btnNavRegister.setManaged(false);
            btnNavTreatment.setVisible(false);
            btnNavTreatment.setManaged(false);
        } else if (role == Role.DOCTOR) {
            // Doctor should NOT have access to: Manage Users, Admin Settings, Register Patients, Activity Logs, Reports
            btnNavRegister.setVisible(false);
            btnNavRegister.setManaged(false);
            btnNavReports.setVisible(false);
            btnNavReports.setManaged(false);
            btnNavLogs.setVisible(false);
            btnNavLogs.setManaged(false);
            btnNavUsers.setVisible(false);
            btnNavUsers.setManaged(false);
            btnNavSettings.setVisible(false);
            btnNavSettings.setManaged(false);
        } else if (role == Role.NURSE) {
            // Nurse should NOT have access to: Start/Complete Treatment, Manage Users, Admin Settings, Reports, Activity Logs, Consultations
            btnNavTreatment.setVisible(false);
            btnNavTreatment.setManaged(false);
            btnNavReports.setVisible(false);
            btnNavReports.setManaged(false);
            btnNavLogs.setVisible(false);
            btnNavLogs.setManaged(false);
            btnNavUsers.setVisible(false);
            btnNavUsers.setManaged(false);
            btnNavSettings.setVisible(false);
            btnNavSettings.setManaged(false);
            btnNavAppointments.setVisible(false);
            btnNavAppointments.setManaged(false);
        } else if (role == Role.PATIENT) {
            // Patient portal restricted view
            btnNavQueue.setVisible(false);
            btnNavQueue.setManaged(false);
            btnNavRegister.setVisible(false);
            btnNavRegister.setManaged(false);
            btnNavPatients.setVisible(false);
            btnNavPatients.setManaged(false);
            btnNavTreatment.setVisible(false);
            btnNavTreatment.setManaged(false);
            btnNavReports.setVisible(false);
            btnNavReports.setManaged(false);
            btnNavLogs.setVisible(false);
            btnNavLogs.setManaged(false);
            btnNavUsers.setVisible(false);
            btnNavUsers.setManaged(false);
            btnNavBeds.setVisible(false);
            btnNavBeds.setManaged(false);
            btnNavSettings.setVisible(false);
            btnNavSettings.setManaged(false);
        }
    }

    private void setActiveButton(Button activeBtn) {
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("sidebar-nav-btn-active");
            }
        }
        if (activeBtn != null && !activeBtn.getStyleClass().contains("sidebar-nav-btn-active")) {
            activeBtn.getStyleClass().add("sidebar-nav-btn-active");
        }
    }

    /**
     * Loads a dynamic view with strict backend permission verification.
     */
    public void loadView(String fxmlPath, String title, Button sourceBtn) {
        if (!isAuthorizedForView(fxmlPath)) {
            NotificationManager.showError("Access Denied", "You are not authorized to access: " + title);
            AlertHelper.showError("Access Denied", "Your account (" +
                    (SessionManager.getInstance().getCurrentUser() != null ?
                            SessionManager.getInstance().getCurrentUser().getRole().getDisplayName() : "Anonymous") +
                    ") does not have permission to access the [" + title + "] module.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setMainLayoutController(this);
                ((BaseController) controller).onAttached();
            }

            // Replace content while preserving notification overlay
            contentStackPane.getChildren().clear();
            contentStackPane.getChildren().add(view);
            contentStackPane.getChildren().add(notificationOverlay);

            lblHeaderTitle.setText(title);
            if (sourceBtn != null) {
                setActiveButton(sourceBtn);
            }
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError("Navigation Error", "Could not load view: " + fxmlPath + "\n" + e.getMessage());
        }
    }

    private boolean isAuthorizedForView(String fxmlPath) {
        SessionManager sm = SessionManager.getInstance();
        if (fxmlPath == null) return false;

        switch (fxmlPath) {
            case "/fxml/AdminDashboard.fxml":
                return sm.hasPermission(Permission.VIEW_ADMIN_DASHBOARD);
            case "/fxml/DoctorDashboard.fxml":
                return sm.hasPermission(Permission.VIEW_DOCTOR_DASHBOARD);
            case "/fxml/NurseDashboard.fxml":
                return sm.hasPermission(Permission.VIEW_NURSE_DASHBOARD);
            case "/fxml/PatientDashboard.fxml":
                return sm.hasPermission(Permission.VIEW_PATIENT_DASHBOARD);
            case "/fxml/LiveQueue.fxml":
                return sm.hasPermission(Permission.VIEW_EMERGENCY_QUEUE);
            case "/fxml/PatientRegistration.fxml":
                return sm.hasPermission(Permission.REGISTER_PATIENTS);
            case "/fxml/PatientManagement.fxml":
            case "/fxml/PatientDetail.fxml":
                return sm.hasPermission(Permission.VIEW_ALL_PATIENTS);
            case "/fxml/TreatmentManagement.fxml":
                return sm.hasPermission(Permission.START_TREATMENT);
            case "/fxml/BedManagement.fxml":
                return sm.hasPermission(Permission.VIEW_BEDS);
            case "/fxml/Appointments.fxml":
                return sm.hasPermission(Permission.MANAGE_APPOINTMENTS) || sm.hasPermission(Permission.BOOK_OWN_APPOINTMENT);
            case "/fxml/Reports.fxml":
                return sm.hasPermission(Permission.VIEW_REPORTS);
            case "/fxml/ActivityLogs.fxml":
                return sm.hasPermission(Permission.VIEW_ACTIVITY_LOGS);
            case "/fxml/UserManagement.fxml":
                return sm.hasPermission(Permission.MANAGE_USERS);
            case "/fxml/Settings.fxml":
                return sm.hasPermission(Permission.MANAGE_SETTINGS);
            case "/fxml/RoleAuthorization.fxml":
            case "/fxml/About.fxml":
                return true;
            default:
                return true;
        }
    }

    // --- Navigation Event Handlers ---

    @FXML
    public void handleNavDashboard() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null && user.getRole() == Role.DOCTOR) {
            loadView("/fxml/DoctorDashboard.fxml", "Doctor Clinical Dashboard", btnNavDashboard);
        } else if (user != null && user.getRole() == Role.NURSE) {
            loadView("/fxml/NurseDashboard.fxml", "Nurse Triage Dashboard", btnNavDashboard);
        } else if (user != null && user.getRole() == Role.PATIENT) {
            loadView("/fxml/PatientDashboard.fxml", "Patient Health Portal", btnNavDashboard);
        } else {
            loadView("/fxml/AdminDashboard.fxml", "Admin Oversight Dashboard", btnNavDashboard);
        }
    }

    @FXML
    public void handleNavQueue() {
        loadView("/fxml/LiveQueue.fxml", "Live Emergency Queue", btnNavQueue);
    }

    @FXML
    public void handleNavRegister() {
        loadView("/fxml/PatientRegistration.fxml", "Patient Emergency Registration", btnNavRegister);
    }

    @FXML
    public void handleNavPatients() {
        loadView("/fxml/PatientManagement.fxml", "Patient Directory & History", btnNavPatients);
    }

    @FXML
    public void handleNavTreatment() {
        loadView("/fxml/TreatmentManagement.fxml", "Emergency Treatment Station", btnNavTreatment);
    }

    @FXML
    public void handleNavBeds() {
        loadView("/fxml/BedManagement.fxml", "Hospital Bed Inventory & Ward Capacity", btnNavBeds);
    }

    @FXML
    public void handleNavAppointments() {
        loadView("/fxml/Appointments.fxml", "Scheduled Consultations & Appointments", btnNavAppointments);
    }

    @FXML
    public void handleNavReports() {
        loadView("/fxml/Reports.fxml", "Reports & Department Analytics", btnNavReports);
    }

    @FXML
    public void handleNavLogs() {
        loadView("/fxml/ActivityLogs.fxml", "System Activity & Audit Logs", btnNavLogs);
    }

    @FXML
    public void handleNavUsers() {
        loadView("/fxml/UserManagement.fxml", "Staff & User Account Management", btnNavUsers);
    }

    @FXML
    public void handleNavRoleAuth() {
        loadView("/fxml/RoleAuthorization.fxml", "Role-Based Access Control (RBAC) Matrix", btnNavRoleAuth);
    }

    @FXML
    public void handleNavSettings() {
        loadView("/fxml/Settings.fxml", "System Configuration & Thresholds", btnNavSettings);
    }

    @FXML
    public void handleNavAbout() {
        loadView("/fxml/About.fxml", "About System & Medical Disclaimer", btnNavAbout);
    }

    @FXML
    public void handleLogout() {
        boolean confirmed = AlertHelper.showConfirmation("Sign Out", "Are you sure you want to sign out?");
        if (!confirmed) return;

        if (clockTimeline != null) {
            clockTimeline.stop();
        }

        authenticationService.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root, 960, 620));
            stage.setTitle("HE-PPMS - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            AlertHelper.showError("Logout Error", "Failed to load login screen: " + e.getMessage());
        }
    }

    // Getters for shared services
    public PriorityQueueManager getQueueManager() { return queueManager; }
    public PriorityQueueService getQueueService() { return queueService; }
    public PatientService getPatientService() { return patientService; }
    public TreatmentService getTreatmentService() { return treatmentService; }
    public BedService getBedService() { return bedService; }
    public AppointmentService getAppointmentService() { return appointmentService; }
    public ReportService getReportService() { return reportService; }
    public ActivityLogService getActivityLogService() { return activityLogService; }
    public UserDAO getUserDAO() { return userDAO; }
}
