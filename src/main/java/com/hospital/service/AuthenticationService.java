package com.hospital.service;

import com.hospital.dao.ActivityLogDAO;
import com.hospital.dao.UserDAO;
import com.hospital.exception.AuthenticationException;
import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.security.PasswordUtil;
import com.hospital.security.SessionManager;

/**
 * Service orchestrating user login, authentication verification, and session control.
 */
public class AuthenticationService {

    private final UserDAO userDAO;
    private final ActivityLogDAO activityLogDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    public AuthenticationService(UserDAO userDAO, ActivityLogDAO activityLogDAO) {
        this.userDAO = userDAO;
        this.activityLogDAO = activityLogDAO;
    }

    /**
     * Authenticates a user with username, password, and optional expected role.
     *
     * @param username The username entered
     * @param rawPassword The plain text password
     * @param expectedRole The selected role (optional; if provided, must match)
     * @return The authenticated User
     * @throws AuthenticationException if validation or credentials fail
     */
    public User login(String username, String rawPassword, Role expectedRole) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new AuthenticationException("Password cannot be empty.");
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            throw new AuthenticationException("Invalid username or password.");
        }

        if (!user.isActive()) {
            throw new AuthenticationException("Your account has been deactivated. Please contact the administrator.");
        }

        if (!PasswordUtil.checkPassword(rawPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password.");
        }

        if (expectedRole != null && user.getRole() != expectedRole) {
            throw new AuthenticationException("Access denied: You cannot log in with the selected role '" +
                    expectedRole.getDisplayName() + "'. Your account role is '" + user.getRole().getDisplayName() + "'.");
        }

        // Set authenticated user in session
        SessionManager.getInstance().setCurrentUser(user);

        // Record audit activity
        activityLogDAO.log(user.getUserId(), "LOGIN", "USER", user.getUserId(),
                "User " + user.getUsername() + " (" + user.getRole().getDisplayName() + ") logged in.");

        return user;
    }

    /**
     * Logs out current user and clears session.
     */
    public void logout() {
        User current = SessionManager.getInstance().getCurrentUser();
        if (current != null) {
            activityLogDAO.log(current.getUserId(), "LOGOUT", "USER", current.getUserId(),
                    "User " + current.getUsername() + " logged out.");
        }
        SessionManager.getInstance().clearSession();
    }
}
