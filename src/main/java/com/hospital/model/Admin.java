package com.hospital.model;

import java.time.LocalDateTime;

/**
 * System Administrator with full administrative access.
 */
public class Admin extends User {

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(int userId, String username, String passwordHash, String fullName,
                 String phone, String email, boolean active, LocalDateTime createdAt) {
        super(userId, username, passwordHash, fullName, Role.ADMIN, phone, email, active, createdAt);
    }
}
