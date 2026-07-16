# Design Decisions

## 1. Round‑down for the 30% rule

**Decision:**  
For a team of `n` members, the maximum number of employees allowed on leave on any working day is `floor(n * 0.30)`.  
For example, a team of 4 → `floor(1.2) = 1`.

**Alternatives considered:**
- Round up  – would allow 2 people, potentially understaffing the team.
- Round to nearest – could fluctuate between 1 and 2, which is inconsistent.

**Reasoning:**  
Rounding down guarantees that the team is never understaffed beyond the intended limit. Since employees are discrete units, we cannot have a fractional person on leave. The safer choice is to err on the side of caution and ensure staffing stays above the threshold.

---

## 2. Multi‑day requests – apply the 30% rule per day

**Decision:**  
For a request spanning multiple days, the 30% rule is checked independently for each working day in the range. Weekends and public holidays are skipped entirely.

**Alternatives considered:**
- Apply the rule to the whole range (e.g., average or total count). This could allow a day with too many people on leave if other days have fewer.
- Apply only on the start or end date – this would miss intermediate days.

**Reasoning:**  
The business rule aims to ensure that on any working day the team has enough staff. Therefore, each day must be validated individually. This prevents a situation where one day is overloaded while another is underused.

---

## 3. Overlap semantics – only approved requests block new requests

**Decision:**  
A new leave request is rejected only if it overlaps an **approved** request for the same employee. Pending requests do **not** block new requests.

**Alternatives considered:**
- Treat pending requests as blocking too – this would prevent an employee from modifying or replacing a pending request.
- Ignore all existing requests – would allow double‑booking approved leaves.

**Reasoning:**  
Allowing pending requests to be superseded gives employees flexibility (e.g., they may want to change dates before the manager reviews). Only approved leaves represent a firm commitment that should not be violated. This interpretation balances fairness and practicality.

---

## Additional decisions

- **Weekends and public holidays:**  
  These are not considered working days. They are skipped in the 30% validation and do not count against the employee's leave balance (if a balance were tracked). The request itself may include these days, but they are ignored for all rule calculations.

- **Calendar view:**  
  The front‑end displays a Calendar-style view for the next 30 days, showing the names of employees on approved leave for each day. This visual representation helps managers quickly spot conflicts.