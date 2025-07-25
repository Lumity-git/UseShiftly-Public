# Super Admin Billing & Receipt Management Panel

## Overview
This admin panel is dedicated to monitoring and managing billing for all admin accounts. It provides:
- Real-time counts of employees, managers, and buildings per admin
- Automated billing and receipt management
- Email notifications to admins for billing and receipts
- Accurate logging for tax and business compliance


## Features

### 1. Usage Monitoring
- Track the number of employees, managers, and buildings for each admin
- Display usage statistics in the super admin panel
- Usage data is the basis for billing calculations

### 2. Billing Model
- Each admin receives up to 5 employees for free
- After 5 employees, billing is $4 per user per month
- Managers and buildings are included in usage tracking for reporting, but only employees count toward billing

### 3. Receipt & Payment Management
- Generate monthly receipts for each admin
- Email receipts to admins automatically
- Store and log all receipts for tax and compliance purposes
- Support for payment processing integration (future)

### 4. Tax & Compliance Logging
- All billing and payment events are logged for audit and tax purposes
- Logs include admin, business, employee, and payment details
- Export logs for accounting and compliance

### 5. Admin Self-Service Portal
- Admins can view/download past receipts and invoices
- Update billing info (address, tax ID, payment method)
- Show current usage and projected next bill

### 6. Payment Processing
- Integrate with payment gateways (Stripe, PayPal, etc.)
- Support for credit card, ACH, and invoicing
- Handle failed payments, retries, and dunning emails

### 7. Subscription Management
- Allow admins to upgrade/downgrade plans
- Support for pausing/cancelling subscriptions
- Prorated billing for mid-cycle changes

### 8. Tax Calculation & Compliance
- Automatic tax calculation based on admin’s location
- Support for VAT, GST, and US sales tax
- Generate tax-compliant invoices and receipts

### 9. Audit & Security
- Log all billing-related actions (who did what, when)
- Secure storage of payment and personal data (PCI compliance)
- Role-based access for billing features

### 10. Notifications & Reminders
- Automated reminders for upcoming payments, expiring cards, etc.
- Customizable email templates for receipts and billing events

### 11. Reporting & Analytics
- Dashboard for super admin: revenue, churn, ARPU, etc.
- Exportable reports for accounting and business analysis

### 12. API for Billing Data
- Provide API endpoints for admins to fetch their own billing/usage data
- Webhooks for payment events (for integrations)


## Implementation Plan
- [x] Add usage tracking endpoints and UI to super admin panel (**First step complete**)
- [x] Implement billing calculation logic (**Complete**)
- [x] Integrate email notifications for receipts (**Complete**)
- [x] Add receipt and payment logging (exportable) (**Complete**)
- [ ] Prepare for payment processor integration (Stripe, etc.)
- [x] Add admin self-service portal for billing (**Complete**)
- [x] Add advanced reporting and analytics (**Complete**)
- [x] Add tax calculation and compliance features (**Basic event logging complete**)

---

## Step 1: Usage Tracking Endpoints and UI

### Backend
- Create API endpoints for super admin to fetch per-admin usage:
  - Number of employees, managers, and buildings per admin
  - Usage history (for billing periods)

### Frontend
- Add a dashboard section in the super admin panel to display usage stats for each admin
- Show which admins are over the free tier and their current billable usage

### Example API (implemented)
- `GET /api/super-admin/admins/usage` → returns list of admins with usage counts
- `GET /api/super-admin/billing/preview` → preview bills for all admins
- `POST /api/super-admin/billing/generate-receipts?period=YYYY-MM` → generate and email receipts for all admins
- `GET /api/super-admin/billing/receipts` → get all receipts
- `GET /api/super-admin/billing/receipts/{adminEmail}` → get receipts for a specific admin
- `GET /api/super-admin/billing/events` → get all billing events (audit log)
- `GET /api/super-admin/billing/events/{adminEmail}` → get billing events for a specific admin
- `GET /api/admin/billing/receipts/{adminEmail}` → admin self-service: get own receipts
- `GET /api/admin/billing/projected-bill/{adminEmail}` → admin self-service: get projected bill

### Example UI Table
| Admin Email         | Employees | Managers | Buildings | Billable Users | Over Free Tier? |
|---------------------|-----------|----------|-----------|---------------|-----------------|
| admin1@company.com  | 8         | 2        | 3         | 3             | Yes             |
| admin2@company.com  | 4         | 1        | 1         | 0             | No              |

---

## Notes
- This panel is for super admin use only
- All billing and compliance features are isolated from regular admin and employee UIs
- Designed for SaaS, multi-tenant, and tax-compliant operation
