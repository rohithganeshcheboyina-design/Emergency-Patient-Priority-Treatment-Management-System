package com.hospital.service;

import com.hospital.dao.ActivityLogDAO;
import com.hospital.dao.BedDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.exception.InvalidPatientDataException;
import com.hospital.model.Bed;
import com.hospital.model.BedStatus;
import com.hospital.model.Patient;
import com.hospital.model.Permission;
import com.hospital.model.User;
import com.hospital.security.SessionManager;

import java.util.List;
import java.util.Map;

/**
 * Service orchestrating bed allocations, emergency bay management, and capacity tracking.
 */
public class BedService {

    private final BedDAO bedDAO;
    private final PatientDAO patientDAO;
    private final ActivityLogDAO activityLogDAO;

    public BedService() {
        this.bedDAO = new BedDAO();
        this.patientDAO = new PatientDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    public BedService(BedDAO bedDAO, PatientDAO patientDAO, ActivityLogDAO activityLogDAO) {
        this.bedDAO = bedDAO;
        this.patientDAO = patientDAO;
        this.activityLogDAO = activityLogDAO;
    }

    public List<Bed> getAllBeds() {
        SessionManager.getInstance().requirePermission(Permission.VIEW_BEDS);
        return bedDAO.getAllBeds();
    }

    public List<Bed> getAvailableBeds() {
        SessionManager.getInstance().requirePermission(Permission.VIEW_BEDS);
        return bedDAO.getAvailableBeds();
    }

    public boolean assignBed(int bedId, int patientId, String notes) throws InvalidPatientDataException {
        SessionManager.getInstance().requirePermission(Permission.ASSIGN_BEDS);

        Bed bed = bedDAO.findById(bedId);
        if (bed == null) {
            throw new InvalidPatientDataException("Bed not found (ID: " + bedId + ").");
        }
        if (bed.getStatus() == BedStatus.OCCUPIED) {
            throw new InvalidPatientDataException("Bed " + bed.getBedNumber() + " is already occupied.");
        }

        Patient patient = patientDAO.findById(patientId);
        if (patient == null) {
            throw new InvalidPatientDataException("Patient not found (ID: " + patientId + ").");
        }

        boolean success = bedDAO.assignBed(bedId, patientId, notes);
        if (success) {
            User staff = SessionManager.getInstance().getCurrentUser();
            int staffId = staff != null ? staff.getUserId() : 1;
            activityLogDAO.log(staffId, "BED_ASSIGNED", "BED", bedId,
                    "Assigned " + bed.getBedNumber() + " to patient " + patient.getFullName() + " (" + patient.getPatientCode() + ").");
        }
        return success;
    }

    public boolean releaseBed(int bedId) {
        SessionManager.getInstance().requirePermission(Permission.ASSIGN_BEDS);

        Bed bed = bedDAO.findById(bedId);
        boolean success = bedDAO.releaseBed(bedId);
        if (success && bed != null) {
            User staff = SessionManager.getInstance().getCurrentUser();
            int staffId = staff != null ? staff.getUserId() : 1;
            activityLogDAO.log(staffId, "BED_RELEASED", "BED", bedId,
                    "Released bed " + bed.getBedNumber() + " back to AVAILABLE pool.");
        }
        return success;
    }

    public boolean createBed(Bed bed) {
        SessionManager.getInstance().requirePermission(Permission.MANAGE_BEDS);
        return bedDAO.createBed(bed);
    }

    public Map<String, Integer> getBedStatistics() {
        SessionManager.getInstance().requirePermission(Permission.VIEW_BEDS);
        return bedDAO.getBedStatistics();
    }
}
