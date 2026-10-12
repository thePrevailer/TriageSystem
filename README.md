# Emergency Department Triage System

Department of Engineering Technology, Grambling State University

A Java program that runs a small hospital emergency room: it decides how sick each patient is,
gives beds to the sickest patients first, and keeps everyone else in a fair waiting line.

## What is this?

In a real emergency room, patients are not seen in the order they arrive. A person having a
heart attack goes ahead of someone with the flu, even if they walked in an hour later.
This is called **triage**.

This program does the same thing. When a patient arrives, it:

1. **Checks how sick they are** and gives them a level from 1 (most serious) to 5 (least serious).
2. **Gives them a bed** if one is free.
3. **Puts them in the waiting line** if all 15 beds are full.
4. **Sends them home** if their problem is minor (level 5).

## How to run it

You need **Java 8 or newer**.

**In VS Code:** open this folder (with the Extension Pack for Java installed), open `Main.java`, and click **Run**.

**In a terminal:**

```
javac *.java
java Main
```

The program runs 10 tests and prints what happens in each one.

## How sick is each patient?

Every patient gets a level from 1 to 5. **Lower number = sicker = seen sooner.**

| Level | Name | Examples |
|---|---|---|
| 1 | Critical | Heart attack, stroke, not breathing |
| 2 | Emergent | Chest pain, seizure, head injury |
| 3 | Urgent | Broken arm, high fever, pneumonia |
| 4 | Less urgent | Flu, migraine, back pain |
| 5 | Non-urgent | Sprained ankle, common cold, sore throat |

The program knows about 40 illnesses and 20 symptoms (like "shortness of breath" or "runny nose").
Capital letters and extra spaces don't matter, so `"Flu "` and `"flu"` are treated the same.
The full lists are in `TriageRules.java`.

## The rules

- **Sickest first.** A level 1 patient always goes before a level 3 patient.
- **Same level? First come, first served.** If two patients are both level 3, whoever arrived first goes first.
- **Level 5 goes home.** There are only 15 beds, so non-urgent patients are not given one.
- **Unknown symptom? Level 3.** If the program doesn't recognize the problem, it plays it safe so a nurse checks the patient.
- **Nobody waits forever.** Every 4 hours a patient waits, they move up one level (until they reach level 1).

### How waiting moves a patient up

Everyone starts with 1 hour on the clock when they join the line. Here's what happens to a level 4 patient who keeps waiting:

| Hours waited | Level |
|---|---|
| 1 – 4 | 4 (Less urgent) |
| 5 – 8 | 3 (Urgent) |
| 9 – 12 | 2 (Emergent) |
| 13 or more | 1 (Critical) |

The clock is **simulated**: time only passes when the program calls `advanceTime(hours)`, not in real life.

## Example: the waiting line in action

In Test 4, all 15 beds are full. Rita arrives with a migraine (level 4) and waits 4 hours, so she
moves up to level 3. Then Zainab (broken leg, level 3) and Peter (cardiac arrest, level 1) arrive:

```
[QUEUED]   Rita Cole added to the waiting line (Level 4 Less urgent)
[CLOCK]    4 hour(s) pass. Hour is now 4.
[AGING]    Rita Cole waited 5 hrs: Level 4 Less urgent -> Level 3 Urgent
[QUEUED]   Zainab Musa added to the waiting line (Level 3 Urgent)
[QUEUED]   Peter Okafor added to the waiting line (Level 1 Critical)
---- Waiting line (3) - served in this order ----
1. P020 Peter Okafor   | Level 1 | Cardiac arrest | waited 1 hr(s)
2. P018 Rita Cole      | Level 3 | Migraine | waited 5 hr(s)
3. P019 Zainab Musa    | Level 3 | Broken leg | waited 1 hr(s)
```

- **Peter** arrived last but goes first, because he is the sickest.
- **Rita** and **Zainab** are both level 3, so Rita goes first because she arrived earlier.

## The four files

Think of the program as a small hospital:

| File | In the hospital, it is... | What it does |
|---|---|---|
| `Main.java` | The day's events | Runs the 10 tests. **This is the file you run.** |
| `EmergencyDepartment.java` | The ER itself | Holds the 15 beds and the waiting line. Admits, moves, and discharges patients. |
| `TriageRules.java` | The nurse's chart | Knows how sick each illness is and who should go first. |
| `Patient.java` | A patient's chart | Stores one patient's ID, name, symptom, level, bed, and status. |

**How they work together:**

```
Main  ──sends a patient──▶  EmergencyDepartment  ──"how sick?"──▶  TriageRules
                                     │
                    ┌────────────────┼────────────────┐
                    ▼                ▼                ▼
               gets a bed     waits in line      is sent home
```

A patient is always in one of these states:
**New** (just arrived) → **Waiting** or **In bed** → **Discharged** (treated) or **Dismissed** (sent home as level 5).

## Data structures used (and why)

| What we used | What it stores | Why we picked it |
|---|---|---|
| **Priority queue** (a binary heap) | The waiting line | Always keeps the sickest patient at the front, without sorting the whole line. |
| **HashMap** | Illness → level (e.g. "flu" → 4) | Finds a patient's level instantly instead of searching every list. |
| **HashMap** | Patient ID → hours waited | Tracks how long each person has waited. |
| **ArrayList** | Every patient who came in | Keeps a full record, even after patients leave. |
| **2D array** (5 rooms × 3 beds) | The 15 beds | The number of beds never changes, so a fixed-size array fits. |

### How fast is each operation?

| Operation | Speed | Meaning |
|---|---|---|
| Look up an illness's level | O(1) | Instant, no matter how many illnesses |
| Add a patient to the line | O(log n) | Very fast, even with a long line |
| Take the next patient from the line | O(log n) | Very fast, even with a long line |
| See who's next (peek) | O(1) | Instant |
| Find a patient by ID | O(n) | Checks each record one by one |

*n = number of patients*

## The 10 tests

| Test | What it checks | What should happen |
|---|---|---|
| 1 | Admit patients into beds | They get their beds |
| 2 | Show all beds | 4 full, 11 empty |
| 3 | Put a patient in a bed that's taken | Refused |
| 4 | Fill every bed | Extra patients wait in line; the sickest goes to the front |
| 5 | Admit an empty (null) patient | Refused |
| 6 | Free up beds | Less urgent patients leave; a sprained ankle is sent home; freed beds go to the next people in line |
| 7 | Show all beds again | The changes from Test 6 appear |
| 8 | Doctor sends patients home | Their beds become empty |
| 9 | Discharge from an empty bed, or a bed that doesn't exist | Refused |
| 10 | Final check | Shows who's next, finds patients, and prints totals |

At the end, the program prints a summary like this:

```
Total patients seen: 20 | In beds: 12 | Waiting: 1 | Discharged: 6 | Dismissed: 1
Free beds: 3/15 | Queue size: 1
```
