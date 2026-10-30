# System Design & Triage Algorithm Specification

This document details the internal design of the priority queue triage engine, state machines, and concurrency controls of the **Hospital Emergency Patient Priority Treatment Management System**.

---

## 1. Emergency Severity Index (ESI) Triage Engine

The system uses a 4-tier triage priority model matching international emergency medicine standards:

```text
+-----------------------+---------------+-------------------------+----------------------+
| Priority Level        | Color Code    | Clinical Classification | Target Response Time |
+-----------------------+---------------+-------------------------+----------------------+
| 1 - RESUSCITATION     | RED           | Immediate Life-Threat   | 0 - 15 minutes       |
| 2 - EMERGENT          | ORANGE        | High Risk / Severe Pain | < 30 minutes         |
| 3 - URGENT            | YELLOW        | Moderate / Multi-System | < 60 minutes         |
| 4 - NON-URGENT        | GREEN         | Minor Injury / Stable   | < 120 minutes        |
+-----------------------+---------------+-------------------------+----------------------+
```

### Dynamic Wait-Time Aging
To prevent starvation of lower-priority patients during sustained ED influx:
- Each patient's wait duration is continuously calculated: `elapsed = now - arrivalTimestamp`.
- If `elapsed > threshold[priority]`, an escalation flag is activated.
- Medical staff receive a visual indicator and prompt for clinical re-assessment.
- Automated triage assists staff, but **qualified clinical personnel retain authoritative override control** at all times.

---

## 2. Queue Comparator Algorithm

The `PriorityQueueManager` maintains an in-memory binary min-heap sorted using a deterministic 2-factor comparator:

```java
Comparator<QueueItem> comparator = (a, b) -> {
    int pCompare = Integer.compare(a.getPriority(), b.getPriority());
    if (pCompare != 0) {
        return pCompare; // Priority 1 before Priority 2, etc.
    }
    // FIFO tie-breaker for identical priority tiers
    return a.getArrivalTime().compareTo(b.getArrivalTime());
};
```

---

## 3. Patient Lifecycle Finite State Machine (FSM)

```text
[REGISTERED]
     │
     ▼
 [WAITING] ────────► [TRIAGE RE-ASSESSMENT]
     │                      │
     ▼                      │
  [CALLED] ◄────────────────┘
     │
     ▼
[IN_TREATMENT] (Bed Assigned)
     │
     ▼
 [TREATED] (Bed Cleared)
     │
     ▼
[DISCHARGED]
```

Every state transition is audited in `patient_status_history` with actor identity, timestamp, and previous state.
