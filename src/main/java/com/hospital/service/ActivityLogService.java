package com.hospital.service;

import com.hospital.dao.ActivityLogDAO;
import com.hospital.model.ActivityLog;

import java.util.List;

/**
 * Service managing audit trails and activity logs.
 */
public class ActivityLogService {

    private final ActivityLogDAO activityLogDAO;

    public ActivityLogService() {
        this.activityLogDAO = new ActivityLogDAO();
    }

    public ActivityLogService(ActivityLogDAO activityLogDAO) {
        this.activityLogDAO = activityLogDAO;
    }

    public List<ActivityLog> getRecentLogs(int limit) {
        return activityLogDAO.getRecentLogs(limit);
    }

    public List<ActivityLog> getAllLogs() {
        return activityLogDAO.getAllLogs();
    }

    public void log(Integer userId, String action, String entityType, Integer entityId, String description) {
        activityLogDAO.log(userId, action, entityType, entityId, description);
    }
}
