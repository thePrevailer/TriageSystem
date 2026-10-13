/*
 * Emergency Department Triage System
 * Department of Engineering Technology
 * Grambling State University, Grambling, LA 71245
 */

// One patient: who they are, how severe their problem is, and where they are in the ED.
public class Patient {

    // Where a patient is right now. NEW means they just walked in and haven't been triaged.
    public enum Status { NEW, WAITING, IN_BED, DISCHARGED, DISMISSED }

    // Known when the patient walks in. These never change, so they are final.
    private final String id;
    private final String name;
    private final String symptom;

    // Filled in by the ED after the patient arrives.
    private int originalLevel;   // severity given at triage (1 = most severe, 5 = least)
    private int level;           // current severity; moves toward 1 the longer they wait
    private int arrivalNumber;   // 0 for the first patient, 1 for the second, and so on
    private int bedNumber;       // which bed they are in (0 = no bed)
    private Status status = Status.NEW;

    public Patient(String id, String name, String symptom) {
        if (id == null || id.trim().isEmpty()
                || name == null || name.trim().isEmpty()
                || symptom == null || symptom.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID, name and symptom are required.");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.symptom = symptom.trim();
    }

    // Getters: let the other files read this patient's information.
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSymptom() { return symptom; }
    public int getOriginalLevel() { return originalLevel; }
    public int getLevel() { return level; }
    public int getArrivalNumber() { return arrivalNumber; }
    public int getBedNumber() { return bedNumber; }
    public Status getStatus() { return status; }

    // Setters: let the ED update this patient. At triage, both levels start the same.
    public void setStartingLevel(int l) { originalLevel = l; level = l; }
    public void setLevel(int l) { level = l; }
    public void setArrivalNumber(int n) { arrivalNumber = n; }
    public void setBedNumber(int b) { bedNumber = b; }
    public void setStatus(Status s) { status = s; }

    // How a patient prints, e.g. "P001 John Smith | Level 2 | Chest pain".
    // %-14s pads the name to 14 characters so the columns line up.
    @Override
    public String toString() {
        return String.format("%s %-14s | Level %d | %s", id, name, level, symptom);
    }
}
