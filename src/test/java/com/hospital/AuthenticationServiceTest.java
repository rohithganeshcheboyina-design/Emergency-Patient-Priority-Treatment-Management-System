package com.hospital;

import com.hospital.model.Role;
import com.hospital.model.User;
import com.hospital.security.PasswordUtil;
import com.hospital.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationServiceTest {

    @Test
    @DisplayName("Should correctly hash and verify passwords using BCrypt")
    public void testPasswordHashing() {
        String raw = "admin123";
        String hash = PasswordUtil.hashPassword(raw);
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));
        assertTrue(PasswordUtil.checkPassword(raw, hash));
        assertFalse(PasswordUtil.checkPassword("wrongpass", hash));
    }

    @Test
    @DisplayName("Should authenticate demo admin account and enforce correct role")
    public void testDemoAdminLogin() throws Exception {
        AuthenticationService auth = new AuthenticationService();
        User admin = auth.login("admin", "admin123", Role.ADMIN);
        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());
        assertEquals(Role.ADMIN, admin.getRole());

        // Test wrong password
        assertThrows(Exception.class, () -> auth.login("admin", "wrongpassword", Role.ADMIN));

        // Test wrong role selection
        assertThrows(Exception.class, () -> auth.login("admin", "admin123", Role.DOCTOR));

        // Test logout
        auth.logout();
        assertNull(com.hospital.security.SessionManager.getInstance().getCurrentUser());
    }

    @Test
    @DisplayName("Should reject blank username or password")
    public void testBlankCredentials() {
        AuthenticationService auth = new AuthenticationService();
        assertThrows(Exception.class, () -> auth.login("", "admin123", Role.ADMIN));
        assertThrows(Exception.class, () -> auth.login("admin", "", Role.ADMIN));
    }
}
