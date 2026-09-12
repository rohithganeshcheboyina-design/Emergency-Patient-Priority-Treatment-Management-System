package com.hospital.controller;

import com.hospital.model.EmergencyLevel;
import com.hospital.model.PatientStatus;
import com.hospital.security.SessionManager;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base Controller providing common navigation, formatting, and session utilities.
 */
public abstract class BaseController {

    protected MainLayoutController mainLayoutController;
    protected static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    protected static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setMainLayoutController(MainLayoutController mainLayoutController) {
        this.mainLayoutController = mainLayoutController;
    }

    public abstract void onAttached();

    protected String formatDateTime(LocalDateTime ldt) {
        if (ldt == null) return "N/A";
        return ldt.format(DATE_TIME_FMT);
    }

    protected String formatTime(LocalDateTime ldt) {
        if (ldt == null) return "N/A";
        return ldt.format(TIME_FMT);
    }

    protected void applyPriorityBadge(Label label, EmergencyLevel level) {
        if (label == null) return;
        label.getStyleClass().removeAll("badge-critical", "badge-high", "badge-medium", "badge-low");
        if (level == null) return;

        label.setText(level.getDisplayName() + " (P" + level.getPriority() + ")");
        switch (level) {
            case CRITICAL:
                label.getStyleClass().add("badge-critical");
                break;
            case HIGH:
                label.getStyleClass().add("badge-high");
                break;
            case MEDIUM:
                label.getStyleClass().add("badge-medium");
                break;
            case LOW:
                label.getStyleClass().add("badge-low");
                break;
        }
    }

    protected void applyStatusBadge(Label label, PatientStatus status) {
        if (label == null || status == null) return;
        label.setText(status.getDisplayName());
        label.getStyleClass().add("badge-status");
    }
}
