# Auto-Scheduling Feature: UseShiftly Shift Scheduler

## Goal
Enable managers/admins to auto-generate shift schedules for departments based on employee availability, ensuring no conflicts and proper coverage.

## Requirements
- Employees specify their availability (days/times).
- Managers/admins can trigger auto-scheduling for a department and date range.
- Scheduling logic must:
  - Assign shifts only within employee availability.
  - Avoid overlapping/conflicting assignments.
  - Cover all required shifts for the department.
  - Optionally balance hours among employees.

## Implementation Plan
1. **Employee Availability Model**
   - Entity to store available days/times for each employee.
2. **DTOs**
   - For availability input and auto-schedule requests.
3. **Service Logic**
   - Algorithm to generate shifts based on availability and department needs.
4. **Controller Endpoint**
   - For managers/admins to trigger auto-scheduling.
5. **Validation**
   - Ensure no scheduling conflicts and proper shift creation.

## Notes
- Consider fairness, preferences, and legal constraints (max hours, breaks).
- Email notifications for generated schedules.
- UI for managers to review/edit auto-generated schedules before publishing.

---

---

## [2025-07-23] Department Shift Templates for Auto-Scheduling

### Problem
Currently, auto-scheduled shifts are created at default times (e.g., 4:00 AM) because there is no way to define department-specific shift start/end times. This does not reflect real-world needs (e.g., store opens at 11:00, cooks start at 8:30, etc.).

### Solution Plan
1. Allow admins/managers to define default shift templates for each department (name, start time, end time, etc.)
2. Store these templates in the database (new table: shift_templates)
3. Backend endpoints to CRUD shift templates
4. Frontend UI for managing shift templates (button/modal in shifts.html)
5. Update auto-scheduling logic to use templates for each department

### Progress Tracking
- [ ] 1. Add `shift_templates` table to database
- [ ] 2. Backend: Model, Repository, Service, Controller for shift templates
- [ ] 3. Frontend: UI for managing templates (CRUD)
- [ ] 4. Update auto-scheduling logic to use templates
- [ ] 5. Testing and validation

**Next step:** Implement database migration and backend model for `shift_templates`.
