# Registration Flows in Shiftly Scheduler

## Employee Registration (Invitation Link)
1. Employee receives an invitation link containing a code and token.
2. Employee opens `register.html?code=...&token=...`.
3. The page validates the code and token with the backend (`/api/auth/validate-invitation`).
4. If valid, the form is pre-filled with building, department, and inviter info.
5. Employee completes the registration form and submits.
6. Backend processes registration and creates the employee account.

## Admin Registration (Request Access)
1. Prospective admin opens `register-admin.html` and fills out their name, business name, and email.
2. Backend sends a 6-digit verification code to the provided email.
3. Admin enters the code in the 6 input boxes.
4. Frontend verifies the code via `/api/auth/verify-admin-code`.
5. If valid, admin is redirected to `register.html` (no code/token in URL).
6. `register.html` detects admin mode (no code/token, or a flag in localStorage).
7. Form is shown with only basic fields (no building/department info).
8. Admin completes the registration form and submits.
9. Backend processes registration and creates the admin account.

## Dual-Mode Registration Logic
- If code/token are present in the URL, use employee flow.
- If not, check for admin mode (e.g., flag in localStorage after code verification).
- Show/hide form fields and send the correct payload to the backend based on mode.

## Summary
- Employees register via invitation links with pre-filled info.
- Admins register after email code verification, with only basic info.
- Both flows are supported in `register.html` with dynamic form logic.
