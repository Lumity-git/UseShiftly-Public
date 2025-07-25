package com.hotel.scheduler.controller;

import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.security.JwtUtils;
import com.hotel.scheduler.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/super-admin/auth")
@RequiredArgsConstructor
public class SuperAdminAuthController {
    private final AuthenticationManager authenticationManager;
    private final EmployeeService employeeService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> superAdminLogin(@Valid @RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Employee employee = (Employee) authentication.getPrincipal();
            System.out.println("[DEBUG] SuperAdmin login attempt: email=" + employee.getEmail() + ", role=" + employee.getRole());
            if (employee.getRole() != Employee.Role.SUPER_ADMIN) {
                System.out.println("[DEBUG] Not a super admin: " + employee.getEmail() + ", role=" + employee.getRole());
                return ResponseEntity.status(403).body(Map.of("error", "Not a super admin"));
            }
            String jwt = jwtUtils.generateJwtToken(employee);
            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "type", "Bearer",
                    "email", employee.getEmail(),
                    "firstName", employee.getFirstName(),
                    "lastName", employee.getLastName(),
                    "role", employee.getRole().name()
            ));
        } catch (Exception e) {
            System.out.println("[DEBUG] SuperAdmin login failed for email=" + email + ": " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }
}
