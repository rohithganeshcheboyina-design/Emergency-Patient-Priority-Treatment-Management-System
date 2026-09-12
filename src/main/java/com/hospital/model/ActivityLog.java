package com.hospital.model;

import java.time.LocalDateTime;

/**
 * System activity log recording all critical operational events.
 */
public class ActivityLog {
    private int logId;
    private Integer userId;
    private String username;
    private String action;
    private String entityType;
    private Integer entityId;
    private LocalDateTime timestamp;
    private String description;

    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ActivityLog(int logId, Integer userId, String username, String action,
                       String entityType, Integer entityId, LocalDateTime timestamp, String description) {
        this.logId = logId;
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.description = description;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
