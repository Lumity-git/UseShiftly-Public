# Building Scheduling Feature Implementation Steps

This guide outlines the steps required to add building separation and filtering to the employee scheduling system, so businesses can schedule by building.

## 1. Backend Changes

### 1.1. Building Model & API
- Ensure you have a `Building` entity/model in your backend.
- Create a `BuildingRepository` and (if not present) a `BuildingController` with endpoints:
  - `GET /api/buildings` — returns all buildings (id, name, etc.)

### 1.2. Employee Model
- Ensure the `Employee` entity has a `building` field (relation to Building).
- Update Employee DTOs and API responses to include `buildingId` and `buildingName`.
- Update employee creation and update endpoints to accept a building ID.

### 1.3. Bulk Import
- Update bulk import logic to accept building info (buildingId or buildingName).

## 2. Frontend Changes

### 2.1. UI Updates
- Add a "Building" filter dropdown to the employee list page (next to Department, Role, Status).
- Add a "Building" select to the employee modal (for create/edit).
- Add building info to employee cards/details.

### 2.2. Data Loading
- Fetch buildings from `/api/buildings` and populate building selects.
- Ensure employees API returns building info for each employee.

### 2.3. Filtering Logic
- Update filter logic to include building selection.

### 2.4. Employee Creation/Editing
- Update employee creation/editing forms to send buildingId to backend.

### 2.5. Bulk Import
- Update frontend bulk import template and logic to support building info.

## 3. Testing
- Test employee creation, editing, filtering, and bulk import with building selection.
- Test UI for correct display and filtering by building.

## 4. Documentation
- Update user documentation to explain building scheduling and filtering features.

---

**Note:** Backend and frontend must both support building info for this feature to work correctly. If you need code samples for any step, request them specifically.
