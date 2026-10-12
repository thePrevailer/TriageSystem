/*
 * Emergency Department Triage System
 * Department of Engineering Technology
 * Grambling State University, Grambling, LA 71245
 */

import java.util.*;

// Runs the ED: the beds, the waiting line, and the record of every patient.
public class EmergencyDepartment {

    // 5 rooms x 3 beds = 15 beds, numbered 1 to 15.
    private static final int ROOMS = 5;
    private static final int BEDS_PER_ROOM = 3;
    private static final int TOTAL_BEDS = ROOMS * BEDS_PER_ROOM;

    private final TriageRules rules = new TriageRules();
    private final ArrayList<Patient> patientRecords = new ArrayList<>();      // everyone who has ever arrived
    private final HashMap<String, Integer> waitHours = new HashMap<>();      // patient ID -> hours waited
    private final PriorityQueue<Patient> waitingQueue =                      // most severe patient at the front
            new PriorityQueue<>(new TriageRules.PatientComparator());
    private final Patient[][] beds = new Patient[ROOMS][BEDS_PER_ROOM];      // null = empty bed

    private int nextArrivalNumber = 0;   // given to each new patient, used to break ties
    private int currentHour = 0;         // simulated clock

    // ---------- Triage ----------

    // First time we see a patient: give them a level and an arrival number, and save them in the records.
    private void triage(Patient p) {
        if (p.getStatus() != Patient.Status.NEW) return;   // already triaged
        p.setStartingLevel(rules.levelFor(p.getSymptom()));
        p.setArrivalNumber(nextArrivalNumber++);
        patientRecords.add(p);
    }

    // Level 5 patients are refused a bed. Returns true if the patient was sent home.
    private boolean dismissIfNonUrgent(Patient p) {
        if (p.getOriginalLevel() != TriageRules.DISMISS_LEVEL) return false;
        p.setStatus(Patient.Status.DISMISSED);
        System.out.printf("  [DISMISSED] %s - '%s' is %s. Refused a bed.%n",
                p.getName(), p.getSymptom(), TriageRules.levelName(p.getLevel()));
        return true;
    }

    // Returns true (and prints a message) if there is no patient.
    private boolean isNull(Patient p) {
        if (p != null) return false;
        System.out.println("  [REJECTED] Null patient. A patient must have an ID, name and symptom.");
        return true;
    }

    // ---------- Admitting ----------

    // Required: triage a patient and put them in the waiting line. O(log n)
    public boolean admitPatient(Patient p) {
        if (isNull(p)) return false;
        triage(p);
        if (dismissIfNonUrgent(p)) return false;
        p.setStatus(Patient.Status.WAITING);
        waitHours.put(p.getId(), 1);   // everyone starts with 1 hour of waiting
        waitingQueue.offer(p);
        System.out.printf("  [QUEUED]   %s added to the waiting line (%s)%n",
                p.getName(), TriageRules.levelName(p.getLevel()));
        return true;
    }

    // Put a patient in a specific bed. The triage check comes before the bed check,
    // because we only learn how sick someone is once they are tested.
    public boolean admitPatient(int bedNumber, Patient p) {
        if (isNull(p)) return false;
        if (!validBed(bedNumber)) {
            System.out.printf("  [REJECTED] Bed %d does not exist (beds 1-%d).%n", bedNumber, TOTAL_BEDS);
            return false;
        }
        triage(p);
        if (dismissIfNonUrgent(p)) return false;
        if (p.getStatus() == Patient.Status.IN_BED) {
            System.out.printf("  [REJECTED] %s is already in bed %d.%n", p.getName(), p.getBedNumber());
            return false;
        }
        Patient occupant = getBed(bedNumber);
        if (occupant != null) {
            System.out.printf("  [REJECTED] Bed %d is occupied by %s. Cannot admit %s.%n",
                    bedNumber, occupant.getName(), p.getName());
            return false;
        }
        waitHours.remove(p.getId());   // no longer waiting
        setBed(bedNumber, p);
        p.setBedNumber(bedNumber);
        p.setStatus(Patient.Status.IN_BED);
        System.out.printf("  [ADMITTED] %s -> %s (%s, %s)%n",
                p.getName(), bedLabel(bedNumber), p.getSymptom(), TriageRules.levelName(p.getLevel()));
        return true;
    }

    // Required: remove and return the most severe waiting patient. O(log n)
    public Patient getNextPatient() {
        Patient next = waitingQueue.poll();
        if (next != null) waitHours.remove(next.getId());
        return next;
    }

    // Required: see who is next without removing them. O(1)
    public Patient peekNextPatient() {
        return waitingQueue.peek();
    }

    // Give a free bed to whoever is next in line. The bed is checked first,
    // so a patient is never taken out of the line and then left without a bed.
    public boolean admitNextPatientToBed(int bedNumber) {
        if (!validBed(bedNumber) || getBed(bedNumber) != null) {
            System.out.printf("  [REJECTED] Bed %d is not available.%n", bedNumber);
            return false;
        }
        Patient next = getNextPatient();
        if (next == null) {
            System.out.println("  [INFO]     Nobody is waiting.");
            return false;
        }
        return admitPatient(bedNumber, next);
    }

    // ---------- Discharging ----------

    // The doctor sends a patient home. Refused if the bed doesn't exist or is already empty.
    public boolean dischargePatient(int bedNumber) {
        if (!validBed(bedNumber)) {
            System.out.printf("  [REJECTED] Bed %d does not exist.%n", bedNumber);
            return false;
        }
        Patient p = getBed(bedNumber);
        if (p == null) {
            System.out.printf("  [REJECTED] %s is already empty. Nobody to discharge.%n", bedLabel(bedNumber));
            return false;
        }
        setBed(bedNumber, null);
        p.setStatus(Patient.Status.DISCHARGED);
        System.out.printf("  [DISCHARGED] %s from %s%n", p.getName(), bedLabel(bedNumber));
        return true;
    }

    // Frees beds by discharging every level 4 patient.
    public void dischargeNonCriticalPatients() {
        for (int b = 1; b <= TOTAL_BEDS; b++) {
            Patient p = getBed(b);
            if (p != null && p.getLevel() >= 4) dischargePatient(b);
        }
    }

    // ---------- Waiting time ----------

    // Move the clock forward and update every waiting patient's wait time and level.
    // A heap can't re-sort itself when levels change, so we empty it, update everyone, and refill it.
    public void advanceTime(int hours) {
        currentHour += hours;
        System.out.printf("  [CLOCK]    %d hour(s) pass. Hour is now %d.%n", hours, currentHour);

        List<Patient> waiting = new ArrayList<>(waitingQueue);
        waitingQueue.clear();
        for (Patient p : waiting) {
            int waited = waitHours.get(p.getId()) + hours;
            waitHours.put(p.getId(), waited);
            int newLevel = TriageRules.agedLevel(p.getOriginalLevel(), waited);
            if (newLevel != p.getLevel()) {
                System.out.printf("  [AGING]    %s waited %d hrs: %s -> %s%n", p.getName(), waited,
                        TriageRules.levelName(p.getLevel()), TriageRules.levelName(newLevel));
                p.setLevel(newLevel);
            }
        }
        waitingQueue.addAll(waiting);
    }

    // ---------- Search and display ----------

    // Look up a patient by ID in the records and say where they are. O(n)
    public void findPatient(String id) {
        for (Patient p : patientRecords) {
            if (!p.getId().equals(id)) continue;
            String where;
            switch (p.getStatus()) {
                case IN_BED:     where = "in " + bedLabel(p.getBedNumber()); break;
                case WAITING:    where = "in the waiting line (waited " + waitHours.get(id) + " hrs)"; break;
                case DISMISSED:  where = "dismissed (non-urgent)"; break;
                case DISCHARGED: where = "discharged"; break;
                default:         where = "registered";
            }
            System.out.printf("  [FOUND]    %s is %s%n", p, where);
            return;
        }
        System.out.printf("  [NOT FOUND] No patient with ID %s%n", id);
    }

    // Print all 15 beds and how many are taken.
    public void displayRoomStatus() {
        System.out.println("  ---- Bed status (5 rooms x 3 beds) ----");
        for (int b = 1; b <= TOTAL_BEDS; b++) {
            Patient p = getBed(b);
            System.out.printf("  %-14s %s%n", bedLabel(b) + ":", p == null ? "EMPTY" : p);
        }
        System.out.printf("  Occupied: %d/%d beds%n", occupiedBeds(), TOTAL_BEDS);
    }

    // Print the waiting line in the order patients will be seen.
    // We empty a copy, so the real line is not changed.
    public void displayWaitingQueue() {
        PriorityQueue<Patient> copy = new PriorityQueue<>(waitingQueue);
        System.out.printf("  ---- Waiting line (%d) - served in this order ----%n", copy.size());
        int position = 1;
        while (!copy.isEmpty()) {
            Patient p = copy.poll();
            System.out.printf("  %d. %s | waited %d hr(s)%n", position++, p, waitHours.get(p.getId()));
        }
    }

    // Count patients in each status and print the totals.
    public void displayStatistics() {
        int inBed = 0, waiting = 0, discharged = 0, dismissed = 0;
        for (Patient p : patientRecords) {
            if (p.getStatus() == Patient.Status.IN_BED) inBed++;
            else if (p.getStatus() == Patient.Status.WAITING) waiting++;
            else if (p.getStatus() == Patient.Status.DISCHARGED) discharged++;
            else if (p.getStatus() == Patient.Status.DISMISSED) dismissed++;
        }
        System.out.println("  ---- Statistics ----");
        System.out.printf("  Total patients seen: %d | In beds: %d | Waiting: %d | Discharged: %d | Dismissed: %d%n",
                patientRecords.size(), inBed, waiting, discharged, dismissed);
        System.out.printf("  Free beds: %d/%d | Queue size: %d%n",
                TOTAL_BEDS - occupiedBeds(), TOTAL_BEDS, waitingQueue.size());
    }

    // ---------- Bed helpers ----------
    // Bed b is stored at beds[(b-1)/3][(b-1)%3]. For example, bed 10 is room 4, position 1.

    private boolean validBed(int b) { return b >= 1 && b <= TOTAL_BEDS; }
    private Patient getBed(int b) { return beds[(b - 1) / BEDS_PER_ROOM][(b - 1) % BEDS_PER_ROOM]; }
    private void setBed(int b, Patient p) { beds[(b - 1) / BEDS_PER_ROOM][(b - 1) % BEDS_PER_ROOM] = p; }
    private String bedLabel(int b) { return "Room " + ((b - 1) / BEDS_PER_ROOM + 1) + " Bed " + b; }

    private int occupiedBeds() {
        int count = 0;
        for (int b = 1; b <= TOTAL_BEDS; b++) if (getBed(b) != null) count++;
        return count;
    }
}
