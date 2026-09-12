package com.hospital.service;

import com.hospital.exception.QueueException;
import com.hospital.model.EmergencyLevel;
import com.hospital.model.Patient;
import com.hospital.model.PatientStatus;
import com.hospital.model.QueueItem;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Dedicated Priority Queue Manager implementing hospital emergency triage queue logic.
 *
 * Queue Ordering Rules:
 * 1. Primary: Lower priority number comes first (1=CRITICAL, 2=HIGH, 3=MEDIUM, 4=LOW).
 * 2. Secondary: Earlier arrival time comes first within the same priority level.
 * 3. Tie-breaker: Deterministic by patient ID.
 *
 * Only active waiting patients (WAITING or CALLED) are maintained in the live queue.
 */
public class PriorityQueueManager {

    private final List<QueueItem> queueItems = new ArrayList<>();
    private final Object lock = new Object();

    public PriorityQueueManager() {}

    /**
     * Enqueues or updates an active emergency patient.
     */
    public void enqueue(Patient patient) {
        if (patient == null) {
            throw new QueueException("Cannot enqueue null patient");
        }

        // Only active patients can remain in the waiting queue
        if (!patient.getCurrentStatus().isActiveInQueue()) {
            remove(patient.getPatientId());
            return;
        }

        synchronized (lock) {
            // Check if patient is already present in queue
            boolean found = false;
            for (int i = 0; i < queueItems.size(); i++) {
                if (queueItems.get(i).getPatientId() == patient.getPatientId()) {
                    queueItems.set(i, new QueueItem(patient));
                    found = true;
                    break;
                }
            }

            if (!found) {
                queueItems.add(new QueueItem(patient));
            }

            reorderAndAssignPositions();
        }
    }

    /**
     * Retrieves and removes the highest priority patient from the front of the queue.
     *
     * @return QueueItem of the highest priority patient, or null if queue is empty.
     */
    public QueueItem dequeue() {
        synchronized (lock) {
            if (queueItems.isEmpty()) {
                return null;
            }
            QueueItem top = queueItems.remove(0);
            reorderAndAssignPositions();
            return top;
        }
    }

    /**
     * Peeks at the highest priority patient currently at the front of the queue without removing.
     *
     * @return QueueItem at position #1, or null if queue is empty.
     */
    public QueueItem peek() {
        synchronized (lock) {
            if (queueItems.isEmpty()) {
                return null;
            }
            return queueItems.get(0);
        }
    }

    /**
     * Removes a patient from the queue by their unique patient ID.
     */
    public boolean remove(int patientId) {
        synchronized (lock) {
            boolean removed = queueItems.removeIf(item -> item.getPatientId() == patientId);
            if (removed) {
                reorderAndAssignPositions();
            }
            return removed;
        }
    }

    /**
     * Bulk loads patients from database into the priority queue.
     */
    public void loadPatients(Collection<Patient> patients) {
        synchronized (lock) {
            queueItems.clear();
            if (patients != null) {
                for (Patient p : patients) {
                    if (p.getCurrentStatus() != null && p.getCurrentStatus().isActiveInQueue()) {
                        queueItems.add(new QueueItem(p));
                    }
                }
            }
            reorderAndAssignPositions();
        }
    }

    /**
     * Sorts the queue according to priority rules and assigns 1-indexed queue positions.
     */
    private void reorderAndAssignPositions() {
        Collections.sort(queueItems);
        for (int i = 0; i < queueItems.size(); i++) {
            queueItems.get(i).setQueuePosition(i + 1);
        }
    }

    /**
     * Returns a snapshot copy of all items in current queue order.
     */
    public List<QueueItem> getAllInOrder() {
        synchronized (lock) {
            return new ArrayList<>(queueItems);
        }
    }

    /**
     * Filters the current queue by optional emergency level, status, and search query.
     */
    public List<QueueItem> filter(EmergencyLevel level, PatientStatus status, String query) {
        synchronized (lock) {
            return queueItems.stream()
                    .filter(item -> level == null || item.getEmergencyLevel() == level)
                    .filter(item -> status == null || item.getStatus() == status)
                    .filter(item -> {
                        if (query == null || query.trim().isEmpty()) return true;
                        String q = query.trim().toLowerCase();
                        return (item.getPatientCode() != null && item.getPatientCode().toLowerCase().contains(q)) ||
                               (item.getTokenNumber() != null && item.getTokenNumber().toLowerCase().contains(q)) ||
                               (item.getPatientName() != null && item.getPatientName().toLowerCase().contains(q)) ||
                               (item.getSymptoms() != null && item.getSymptoms().toLowerCase().contains(q));
                    })
                    .collect(Collectors.toList());
        }
    }

    public int size() {
        synchronized (lock) {
            return queueItems.size();
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return queueItems.isEmpty();
        }
    }

    public void clear() {
        synchronized (lock) {
            queueItems.clear();
        }
    }

    // --- Analytics & Statistics ---

    public long getCriticalCount() {
        synchronized (lock) {
            return queueItems.stream().filter(i -> i.getEmergencyLevel() == EmergencyLevel.CRITICAL).count();
        }
    }

    public long getHighCount() {
        synchronized (lock) {
            return queueItems.stream().filter(i -> i.getEmergencyLevel() == EmergencyLevel.HIGH).count();
        }
    }

    public long getMediumCount() {
        synchronized (lock) {
            return queueItems.stream().filter(i -> i.getEmergencyLevel() == EmergencyLevel.MEDIUM).count();
        }
    }

    public long getLowCount() {
        synchronized (lock) {
            return queueItems.stream().filter(i -> i.getEmergencyLevel() == EmergencyLevel.LOW).count();
        }
    }

    public double getAverageWaitingTimeMinutes() {
        synchronized (lock) {
            if (queueItems.isEmpty()) return 0.0;
            return queueItems.stream()
                    .mapToLong(QueueItem::getWaitingTimeMinutes)
                    .average()
                    .orElse(0.0);
        }
    }

    public long getMaxWaitingTimeMinutes() {
        synchronized (lock) {
            return queueItems.stream()
                    .mapToLong(QueueItem::getWaitingTimeMinutes)
                    .max()
                    .orElse(0L);
        }
    }

    public double getAverageWaitingTimeByLevel(EmergencyLevel level) {
        if (level == null) return 0.0;
        synchronized (lock) {
            return queueItems.stream()
                    .filter(i -> i.getEmergencyLevel() == level)
                    .mapToLong(QueueItem::getWaitingTimeMinutes)
                    .average()
                    .orElse(0.0);
        }
    }

    public List<QueueItem> getPatientsExceedingThreshold(long thresholdMinutes) {
        synchronized (lock) {
            return queueItems.stream()
                    .filter(i -> i.getWaitingTimeMinutes() >= thresholdMinutes)
                    .collect(Collectors.toList());
        }
    }
}
