package com.useshiftly.scheduler.controller;

import com.useshiftly.scheduler.dto.EmployeeDTO;
import com.useshiftly.scheduler.model.Employee;
import com.useshiftly.scheduler.service.EmployeeService;
import com.useshiftly.scheduler.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
    /**
     * Lists all employees (except admins) for the manager's assigned building.
     * Endpoint: GET /api/employees/my-building
     * Only accessible by MANAGER role.
     */
    @GetMapping("/my-building")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getEmployeesForMyBuilding(@AuthenticationPrincipal Employee currentUser) {
        // Find manager's building
        var buildingOpt = employeeService.getBuildingForManager(currentUser.getId());
        if (buildingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(new MessageResponse("Manager is not assigned to any building."));
        }
        Long buildingId = buildingOpt.get().getId();
        // Get all employees for this building
        List<Employee> employees = employeeService.getEmployeesByBuilding(buildingId);
        // Filter out admins
        List<EmployeeDTO> dtos = employees.stream()
            .filter(e -> e.getRole() != Employee.Role.ADMIN)
            .map(EmployeeDTO::fromEntity)
            .toList();
        return ResponseEntity.ok(dtos);
    }
    // Utility: Check if current user is admin for a building
    private void assertAdminForBuilding(Long buildingId, Employee currentUser) {
        var buildingOpt = employeeService.getBuildingById(buildingId);
        if (buildingOpt.isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Not your building");
        }
        var building = buildingOpt.get();
        boolean isAdmin = building.getAdmin() != null && building.getAdmin().getId().equals(currentUser.getId());
        boolean isManager = building.getManagers() != null && building.getManagers().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
        if (!(isAdmin || isManager)) {
            throw new org.springframework.security.access.AccessDeniedException("Not your building");
        }
    }
    /**
     * Permanently deletes an employee by ID (Admin only).
     * Endpoint: DELETE /api/employees/{id}/delete
     * Logs the action.
     */
    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            employeeService.deleteEmployee(id);
            userActionLogService.logAction("DELETED_EMPLOYEE", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Employee deleted permanently"));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_DELETE_EMPLOYEE", currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    /**
     * Updates only the active status of an employee (activate/deactivate).
     * Endpoint: PUT /api/employees/{id}/status
     * Request: { "active": true/false }
     * Response: Success or error message
     */

    private final com.useshiftly.scheduler.service.UserActionLogService userActionLogService;
    private final NotificationService notificationService;

    /**
     * Lists all employees assigned to a specific building.
     * Endpoint: GET /api/employees/by-building/{buildingId}
     * @param buildingId the building ID
     * @return list of EmployeeDTOs
     */
    @GetMapping("/by-building/{buildingId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeesByBuilding(@PathVariable Long buildingId, @AuthenticationPrincipal Employee currentUser) {
        assertAdminForBuilding(buildingId, currentUser);
        List<Employee> employees = employeeService.getEmployeesByBuilding(buildingId);
        List<EmployeeDTO> dtos = employees.stream().map(EmployeeDTO::fromEntity).toList();
        return ResponseEntity.ok(dtos);
    }
    /**
     * Update employee info by ID (Manager/Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id,
                                            @RequestBody Map<String, Object> employeeUpdate,
                                            @AuthenticationPrincipal Employee currentUser) {
        try {
            Employee employee = employeeService.getEmployeeById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            assertAdminForBuilding(employee.getBuilding().getId(), currentUser);
            if (employeeUpdate.containsKey("firstName")) {
                employee.setFirstName((String) employeeUpdate.get("firstName"));
            }
            if (employeeUpdate.containsKey("lastName")) {
                employee.setLastName((String) employeeUpdate.get("lastName"));
            }
            if (employeeUpdate.containsKey("email")) {
                employee.setEmail((String) employeeUpdate.get("email"));
            }
            if (employeeUpdate.containsKey("phoneNumber")) {
                employee.setPhoneNumber((String) employeeUpdate.get("phoneNumber"));
            }
            if (employeeUpdate.containsKey("dateOfBirth")) {
                employee.setDateOfBirth((String) employeeUpdate.get("dateOfBirth"));
            }
            if (employeeUpdate.containsKey("address")) {
                employee.setAddress((String) employeeUpdate.get("address"));
            }
            if (employeeUpdate.containsKey("emergencyContactName")) {
                employee.setEmergencyContactName((String) employeeUpdate.get("emergencyContactName"));
            }
            if (employeeUpdate.containsKey("emergencyContactRelation")) {
                employee.setEmergencyContactRelation((String) employeeUpdate.get("emergencyContactRelation"));
            }
            if (employeeUpdate.containsKey("emergencyContactPhone")) {
                employee.setEmergencyContactPhone((String) employeeUpdate.get("emergencyContactPhone"));
            }
            if (employeeUpdate.containsKey("role")) {
                employee.setRole(Employee.Role.valueOf((String) employeeUpdate.get("role")));
            }
            if (employeeUpdate.containsKey("departmentId")) {
                Object deptObj = employeeUpdate.get("departmentId");
                if (deptObj != null && !deptObj.toString().isBlank()) {
                    try {
                        Long deptId = Long.valueOf(deptObj.toString());
                        employeeService.assignEmployeeToDepartment(employee, deptId);
                    } catch (NumberFormatException ex) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Invalid departmentId value"));
                    }
                }
            }
            if (employeeUpdate.containsKey("buildingId")) {
                Object buildingObj = employeeUpdate.get("buildingId");
                if (buildingObj != null && !buildingObj.toString().isBlank()) {
                    try {
                        Long buildingId = Long.valueOf(buildingObj.toString());
                        employeeService.assignEmployeeToBuilding(employee, buildingId);
                    } catch (NumberFormatException ex) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Invalid buildingId value"));
                    }
                }
            }
            if (employeeUpdate.containsKey("active")) {
                employee.setActive(Boolean.valueOf(employeeUpdate.get("active").toString()));
            }
            Employee updated = employeeService.updateEmployee(employee);
            userActionLogService.logAction("UPDATED_EMPLOYEE", currentUser.getId());
            return ResponseEntity.ok(EmployeeDTO.fromEntity(updated));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_UPDATE_EMPLOYEE", currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Export employees as CSV (Manager/Admin only)
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<String> exportEmployeesCsv(@AuthenticationPrincipal Employee currentUser) {
        List<Employee> employees = employeeService.getAllActiveEmployees();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,First Name,Last Name,Email,Phone,Role,Department,Active\n");
        for (Employee e : employees) {
            csv.append(e.getId()).append(",")
                .append(e.getFirstName()).append(",")
                .append(e.getLastName()).append(",")
                .append(e.getEmail()).append(",")
                .append(e.getPhoneNumber() != null ? e.getPhoneNumber() : "").append(",")
                .append(e.getRole().name()).append(",")
                .append(e.getDepartment() != null ? e.getDepartment().getName() : "Unassigned").append(",")
                .append(e.getActive() ? "Active" : "Inactive").append("\n");
        }
        userActionLogService.logAction("EXPORTED_EMPLOYEES", currentUser.getId());
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .body(csv.toString());
    }
    /**
     * EmployeeController: Handles all employee-related REST API endpoints for the UseShiftly system.
     * 
     * Usage:
     * - Use this controller for employee CRUD, profile, and role management operations.
     * - For self-service profile updates, use /me and /profile endpoints.
     * - For admin/manager operations, use endpoints requiring MANAGER or ADMIN roles.
     * - All responses use EmployeeDTO for safe serialization.
     * 
     * Security:
     * - Most endpoints require MANAGER or ADMIN role, some allow EMPLOYEE.
     * - Role-based access is enforced via @PreAuthorize annotations.
     * - Authenticated user is injected via @AuthenticationPrincipal.
     * 
     * Key Endpoints:
     * - GET /api/employees: List all active employees (manager/admin only)
     * - GET /api/employees/me: Get current authenticated employee profile
     * - GET /api/employees/profile: Get current employee profile (alias)
     * - GET /api/employees/{id}: Get employee details by ID (manager/admin only)
     * - GET /api/employees/eligible-for-trade: List employees eligible for shift trade (excluding self)
     * - PUT /api/employees/me: Update current employee's profile (self-service)
     * - GET /api/employees/department/{departmentId}: List employees by department (manager/admin only)
     * - DELETE /api/employees/{id}: Deactivate employee (admin only)
     * - PUT /api/employees/{id}/role: Update employee role (admin only)
     * - POST /api/employees: Create new employee (manager/admin only)
     * - POST /api/employees/departments/{departmentId}/assign: Assign employees to department (manager/admin only)
     * 
     * Dependencies:
     * - EmployeeService: Business logic for employee management
     * - EmployeeDTO: Data transfer object for employee responses
     */
    /**
     * Get eligible employees for shift trade (excluding self). Accessible by EMPLOYEE role.
     */
    @GetMapping("/eligible-for-trade")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDTO>> getEligibleEmployeesForTrade(@AuthenticationPrincipal Employee currentUser) {
        List<Employee> employees = employeeService.getAllActiveEmployees();
        List<EmployeeDTO> eligible = employees.stream()
            .filter(e -> !e.getId().equals(currentUser.getId()))
            .map(EmployeeDTO::fromEntity)
            .toList();
        return ResponseEntity.ok(eligible);
    }
    /**
     * Get employee details by ID (Manager/Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            var employeeOpt = employeeService.getEmployeeById(id);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(404).body(new MessageResponse("Employee not found"));
            }
            assertAdminForBuilding(employeeOpt.get().getBuilding().getId(), currentUser);
            return ResponseEntity.ok(EmployeeDTO.fromEntity(employeeOpt.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    private final EmployeeService employeeService;
    
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees(@AuthenticationPrincipal Employee currentUser) {
        List<Employee> employees = employeeService.getAllByAdminId(currentUser.getId());
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(EmployeeDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(employeeDTOs);
    }
    
    @GetMapping("/me")
    public ResponseEntity<EmployeeDTO> getCurrentEmployee(@AuthenticationPrincipal Employee currentUser) {
        return ResponseEntity.ok(EmployeeDTO.fromEntity(currentUser));
    }
    
    @GetMapping("/profile")
    public ResponseEntity<EmployeeDTO> getEmployeeProfile(@AuthenticationPrincipal Employee currentUser) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeWithBuilding(currentUser.getId());
        if (employeeOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(EmployeeDTO.fromEntity(employeeOpt.get()));
    }
    
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentEmployee(@RequestBody Map<String, Object> employeeUpdate, 
                                                   @AuthenticationPrincipal Employee currentUser) {
        try {
            // Only allow updating certain fields for regular employees
            if (employeeUpdate.containsKey("firstName")) {
                currentUser.setFirstName((String) employeeUpdate.get("firstName"));
            }
            if (employeeUpdate.containsKey("lastName")) {
                currentUser.setLastName((String) employeeUpdate.get("lastName"));
            }
            if (employeeUpdate.containsKey("phoneNumber")) {
                currentUser.setPhoneNumber((String) employeeUpdate.get("phoneNumber"));
            }
            
            Employee updated = employeeService.updateEmployee(currentUser);
            return ResponseEntity.ok(EmployeeDTO.fromEntity(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable Long departmentId, @AuthenticationPrincipal Employee currentUser) {
        var deptOpt = employeeService.getDepartmentById(departmentId);
        if (deptOpt.isEmpty()) {
            return ResponseEntity.status(404).body(List.of());
        }
        assertAdminForBuilding(deptOpt.get().getBuilding().getId(), currentUser);
        List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(EmployeeDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(employeeDTOs);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateEmployee(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            Employee employee = employeeService.getEmployeeById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            assertAdminForBuilding(employee.getBuilding().getId(), currentUser);
            employeeService.deactivateEmployee(id);
            userActionLogService.logAction("DEACTIVATED_EMPLOYEE", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Employee deactivated successfully"));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_DEACTIVATE_EMPLOYEE", currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEmployeeRole(@PathVariable Long id, 
                                               @RequestBody Map<String, String> request,
                                               @AuthenticationPrincipal Employee currentUser) {
        try {
            String newRole = request.get("role");
            if (!List.of("EMPLOYEE", "MANAGER", "ADMIN").contains(newRole)) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid role"));
            }
            
            Employee employee = employeeService.getEmployeeById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            employee.setRole(Employee.Role.valueOf(newRole));
            Employee updated = employeeService.updateEmployee(employee);
            
            userActionLogService.logAction("UPDATED_EMPLOYEE_ROLE", currentUser.getId());
            return ResponseEntity.ok(EmployeeDTO.fromEntity(updated));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_UPDATE_EMPLOYEE_ROLE", currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, Object> employeeData,
                                          @AuthenticationPrincipal Employee currentUser) {
        try {
            // Require buildingId
            if (!employeeData.containsKey("buildingId")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: buildingId is required for employee creation."));
            }
            Long buildingId = Long.valueOf(employeeData.get("buildingId").toString());
            var buildingOpt = employeeService.getBuildingById(buildingId);
            if (buildingOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Building not found."));
            }
            Employee newEmployee = new Employee();
            newEmployee.setId(null); // Always null, let JPA generate
            newEmployee.setUuid(java.util.UUID.randomUUID().toString()); // Always new UUID
            newEmployee.setFirstName((String) employeeData.get("firstName"));
            newEmployee.setLastName((String) employeeData.get("lastName"));
            newEmployee.setEmail((String) employeeData.get("email"));
            newEmployee.setPhoneNumber((String) employeeData.get("phoneNumber"));
            newEmployee.setDateOfBirth((String) employeeData.get("dateOfBirth"));
            newEmployee.setAddress((String) employeeData.get("address"));
            newEmployee.setEmergencyContactName((String) employeeData.get("emergencyContactName"));
            newEmployee.setEmergencyContactRelation((String) employeeData.get("emergencyContactRelation"));
            newEmployee.setEmergencyContactPhone((String) employeeData.get("emergencyContactPhone"));
            // Set role - default to EMPLOYEE if not specified
            String roleStr = (String) employeeData.getOrDefault("role", "EMPLOYEE");
            newEmployee.setRole(Employee.Role.valueOf(roleStr));
            // Set active status
            newEmployee.setActive(true);

            // Assign building before saving
            newEmployee.setBuilding(buildingOpt.get());

            // Generate a secure temporary password
            String tempPassword = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "!";
            newEmployee.setPassword(tempPassword); // Will be hashed by service
            newEmployee.setMustChangePassword(true);

            // Create employee and persist
            Employee saved = employeeService.createEmployee(newEmployee, true);

            // Assign department if provided
            if (employeeData.containsKey("departmentId")) {
                Long deptId = Long.valueOf(employeeData.get("departmentId").toString());
                employeeService.assignEmployeeToDepartment(saved, deptId);
            }

            // Send registration email with temp password
            notificationService.sendEmployeeRegistrationEmail(saved, tempPassword);

            userActionLogService.logAction("CREATED_EMPLOYEE", currentUser.getId());
            return ResponseEntity.ok(EmployeeDTO.fromEntity(saved));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_CREATE_EMPLOYEE", currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/departments/{departmentId}/assign")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> assignEmployeesToDepartment(@PathVariable Long departmentId,
                                                        @RequestBody Map<String, List<Long>> request,
                                                        @AuthenticationPrincipal Employee currentUser) {
        try {
            List<Long> employeeIds = request.get("employeeIds");
            if (employeeIds == null || employeeIds.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("No employee IDs provided"));
            }
            employeeService.assignEmployeesToDepartment(employeeIds, departmentId);
            userActionLogService.logAction("ASSIGNED_EMPLOYEES_TO_DEPARTMENT", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Employees assigned to department successfully"));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_ASSIGN_EMPLOYEES_TO_DEPARTMENT", currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Change password endpoint for employees, managers, and admins.
     * Requires old password, new password. Sets mustChangePassword=false on success.
     * Exception: If user has mustChangePassword=true, old password is not required.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> req,
                                            @AuthenticationPrincipal Employee currentUser) {
        String oldPassword = req.get("oldPassword");
        String newPassword = req.get("newPassword");
        
        if (newPassword == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Missing new password"));
        }
        
        // If user must change password, skip old password check
        if (currentUser.isMustChangePassword()) {
            employeeService.updatePassword(currentUser, newPassword);
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        }
        
        // Normal password change requires old password
        if (oldPassword == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Missing old password"));
        }
        if (!employeeService.checkPassword(currentUser, oldPassword)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Old password is incorrect"));
        }
        employeeService.updatePassword(currentUser, newPassword);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    /**
     * Sends a new temporary password to the employee's email and sets mustChangePassword=true.
     * Endpoint: POST /api/employees/{id}/send-temp-password
     * Only accessible by MANAGER or ADMIN.
     */
    @PostMapping("/{id}/send-temp-password")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> sendTempPasswordToEmployee(@PathVariable Long id, @RequestBody Map<String, String> req,
                                                       @AuthenticationPrincipal Employee currentUser) {
        try {
            Employee employee = employeeService.getEmployeeById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            assertAdminForBuilding(employee.getBuilding().getId(), currentUser);
            // Generate new temp password
            String tempPassword = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "!";
            employeeService.updatePassword(employee, tempPassword);
            employee.setMustChangePassword(true);
            employeeService.updateEmployee(employee);
            notificationService.sendEmployeeRegistrationEmail(employee, tempPassword);
            userActionLogService.logAction("SENT_TEMP_PASSWORD", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Temporary password sent to employee's email."));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_SEND_TEMP_PASSWORD", currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    // Helper class for response messages
    public static class MessageResponse {
        private String message;
        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
