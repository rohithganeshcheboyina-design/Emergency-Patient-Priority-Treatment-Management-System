package com.hospital.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads system and database properties from config.properties.
 */
public class ConfigReader {
    private static final Properties properties = new Properties();
    private static boolean loaded = false;

    static {
        loadProperties();
    }

    private static synchronized void loadProperties() {
        if (loaded) return;

        // Try external config.properties (in config/ or current working directory)
        File[] candidateFiles = new File[] {
            new File("config/config.properties"),
            new File("config.properties")
        };
        for (File externalFile : candidateFiles) {
            if (externalFile.exists()) {
                try (InputStream in = new FileInputStream(externalFile)) {
                    properties.load(in);
                    loaded = true;
                    return;
                } catch (Exception ignored) {}
            }
        }

        // Fallback to classpath resource
        try (InputStream in = ConfigReader.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                properties.load(in);
                loaded = true;
            }
        } catch (Exception e) {
            System.err.println("Warning: Unable to load config.properties, using built-in defaults: " + e.getMessage());
        }
    }

    public static String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        String val = properties.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getDbUrl() {
        return get("db.url", "jdbc:mysql://localhost:3306/hospital_emergency_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public static String getDbUsername() {
        return get("db.username", "root");
    }

    public static String getDbPassword() {
        return get("db.password", "");
    }

    public static int getCriticalWaitThreshold() {
        return getInt("app.emergency.critical.wait_threshold_min", 15);
    }

    public static int getHighWaitThreshold() {
        return getInt("app.emergency.high.wait_threshold_min", 30);
    }

    public static int getMediumWaitThreshold() {
        return getInt("app.emergency.medium.wait_threshold_min", 60);
    }

    public static int getLowWaitThreshold() {
        return getInt("app.emergency.low.wait_threshold_min", 120);
    }

    public static int getAutoRefreshSeconds() {
        return getInt("app.queue.auto_refresh_seconds", 10);
    }
}
