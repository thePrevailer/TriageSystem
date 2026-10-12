/*
 * Emergency Department Triage System
 * Department of Engineering Technology
 * Grambling State University, Grambling, LA 71245
 */

import java.util.*;

// The medical rules: how severe each illness is, how waiting raises priority, and who goes first.
public class TriageRules {

    public static final int DISMISS_LEVEL = 5;        // level 5 patients are sent home, not given a bed
    public static final int UNKNOWN_LEVEL = 3;        // unrecognized symptoms get level 3 so a nurse checks them
    public static final int HOURS_PER_PROMOTION = 4;  // every 4 hours of waiting moves a patient up one level

    // Index = level, so LEVEL_NAMES[1] is "Critical". Index 0 is unused.
    private static final String[] LEVEL_NAMES =
            {"", "Critical", "Emergent", "Urgent", "Less urgent", "Non-urgent"};

    // HashMap 1 and HashMap 2: level -> list of illnesses or symptoms at that level.
    private final HashMap<Integer, List<String>> knownConditions = new HashMap<>();
    private final HashMap<Integer, List<String>> knownSymptoms = new HashMap<>();

    // Reverse of the two maps above (illness -> level), so a lookup is O(1) instead of searching every list.
    private final HashMap<String, Integer> illnessToLevel = new HashMap<>();

    public TriageRules() {
        // HashMap 1: known conditions, from most severe (1) to least severe (5).
        knownConditions.put(1, Arrays.asList("cardiac arrest", "heart attack", "stroke", "stroke symptoms",
                "anaphylaxis", "severe trauma", "major bleeding", "not breathing", "unresponsive"));
        knownConditions.put(2, Arrays.asList("chest pain", "difficulty breathing", "seizure", "overdose",
                "head injury", "severe burns", "sepsis", "severe asthma attack"));
        knownConditions.put(3, Arrays.asList("broken arm", "broken leg", "deep cut", "abdominal pain",
                "high fever", "dehydration", "kidney stones", "pneumonia", "dislocated shoulder"));
        knownConditions.put(4, Arrays.asList("flu", "migraine", "ear infection", "urinary tract infection",
                "back pain", "minor burn", "vomiting", "minor fracture"));
        knownConditions.put(5, Arrays.asList("sprained ankle", "common cold", "sore throat", "minor rash",
                "insect bite", "prescription refill", "mild headache", "splinter"));

        // HashMap 2: symptoms, for when the patient describes how they feel instead of a condition.
        knownSymptoms.put(1, Arrays.asList("no pulse", "unconscious", "severe bleeding", "blue lips"));
        knownSymptoms.put(2, Arrays.asList("shortness of breath", "confusion", "slurred speech",
                "coughing blood", "severe pain"));
        knownSymptoms.put(3, Arrays.asList("persistent vomiting", "fever over 103", "moderate pain",
                "blood in urine"));
        knownSymptoms.put(4, Arrays.asList("mild fever", "earache", "painful urination", "nausea"));
        knownSymptoms.put(5, Arrays.asList("no pain", "mild cough", "runny nose", "itching", "minor bruise"));

        // Fill the reverse map. Conditions go in last, so they win if a word is in both maps.
        for (int level : knownSymptoms.keySet())
            for (String symptom : knownSymptoms.get(level)) illnessToLevel.put(symptom, level);
        for (int level : knownConditions.keySet())
            for (String condition : knownConditions.get(level)) illnessToLevel.put(condition, level);
    }

    // Returns the severity level for a symptom. "Flu " and "flu" match because we trim and lowercase.
    public int levelFor(String symptom) {
        if (symptom == null) return UNKNOWN_LEVEL;
        return illnessToLevel.getOrDefault(symptom.trim().toLowerCase(), UNKNOWN_LEVEL);
    }

    // Aging: everyone starts at 1 hour, and each full 4 hours after that moves them up one level (never past 1).
    public static int agedLevel(int originalLevel, int hoursWaited) {
        int promotions = (hoursWaited - 1) / HOURS_PER_PROMOTION;
        return Math.max(1, originalLevel - promotions);
    }

    // Turns 1 into "Level 1 Critical" for printing.
    public static String levelName(int level) {
        return "Level " + level + " " + LEVEL_NAMES[level];
    }

    // Tells the priority queue who goes first: the lower level first,
    // and if two patients have the same level, whoever arrived first (FIFO within priority).
    public static class PatientComparator implements Comparator<Patient> {
        @Override
        public int compare(Patient a, Patient b) {
            if (a.getLevel() != b.getLevel()) return Integer.compare(a.getLevel(), b.getLevel());
            return Integer.compare(a.getArrivalNumber(), b.getArrivalNumber());
        }
    }
}
