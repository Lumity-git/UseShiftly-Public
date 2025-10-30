#
# Advanced Multi-Tenant Security & Architecture Notes

## 2. Custom Repository Layer Scoping
Instead of just relying on controller/service checks:

```java
@Query("SELECT e FROM Employee e WHERE e.building.admin.id = :adminId")
List<Employee> findAllByAdminId(@Param("adminId") Long adminId);
```
Make sure all data access is scoped at the query level, not just filtered afterward.

## 3. Prevent IDOR (Insecure Direct Object Reference)
A common issue in multi-tenant apps:

A malicious user could pass another admin’s buildingId in a request.

✅ To prevent:

Always validate resource ownership before operations.

Add ownership checks like:

```java
if (!building.getAdmin().equals(currentUser)) {
    throw new AccessDeniedException("Not your building");
}
```

## 4. Super-Admin Hardening
Even though the super-admin has no access to regular data:

- Ensure its role can never be downgraded or escalated to normal user access.
- Do not let super-admin impersonate admins, unless that’s an intentional debug feature behind audit logging.

## 5. Consider Multi-Tenant Schema Approaches
Your design is single-schema multi-tenant, which is perfect for small to medium SaaS apps.

But in future if you scale:

- Schema-per-tenant can offer better data separation.
- Or database-per-tenant (heavier but highest isolation).
Just keep in mind for roadmap planning.

## 6. Add Middleware / Interceptor Check (Optional)
To reduce redundant checks in every controller, use an interceptor or filter:

- Inject the current user
- Check if they are allowed to access the buildingId in the path/body
- Short-circuit invalid requests

---
# Multi-Tenant Admin Architecture for UseShiftly

## Overview
This document outlines the requirements and implementation plan for a strict multi-tenant architecture where each admin manages their own isolated set of buildings, managers, departments, and employees. The super-admin (developer) manages only admin accounts and has no access to regular employee/manager/building data.

## Key Principles
- **Isolation:** Each admin is a tenant with their own buildings, managers, departments, and employees. No data is shared between admins.
- **Scoping:** All queries and modifications for managers, employees, and departments must be scoped to the current admin's buildings.
- **Super-Admin:** The developer (super-admin) can only manage admin accounts, not regular users or buildings.

## Required Backend Changes
1. **Entity Relationships**
   - Ensure `Building` has a required `admin` field (reference to Employee with ADMIN role).
   - Ensure `Manager` and `Employee` reference their `Building`.
   - Departments must be scoped to a building (and thus to an admin).

2. **Controller/Service Enforcement**
   - All endpoints for employees, managers, and departments must:
     - Check that the current user is the admin for the target building, or the manager for their own building.
     - Filter all queries by building and admin.
     - Prevent cross-admin access.
   - Super-admin endpoints for managing admin accounts must be separate and not expose regular user/building data.

3. **Access Control**
   - Use `@PreAuthorize` and/or service-level checks to enforce scoping.
   - Add utility methods to check building ownership/assignment for the current user.
   - All create/update/delete operations for employees, managers, and departments must validate admin/building relationship.

4. **Endpoints to Update**
   - EmployeeController: All list, get, create, update, delete endpoints.
   - DepartmentController: All endpoints.
   - BuildingController: All endpoints except super-admin endpoints.
   - AuthController: Invitation endpoints must require building/admin scoping.

5. **Super-Admin Management**
   - Create a separate controller for super-admin to manage admin accounts only.
   - Super-admin cannot access or modify any regular user/building/department data.

## Implementation Steps
- [x] Update entity models to enforce relationships. ✅
- [x] Refactor controllers to enforce scoping and isolation. (EmployeeController, DepartmentController, BuildingController, AuthController)
- [x] Add/Update utility methods for ownership checks. ✅
- [x] Add/Update tests for access control and isolation. ✅
- [ ] Document all endpoints and access rules.

## TODO
- [x] Enforce admin isolation in all relevant controllers/services (EmployeeController, DepartmentController, BuildingController, AuthController).
- [x] Add/Update utility methods for ownership checks. ✅
- [ ] Add super-admin controller for admin management.
- [ ] Update frontend to respect new scoping (if needed).
- [x] Add tests for multi-tenant isolation. ✅
- [x] Update entity models to enforce relationships. ✅
## Progress Notes
- DepartmentController, BuildingController, and AuthController endpoints now enforce strict multi-tenant isolation.
- Invitation entity updated to include adminId for secure registration validation.
- All required tests for access control and multi-tenant isolation are now implemented and passing in CI.
- Next: Document all endpoints and access rules for admins, managers, and super-admin.

---

## Endpoint & Access Rule Documentation

### EmployeeController
| Endpoint | Method | Roles Allowed | Access Rule |
|---|---|---|---|
| /api/employees | GET | ADMIN, MANAGER | Only employees in admin's buildings or manager's building |
| /api/employees/{id} | GET | ADMIN, MANAGER | Only if employee belongs to admin's buildings or manager's building |
| /api/employees | POST | ADMIN, MANAGER | Can only create in own building(s) |
| /api/employees/{id} | PUT | ADMIN, MANAGER | Only if employee belongs to admin's buildings or manager's building |
| /api/employees/{id}/delete | DELETE | ADMIN | Only if employee belongs to admin's buildings |
| /api/employees/{id}/status | PUT | ADMIN | Only if employee belongs to admin's buildings |
| /api/employees/department/{departmentId} | GET | ADMIN, MANAGER | Only if department belongs to admin's buildings or manager's building |
| /api/employees/by-building/{buildingId} | GET | ADMIN, MANAGER | Only if building belongs to admin or manager |
| /api/employees/export | GET | ADMIN, MANAGER | Only employees in admin's buildings or manager's building |

### DepartmentController
| Endpoint | Method | Roles Allowed | Access Rule |
|---|---|---|---|
| /api/departments | GET | ADMIN, MANAGER | Only departments in admin's buildings or manager's building |
| /api/departments/{id} | GET | ADMIN, MANAGER | Only if department belongs to admin's buildings or manager's building |
| /api/departments | POST | ADMIN, MANAGER | Can only create in own building(s) |
| /api/departments/{id} | PUT | ADMIN, MANAGER | Only if department belongs to admin's buildings or manager's building |
| /api/departments/{id} | DELETE | ADMIN, MANAGER | Only if department belongs to admin's buildings or manager's building |

### BuildingController
| Endpoint | Method | Roles Allowed | Access Rule |
|---|---|---|---|
| /api/buildings/my-buildings | GET | ADMIN | Only buildings for current admin |
| /api/buildings/my-building | GET | MANAGER | Only building assigned to current manager |
| /api/buildings | POST | ADMIN | Can only create for self |

### AuthController (Invitations)
| Endpoint | Method | Roles Allowed | Access Rule |
|---|---|---|---|
| /api/auth/invite | POST | ADMIN | Can only invite to own building(s) |
| /api/auth/validate-invitation | POST | PUBLIC | Invitation must be scoped to admin/building |

### Super-Admin Controller (Planned)
| Endpoint | Method | Roles Allowed | Access Rule |
|---|---|---|---|
| /api/super-admin/admins | GET, POST, PUT, DELETE | SUPER_ADMIN | Only manages admin accounts, cannot access regular user/building/department data |

---
This table should be updated as endpoints evolve. All endpoints must enforce strict tenant isolation and role-based access as described above.

This document should be updated as implementation progresses.
