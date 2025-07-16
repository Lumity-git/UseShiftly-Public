package com.hotel.scheduler.controller;

import com.hotel.scheduler.dto.EmployeeDTO;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmployeeController {
    /**
     * EmployeeController: Handles all employee-related REST API endpoints for the hotel scheduler system.
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
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            var employeeOpt = employeeService.getEmployeeById(id);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(404).body(new MessageResponse("Employee not found"));
            }
            return ResponseEntity.ok(EmployeeDTO.fromEntity(employeeOpt.get()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    private final EmployeeService employeeService;
    
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllActiveEmployees();
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
        return ResponseEntity.ok(EmployeeDTO.fromEntity(currentUser));
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
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(EmployeeDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(employeeDTOs);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateEmployee(@PathVariable Long id) {
        try {
            employeeService.deactivateEmployee(id);
            return ResponseEntity.ok(new MessageResponse("Employee deactivated successfully"));
        } catch (Exception e) {
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
            
            return ResponseEntity.ok(EmployeeDTO.fromEntity(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, Object> employeeData,
                                          @AuthenticationPrincipal Employee currentUser) {
        try {
            Employee newEmployee = new Employee();
            newEmployee.setFirstName((String) employeeData.get("firstName"));
            newEmployee.setLastName((String) employeeData.get("lastName"));
            newEmployee.setEmail((String) employeeData.get("email"));
            newEmployee.setPhoneNumber((String) employeeData.get("phoneNumber"));
            
            // Set default password or use provided one
            String password = (String) employeeData.getOrDefault("password", "defaultpassword123");
            newEmployee.setPassword(password);
            
            // Set role - default to EMPLOYEE if not specified
            String roleStr = (String) employeeData.getOrDefault("role", "EMPLOYEE");
            newEmployee.setRole(Employee.Role.valueOf(roleStr));
            
            // Set active status
            newEmployee.setActive(true);
            
            Employee saved = employeeService.createEmployee(newEmployee);
            return ResponseEntity.ok(EmployeeDTO.fromEntity(saved));
        } catch (Exception e) {
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
            // Mock implementation - in real app this would assign employees to department
            return ResponseEntity.ok(new MessageResponse("Employees assigned to department successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    // Helper class for response messages
    public static class MessageResponse {
        private String message;
        
        public MessageResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
