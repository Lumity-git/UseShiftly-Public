# Future Multi-Building Admin Features

## 1. Building Ownership & Admin Assignment
- Add `owner` or `admin` field to `Building` entity (reference to Employee).
- Endpoint to assign admin to building.
- Endpoint to list buildings by admin.

## 2. Building-Scoped Employee/Manager Listing
- Endpoint to list employees/managers for a specific building (`GET /api/buildings/{id}/employees`).
- Restrict manager access so they only see/manage employees in their building.

## 3. Admin/Owner Account Management
- Endpoints for owners/admins to update their own account info.
- Endpoints for owners/admins to invite managers/employees to their building.

## 4. Dev/Super-Admin Panel
- Panel to manage all buildings, owners, and pricing.
- View and edit all building assignments and user roles.

## 5. Pricing/Subscription Model
- Add subscription info to Building or Owner (user count, billing).
- Endpoint to view/update subscription details.

## 6. Access Control
- Ensure managers only see/manage users in their building.
- Owners/admins can see all users in their buildings.
- Super-admin/dev can see all buildings and users.

---

**Note:** These features will support scalable, secure, and flexible multi-building management for owners and managers, with future billing and dev panel capabilities.
