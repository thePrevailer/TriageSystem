/*
 * Emergency Department Triage System
 * Department of Engineering Technology
 * Grambling State University, Grambling, LA 71245
 */

// Runs the 10 test cases. This is the file to run.
public class Main {

    public static void main(String[] args) {
        EmergencyDepartment ed = new EmergencyDepartment();

        // Test 1: admit four patients directly into beds.
        System.out.println("\n=== Test 1: Admit patients ===");
        ed.admitPatient(1, new Patient("P001", "John Smith", "Chest pain"));
        ed.admitPatient(2, new Patient("P002", "Maria Garcia", "Broken arm"));
        ed.admitPatient(3, new Patient("P003", "James Lee", "Stroke symptoms"));
        ed.admitPatient(10, new Patient("P006", "Lisa Anderson", "Flu"));

        // Test 2: show all beds (4 full, 11 empty).
        System.out.println("\n=== Test 2: Display room statuses ===");
        ed.displayRoomStatus();

        // Test 3: bed 1 is taken, so Kevin must be refused.
        System.out.println("\n=== Test 3: Try to admit to an occupied room (not allowed) ===");
        Patient kevin = new Patient("P007", "Kevin Brooks", "Ear infection");
        ed.admitPatient(1, kevin);

        // Test 4: fill the remaining beds. Once full, new arrivals wait in line.
        // Rita waits 4 hours and moves up a level; Peter (cardiac arrest) goes straight to the front.
        System.out.println("\n=== Test 4: Admit patients to empty beds until the ED is full ===");
        ed.admitPatient(4,  new Patient("P008", "Aisha Bello", "Difficulty breathing"));
        ed.admitPatient(5,  new Patient("P009", "Grace Kim", "Abdominal pain"));
        ed.admitPatient(6,  new Patient("P010", "Tom Reed", "Back pain"));
        ed.admitPatient(7,  new Patient("P011", "Samuel Ade", "Deep cut"));
        ed.admitPatient(8,  new Patient("P012", "Nora Hill", "High fever"));
        ed.admitPatient(9,  new Patient("P013", "Ken Watanabe", "Pneumonia"));
        ed.admitPatient(11, new Patient("P014", "Ada Okoro", "Seizure"));
        ed.admitPatient(12, kevin);
        ed.admitPatient(13, new Patient("P015", "Chloe Martin", "Kidney stones"));
        ed.admitPatient(14, new Patient("P016", "Femi Adeyemi", "Head injury"));
        ed.admitPatient(15, new Patient("P017", "Uche Eze", "Dehydration"));
        ed.displayRoomStatus();
        System.out.println("  All 15 beds are full. New arrivals must wait in the line:");
        ed.admitPatient(new Patient("P018", "Rita Cole", "Migraine"));
        ed.advanceTime(4);
        ed.admitPatient(new Patient("P019", "Zainab Musa", "Broken leg"));
        ed.admitPatient(new Patient("P020", "Peter Okafor", "Cardiac arrest"));
        ed.displayWaitingQueue();

        // Test 5: a null patient must be refused, both for a bed and for the line.
        System.out.println("\n=== Test 5: Try to admit a null patient (not allowed) ===");
        ed.admitPatient(5, null);
        ed.admitPatient(null);

        // Test 6: discharge level 4 patients. David's sprained ankle (level 5) gets him refused,
        // and the freed beds 10 and 12 go to the most severe waiting patients.
        System.out.println("\n=== Test 6: Discharge non-critical patients ===");
        ed.dischargeNonCriticalPatients();
        System.out.println("  David arrives. His condition is unknown until he is tested against our rules:");
        ed.admitPatient(4, new Patient("P005", "David Brown", "Sprained ankle"));
        System.out.println("  Freed beds go to the most severe waiting patients (bed 6 stays empty):");
        ed.admitNextPatientToBed(10);
        ed.admitNextPatientToBed(12);

        // Test 7: show the beds and the line after Test 6.
        System.out.println("\n=== Test 7: Display updated room statuses ===");
        ed.displayRoomStatus();
        ed.displayWaitingQueue();

        // Test 8: the doctor discharges the patients in beds 1 and 3.
        System.out.println("\n=== Test 8: Discharge cured patients on the doctor's advice ===");
        ed.dischargePatient(1);
        ed.dischargePatient(3);

        // Test 9: bed 6 is empty and bed 99 doesn't exist, so both must be refused.
        System.out.println("\n=== Test 9: Try to discharge from an empty room (not allowed) ===");
        ed.dischargePatient(6);
        ed.dischargePatient(99);

        // Test 10: peek at the next patient, search for patients in every state, and show final totals.
        System.out.println("\n=== Test 10: Final status - search, peek, queue and occupancy ===");
        Patient next = ed.peekNextPatient();
        System.out.println("  Next patient (peek): " + (next == null ? "nobody" : next));
        ed.findPatient("P020");   // in a bed
        ed.findPatient("P019");   // waiting
        ed.findPatient("P005");   // dismissed
        ed.findPatient("P001");   // discharged
        ed.findPatient("P999");   // doesn't exist
        ed.displayWaitingQueue();
        ed.displayRoomStatus();
        ed.displayStatistics();
    }
}
