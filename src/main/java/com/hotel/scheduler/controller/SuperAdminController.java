package com.hotel.scheduler.controller;

import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * SuperAdminController: Only for managing admin accounts.
 * Super-admin cannot access or modify any regular user/building/department data.
 */
@RestController
@RequestMapping("/api/super-admin/admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {
    private final EmployeeService employeeService;

    /**
     * List all admin accounts.
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllAdmins() {
        List<Employee> admins = employeeService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * Create a new admin account.
     */
    @PostMapping
    public ResponseEntity<?> createAdmin(@RequestBody Map<String, String> req) {
        Employee admin = new Employee();
        admin.firstName = req.get("firstName");
        admin.lastName = req.get("lastName");
        admin.email = req.get("email");
        admin.role = Employee.Role.ADMIN;
        admin.active = true;
        // Set password and other fields as needed
        Employee saved = employeeService.createAdmin(admin, req.get("password"));
        return ResponseEntity.ok(saved);
    }

    /**
     * Update an admin account.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @RequestBody Map<String, String> req) {
        Employee admin = employeeService.getAdminById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if (req.containsKey("firstName")) admin.firstName = req.get("firstName");
        if (req.containsKey("lastName")) admin.lastName = req.get("lastName");
        if (req.containsKey("email")) admin.email = req.get("email");
        Employee updated = employeeService.updateEmployee(admin);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete an admin account.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        employeeService.deleteAdmin(id);
        return ResponseEntity.ok(Map.of("message", "Admin deleted"));
    }
}
