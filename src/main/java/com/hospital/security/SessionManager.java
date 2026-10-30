package com.hospital.security;

import com.hospital.exception.UnauthorizedException;
import com.hospital.model.Permission;
import com.hospital.model.Role;
import com.hospital.model.User;

/**
 * Manages the current logged-in user session and strict Role-Based Access Control (RBAC).
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized void clearSession() {
        this.currentUser = null;
    }

    public synchronized boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public synchronized void requireRole(Role role) {
        if (!hasRole(role)) {
            String roleName = role != null ? role.getDisplayName() : "Authorized";
            throw new UnauthorizedException("Access Denied: Requires " + roleName + " privilege.");
        }
    }

    public synchronized void requireAnyRole(Role... roles) {
        if (currentUser == null) {
            throw new UnauthorizedException("Access Denied: User is not authenticated.");
        }
        for (Role r : roles) {
            if (currentUser.getRole() == r) {
                return;
            }
        }
        throw new UnauthorizedException("Access Denied: You do not have permission to access this module.");
    }

    public synchronized boolean hasPermission(Permission permission) {
        if (currentUser == null || currentUser.getRole() == null || permission == null) {
            return false;
        }
        return currentUser.getRole().hasPermission(permission);
    }

    public synchronized void requirePermission(Permission permission) {
        if (currentUser == null) {
            throw new UnauthorizedException("Access Denied: User is not authenticated.");
        }
        if (!hasPermission(permission)) {
            throw new UnauthorizedException("Access Denied: Missing required permission [" +
                    (permission != null ? permission.getTitle() : "Unknown") + "].");
        }
    }
}
