package com.hospital.controller;

import com.hospital.dao.UserDAO;
import com.hospital.model.*;
import com.hospital.security.PasswordUtil;
import com.hospital.security.SessionManager;
import com.hospital.util.AlertHelper;
import com.hospital.util.NotificationManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for Admin User Management.
 */
public class UserManagementController extends BaseController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Role> cmbRole;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;

    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colActive;
    @FXML private TableColumn<User, Void> colAction;

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void onAttached() {
        // Enforce Admin access
        SessionManager.getInstance().requireRole(Role.ADMIN);

        cmbRole.setItems(FXCollections.observableArrayList(Role.values()));
        cmbRole.getSelectionModel().select(Role.DOCTOR);

        initTable();
        loadUsers();
    }

    private void initTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole().getDisplayName()));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colActive.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Inactive"));

        colRole.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(role);
                    badge.getStyleClass().add("badge-status");
                    if ("Admin".equalsIgnoreCase(role)) {
                        badge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #B45309; -fx-font-weight: bold;");
                    } else if ("Doctor".equalsIgnoreCase(role)) {
                        badge.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-weight: bold;");
                    } else if ("Nurse".equalsIgnoreCase(role)) {
                        badge.setStyle("-fx-background-color: #ECFDF5; -fx-text-fill: #047857; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #F3E8FF; -fx-text-fill: #7E22CE; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnToggle = new Button("Toggle");
            {
                btnToggle.getStyleClass().add("btn-secondary");
                btnToggle.setStyle("-fx-font-size: 11px; -fx-padding: 3px 8px;");
                btnToggle.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    handleToggleUser(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User u = getTableView().getItems().get(getIndex());
                    btnToggle.setText(u.isActive() ? "Deactivate" : "Activate");
                    setGraphic(btnToggle);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    public void loadUsers() {
        List<User> list = userDAO.getAllUsers();
        tblUsers.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void handleCreateUser() {
        String fullName = txtFullName.getText() != null ? txtFullName.getText().trim() : "";
        String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String rawPassword = txtPassword.getText();
        Role role = cmbRole.getSelectionModel().getSelectedItem();
        String phone = txtPhone.getText() != null ? txtPhone.getText().trim() : "";
        String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";

        if (fullName.isEmpty()) {
            AlertHelper.showWarning("Missing Name", "Please provide the staff member's full name.");
            return;
        }
        if (username.isEmpty()) {
            AlertHelper.showWarning("Missing Username", "Please enter a unique username.");
            return;
        }
        if (rawPassword == null || rawPassword.length() < 4) {
            AlertHelper.showWarning("Weak Password", "Please provide a password of at least 4 characters.");
            return;
        }
        if (role == null) {
            AlertHelper.showWarning("Missing Role", "Please select a role.");
            return;
        }

        // Check if username already taken
        if (userDAO.findByUsername(username) != null) {
            AlertHelper.showError("Duplicate Username", "Username '" + username + "' is already registered.");
            return;
        }

        String passwordHash = PasswordUtil.hashPassword(rawPassword);

        User newUser;
        if (role == Role.DOCTOR) {
            newUser = new Doctor(0, username, passwordHash, fullName, phone, email, true, LocalDateTime.now(), "Emergency Physician", "Emergency Medicine");
        } else if (role == Role.NURSE) {
            newUser = new Nurse(0, username, passwordHash, fullName, phone, email, true, LocalDateTime.now(), "General Shift", "Triage Station 1");
        } else if (role == Role.PATIENT) {
            newUser = new PatientUser(0, username, passwordHash, fullName, phone, email, true, LocalDateTime.now(), "P" + String.format("%04d", (int)(Math.random() * 9000 + 1000)));
        } else {
            newUser = new Admin(0, username, passwordHash, fullName, phone, email, true, LocalDateTime.now());
        }

        boolean created = userDAO.createUser(newUser);
        if (created) {
            NotificationManager.showInfo("User Created", "Staff account for " + fullName + " created successfully.");
            handleClearForm();
            loadUsers();
        } else {
            AlertHelper.showError("Creation Failed", "Could not register new staff member in database.");
        }
    }

    private void handleToggleUser(User user) {
        if (user.getUsername().equalsIgnoreCase("admin")) {
            AlertHelper.showWarning("Protected Account", "The primary admin account cannot be deactivated.");
            return;
        }

        user.setActive(!user.isActive());
        userDAO.updateUser(user);
        NotificationManager.showInfo("Status Updated", "Account " + user.getUsername() + " set to " + (user.isActive() ? "Active" : "Inactive"));
        loadUsers();
    }

    @FXML
    public void handleClearForm() {
        txtFullName.clear();
        txtUsername.clear();
        txtPassword.clear();
        txtPhone.clear();
        txtEmail.clear();
        cmbRole.getSelectionModel().select(Role.DOCTOR);
    }

    @FXML
    public void handleRefresh() {
        loadUsers();
    }
}
