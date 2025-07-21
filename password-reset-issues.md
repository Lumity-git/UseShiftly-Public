# Password Reset Flow: Issues and Fixes Needed

## Problem Summary
- Admins and managers should be able to generate password reset links for employees.
- Employees should be able to reset their password using a link with code and token, without being authenticated.
- Currently, the backend sometimes returns 401 Unauthorized or other errors when using the password reset link.

## Current State
- **Frontend** correctly sends `{ newPassword, code, token }` to `/api/auth/change-password`.
- **Backend** endpoint `/api/auth/change-password` is designed to allow unauthenticated password reset via code/token.
- **Spring Security** config allows unauthenticated access to `/api/auth/change-password`.

## Issues
1. **401 Unauthorized on password reset link**
   - Sometimes, unauthenticated users are blocked from accessing `/api/auth/change-password`.
   - May be due to backend logic, security filter, or token validation issues.
2. **NullPointerException in backend response**
   - Occurred when building response map with null values.
   - Fixed by using HashMap and only including non-null values.
3. **Frontend/Backend contract**
   - Confirmed both sides match for password reset flow.

## What Needs to Be Fixed
- [ ] Ensure `/api/auth/change-password` always allows unauthenticated access for code/token requests.
- [ ] Add extra logging to backend for all password reset attempts (code/token, authentication principal, etc).
- [ ] Confirm that the frontend always sends the correct payload and endpoint.
- [ ] Test password reset flow end-to-end for all user roles.
- [ ] Document any additional security or validation requirements for password reset.

## Next Steps
1. Add more backend logging for password reset attempts.
2. Test with various scenarios (valid/invalid code/token, authenticated/unauthenticated).
3. Confirm security config is loaded and correct in all environments.
4. Update this document with any new findings or fixes.

---

_Last updated: 2025-07-20_
