package com.hospital.service;

import com.hospital.dao.PatientDAO;
import com.hospital.model.EmergencyLevel;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.model.QueueItem;

import java.util.List;

/**
 * High-level service managing priority queue synchronization with database.
 */
public class PriorityQueueService {

    private final PriorityQueueManager queueManager;
    private final PatientDAO patientDAO;

    public PriorityQueueService(PriorityQueueManager queueManager) {
        this.queueManager = queueManager;
        this.patientDAO = new PatientDAO();
        refreshFromDatabase();
    }

    public PriorityQueueManager getQueueManager() {
        return queueManager;
    }

    /**
     * Synchronizes live in-memory priority queue with the persistent database.
     */
    public synchronized void refreshFromDatabase() {
        List<Patient> activePatients = patientDAO.getActiveQueuePatients();
        queueManager.loadPatients(activePatients);
    }

    public List<QueueItem> getLiveQueue() {
        return queueManager.getAllInOrder();
    }

    public List<QueueItem> filterQueue(EmergencyLevel level, PatientStatus status, String search) {
        return queueManager.filter(level, status, search);
    }

    public QueueItem peekNext() {
        return queueManager.peek();
    }

    public QueueItem dequeueNext() {
        return queueManager.dequeue();
    }

    public void remove(int patientId) {
        queueManager.remove(patientId);
    }

    public int getQueueSize() {
        return queueManager.size();
    }

    public boolean isEmpty() {
        return queueManager.isEmpty();
    }

    public long getCriticalCount() {
        return queueManager.getCriticalCount();
    }

    public long getHighCount() {
        return queueManager.getHighCount();
    }

    public long getMediumCount() {
        return queueManager.getMediumCount();
    }

    public long getLowCount() {
        return queueManager.getLowCount();
    }

    public double getAverageWaitingTimeMinutes() {
        return queueManager.getAverageWaitingTimeMinutes();
    }

    public long getMaxWaitingTimeMinutes() {
        return queueManager.getMaxWaitingTimeMinutes();
    }

    public List<QueueItem> getPatientsExceedingThreshold(long thresholdMinutes) {
        return queueManager.getPatientsExceedingThreshold(thresholdMinutes);
    }
}
