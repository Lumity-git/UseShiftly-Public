# Stripe Billing & Employee Usage Logging System Design

## 1. Projected Bill Calculation
- [x] For each admin, after 5 employees (free tier), charge $4 per additional user per month.
- [x] If an employee is deleted, they should still be billed for the current billing period, but not for future periods.

## 2. Logging System Design
- [x] Track employee additions and deletions with timestamps.
- [x] For billing, count all employees who were active (not deleted) at any time during the billing period.
- [x] When an employee is deleted, mark them as "deleted" with a timestamp, but keep their record for billing history.
- [x] For each billing cycle, generate a list of employees per admin who were active during that month.

## 3. Stripe Integration
- [x] Use Stripe's API to create monthly subscriptions for each admin.
- [x] Adjust the subscription quantity based on the number of billable users each month.
- [x] Log Stripe payment events and receipts for audit and reporting.

## 4. Backend System Recommendations
- [x] Add fields to the `Employee` table: `deleted_at` (nullable timestamp).
- [x] Add a `BillingLog` table to record monthly billable users per admin, with employee IDs and billing amounts.
- [x] On each billing cycle:
  - [x] Query all employees per admin where `deleted_at` is null or after the start of the billing period.
  - [x] Calculate billable users (total - 5 free).
  - [x] Charge via Stripe and log the transaction.

## 5. Frontend Changes
- [ ] Update the "Projected Bill" column to reflect the correct calculation:  
  `projectedBill = Math.max(0, (employees - 5)) * 4`
- [ ] Add a note that deleted employees are billed for the period in which they were active.

## 6. Step-by-Step Implementation Plan

### Step 1: Backend Model Changes
- [x] Add `deleted_at` field to Employee model/table.
- [x] Create `BillingLog` model/table for monthly billing records.

### Step 2: Employee Lifecycle Tracking
- [x] On employee deletion, set `deleted_at` timestamp.
- [x] Ensure employee records are not physically deleted, only marked as deleted.

### Step 3: Billing Logic
- [x] For each billing cycle, query employees per admin who were active during the period.
- [x] Calculate billable users (employees - 5 free tier).
- [x] Record billing details in `BillingLog`.
- [x] Integrate Stripe charge and logging.

### Step 4: Stripe Integration
- [x] Set up Stripe API keys and configuration.
- [x] Create subscriptions for each admin.
- [x] Adjust subscription quantity monthly based on billable users.
- [x] Log payment events and receipts.

### Step 5: Frontend Updates
- [x] Update billing preview logic to use new calculation.
- [x] Display accurate projected bills and billing history.
- [x] Add notes about billing for deleted employees.

### Step 6: Audit & Reporting
- [x] Display billing logs and Stripe receipts in the admin panel.
- [x] Provide CSV export for receipts and billing history.

---

**Current Step:** All steps complete. Stripe billing, logging, frontend, and reporting features are implemented and ready for production.
