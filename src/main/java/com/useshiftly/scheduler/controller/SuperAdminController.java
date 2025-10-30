package com.useshiftly.scheduler.controller;



import com.useshiftly.scheduler.model.Employee;
import com.useshiftly.scheduler.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * Sets a temporary password for the admin and sets mustChangePassword=true, but does NOT send an email.
     * Endpoint: POST /api/super-admin/admins/{id}/set-temp-password-manual
     * Body: { "tempPassword": "..." }
     */
    @PostMapping("/{id}/set-temp-password-manual")
    public ResponseEntity<?> setTempPasswordManual(@PathVariable Long id, @RequestBody Map<String, String> req) {
        Employee admin = employeeService.getAdminById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        String tempPassword = req.get("tempPassword");
        if (tempPassword == null || tempPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Temp password is required."));
        }
        admin.setPassword(employeeService.encodePassword(tempPassword));
        admin.setMustChangePassword(true);
        employeeService.updateEmployee(admin);
        return ResponseEntity.ok(Map.of("message", "Temporary password set for admin (manual, no email sent)."));
    }
    /**
     * Sends a temporary password to the admin's email and sets mustChangePassword=true.
     * Endpoint: POST /api/super-admin/admins/{id}/send-temp-password
     * Body: { "tempPassword": "..." }
     */
    @PostMapping("/{id}/send-temp-password")
    public ResponseEntity<?> sendTempPasswordToAdmin(@PathVariable Long id, @RequestBody Map<String, String> req) {
        Employee admin = employeeService.getAdminById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        String tempPassword = req.get("tempPassword");
        if (tempPassword == null || tempPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Temp password is required."));
        }
        admin.setPassword(employeeService.encodePassword(tempPassword));
        admin.setMustChangePassword(true);
        employeeService.updateEmployee(admin);
        // Send email to admin
        String subject = "Your Temporary Admin Password";
        String body = String.format(
                "Hello %s,\n\nA new temporary password has been set for your admin account.\n\nTemporary password: %s\n\nYou will be required to change your password on your next login.\n\nIf you did not request this, please contact your super admin immediately.\n\nBest regards,\nUseShiftly Team",
                admin.getFirstName(), tempPassword);
        employeeService.sendEmailToAdmin(admin, subject, body);
        return ResponseEntity.ok(Map.of("message", "Temporary password sent to admin email."));
    }
    /**
     * Send Stripe invoice to all admins.
     */
    @PostMapping("/invoice/send-all")
    public ResponseEntity<?> sendInvoiceToAllAdmins() {
        List<Employee> admins = employeeService.getAllAdmins();
        int sent = 0;
        for (Employee admin : admins) {
            try {
                employeeService.sendStripeInvoice(admin);
                sent++;
            } catch (Exception e) {
                // Log error, continue
            }
        }
        return ResponseEntity.ok(Map.of("message", "Invoices sent to " + sent + " admins."));
    }

    /**
     * Send Stripe invoice to a specific admin.
     */
    @PostMapping("/invoice/send/{id}")
    public ResponseEntity<?> sendInvoiceToAdmin(@PathVariable Long id) {
        Employee admin = employeeService.getAdminById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        employeeService.sendStripeInvoice(admin);
        return ResponseEntity.ok(Map.of("message", "Invoice sent to admin " + id));
    }
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
        admin.setFirstName(req.get("firstName"));
        admin.setLastName(req.get("lastName"));
        admin.setEmail(req.get("email"));
        admin.setRole(Employee.Role.ADMIN);
        admin.setActive(true);
        // Set password and other fields as needed
        if (req.containsKey("packageType") && req.get("packageType") != null && !req.get("packageType").isEmpty()) {
            admin.setPackageType(req.get("packageType"));
        } else {
            admin.setPackageType("Basic");
        }
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
        if (req.containsKey("firstName")) admin.setFirstName(req.get("firstName"));
        if (req.containsKey("lastName")) admin.setLastName(req.get("lastName"));
        if (req.containsKey("email")) admin.setEmail(req.get("email"));
        if (req.containsKey("packageType")) admin.setPackageType(req.get("packageType"));
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
