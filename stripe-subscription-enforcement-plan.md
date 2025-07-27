# Stripe Subscription Enforcement & Automated Invoice Notification System

## 1. Automated Invoice Notification (2 Days Before Billing)
- [x] Create a scheduled backend task (Spring @Scheduled) to run daily.
- [x] For each admin, check if their billing date is in 2 days.
- [x] Query employees, package, and projected bill for the admin.
- [x] Send an email to the admin with:
    - List of employees
    - Package type
    - Projected charge
    - Billing period
- [x] Log the notification event in the audit log.

## 2. Subscription Enforcement (Access Control)
- [x] Track Stripe subscription status for each admin (active, past_due, canceled, unpaid).
- [x] On payment failure or subscription cancellation:
    - [x] Set admin and all their employees to inactive (disable access).
    - [x] Notify admin and employees of access suspension via email and in-app notification.
- [x] On successful payment:
    - [x] Reactivate admin and employees (restore access).
    - [x] Notify admin and employees of access restoration.
- [x] Log all access changes and payment events in the audit log.

## 3. Implementation Steps
### Backend
- [x] Add Stripe webhook endpoint to listen for payment events (invoice.paid, invoice.payment_failed, customer.subscription.deleted, etc.).
- [x] Update admin and employee status based on webhook events.
- [x] Implement scheduled task for invoice notification.
- [x] Integrate with email service for notifications.
- [x] Update audit log with all relevant events.

### Frontend
- [x] Display access status and payment status to admins only (not employees).
- [x] Show notification banners for suspended/restored access.
- [x] Ensure login and dashboard logic checks for active status.

### Database
- [x] Add fields to track subscription/payment status for admins.
- [x] Ensure employee active status is linked to admin's subscription status.
- [x] Log all relevant events in the audit log table.

## 4. Testing & Compliance
- [ ] Test all scenarios: payment success, payment failure, subscription cancellation, restoration.
- [ ] Ensure compliance with Stripe and data privacy requirements.
- [ ] Document all logic and flows for audit purposes.

---

**Current Step:** Design and implement automated invoice notification and subscription enforcement system.
