package com.hospital.dao;

import com.hospital.exception.DatabaseException;
import com.hospital.util.ConfigReader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Centralized Database Connection Manager.
 * Handles JDBC connectivity to MySQL with automatic graceful fallback to embedded mode
 * for zero-configuration testing and offline academic demonstration.
 */
public class DatabaseConnection {

    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String H2_FALLBACK_URL = "jdbc:h2:mem:hospital_emergency_system;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";

    private static boolean useFallback = false;
    private static boolean fallbackInitialized = false;

    static {
        initDriver();
    }

    private static void initDriver() {
        try {
            Class.forName(MYSQL_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found, falling back to H2: " + e.getMessage());
            useFallback = true;
        }

        try {
            Class.forName(H2_DRIVER);
        } catch (ClassNotFoundException ignored) {}
    }

    /**
     * Obtains a standard JDBC Connection.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (!useFallback) {
            try {
                String url = ConfigReader.getDbUrl();
                String user = ConfigReader.getDbUsername();
                String pass = ConfigReader.getDbPassword();
                DriverManager.setLoginTimeout(3); // 3 seconds timeout
                return DriverManager.getConnection(url, user, pass);
            } catch (SQLException e) {
                System.out.println("[DatabaseConnection] MySQL not available (" + e.getMessage() +
                        "). Activating seamless in-memory database fallback...");
                useFallback = true;
            }
        }

        // Connect using embedded fallback
        Connection conn = DriverManager.getConnection(H2_FALLBACK_URL, "sa", "");
        if (!fallbackInitialized) {
            initializeFallbackDatabase(conn);
            fallbackInitialized = true;
        }
        return conn;
    }

    /**
     * Initializes the embedded database using schema.sql and sample_data.sql.
     */
    private static void initializeFallbackDatabase(Connection conn) {
        System.out.println("[DatabaseConnection] Initializing fallback in-memory schema and sample data...");
        executeSqlScript(conn, "/schema.sql", false);
        executeSqlScript(conn, "/sample_data.sql", true);
        System.out.println("[DatabaseConnection] Fallback database initialized successfully.");
    }

    /**
     * Executes a SQL script file from the classpath.
     */
    public static void executeSqlScript(Connection conn, String resourcePath, boolean ignoreErrors) {
        try (InputStream in = DatabaseConnection.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                // Try reading from file system if not on classpath root
                FileScriptLoader.loadFromFileSystem(conn, resourcePath, ignoreErrors);
                return;
            }
            executeScriptStream(conn, in, ignoreErrors);
        } catch (Exception e) {
            if (!ignoreErrors) {
                System.err.println("[DatabaseConnection] Error executing script " + resourcePath + ": " + e.getMessage());
            }
        }
    }

    private static void executeScriptStream(Connection conn, InputStream in, boolean ignoreErrors) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("//") || line.startsWith("/*")) {
                    continue;
                }
                // Skip MySQL-specific database create/use statements in embedded mode
                if (line.toUpperCase().startsWith("CREATE DATABASE") ||
                    line.toUpperCase().startsWith("CHARACTER SET") ||
                    line.toUpperCase().startsWith("COLLATE") ||
                    line.toUpperCase().startsWith("USE ")) {
                    continue;
                }
                if (line.toUpperCase().startsWith("SET FOREIGN_KEY_CHECKS")) {
                    if (useFallback) {
                        if (line.contains("0")) {
                            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
                        } else {
                            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
                        }
                    }
                    continue;
                }
                if (useFallback && line.toUpperCase().startsWith("TRUNCATE TABLE ")) {
                    String table = line.substring(15).replace(";", "").trim();
                    stmt.execute("DELETE FROM " + table);
                    continue;
                }
                sb.append(" ").append(line);
                if (line.endsWith(";")) {
                    String sql = sb.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1);
                    }
                    if (useFallback) {
                        sql = sql.replaceAll("(?i)DATE_SUB\\s*\\(\\s*NOW\\(\\)\\s*,\\s*INTERVAL\\s+(\\d+)\\s+MINUTE\\s*\\)", "DATEADD('minute', -$1, CURRENT_TIMESTAMP)");
                        sql = sql.replaceAll("(?i)DATE_ADD\\s*\\(\\s*NOW\\(\\)\\s*,\\s*INTERVAL\\s+(\\d+)\\s+DAY\\s*\\)", "DATEADD('day', $1, CURRENT_TIMESTAMP)");
                    }
                    try {
                        if (!sql.isEmpty()) {
                            stmt.execute(sql);
                        }
                    } catch (SQLException ex) {
                        System.err.println("[SQL Warning] " + ex.getMessage() + " for query: " + (sql.length() > 100 ? sql.substring(0, 100) + "..." : sql));
                    }
                    sb.setLength(0);
                }
            }
        } catch (Exception e) {
            if (!ignoreErrors) {
                throw new DatabaseException("Failed executing database script: " + e.getMessage(), e);
            }
        }
    }

    public static boolean isUsingFallback() {
        return useFallback;
    }

    public static void setForceFallback(boolean force) {
        useFallback = force;
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    private static class FileScriptLoader {
        static void loadFromFileSystem(Connection conn, String resourcePath, boolean ignoreErrors) {
            java.io.File file = new java.io.File("database" + resourcePath);
            if (!file.exists()) {
                file = new java.io.File("." + resourcePath);
            }
            if (file.exists()) {
                try (InputStream fin = new java.io.FileInputStream(file)) {
                    executeScriptStream(conn, fin, ignoreErrors);
                } catch (Exception ignored) {}
            }
        }
    }
}
