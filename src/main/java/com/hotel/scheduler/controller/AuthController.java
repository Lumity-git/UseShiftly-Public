package com.hotel.scheduler.controller;

import com.hotel.scheduler.dto.auth.LoginRequest;
import com.hotel.scheduler.dto.auth.RegisterRequest;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.model.Department;
import com.hotel.scheduler.repository.DepartmentRepository;
import com.hotel.scheduler.security.JwtUtils;
import com.hotel.scheduler.service.EmployeeService;
import com.hotel.scheduler.service.InvitationService;
import com.hotel.scheduler.model.Invitation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.io.FileWriter;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    /**
     * Registers a new building admin and building.
     * Endpoint: POST /api/auth/register-admin
     * Request: AdminRegisterRequest (JSON)
     * Response: Success or error message
     */
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(
            @Valid @RequestBody com.hotel.scheduler.dto.auth.AdminRegisterRequest request) {
        try {
            if (employeeService.getEmployeeByEmail(request.getOwnerEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already taken!"));
            }
            // Create new admin employee
            Employee admin = new Employee();
            admin.setEmail(request.getOwnerEmail());
            admin.setPassword(request.getPassword());
            admin.setFirstName(request.getOwnerName());
            admin.setLastName("");
            admin.setRole(Employee.Role.ADMIN);
            // Create building and assign admin
            var building = new com.hotel.scheduler.model.Building();
            building.setName(request.getBuildingName());
            building.setAddress(request.getBuildingAddress());
            building.setAdmin(admin);
            // Save admin first so it has an ID (if needed by building)
            employeeService.createEmployeeWithUserPassword(admin);
            // Persist building to generate unique ID
            com.hotel.scheduler.model.Building savedBuilding = buildingRepository.save(building);
            // Assign building to admin (if Employee has a building field)
            admin.setBuilding(savedBuilding);
            employeeService.updateEmployee(admin); // Save admin with building assigned
            log.info("Created building: {} (ID: {}) for admin: {}", savedBuilding.getName(), savedBuilding.getId(), admin.getEmail());
            return ResponseEntity.ok(new MessageResponse("Admin and building registered successfully! Building ID: " + savedBuilding.getId()));
        } catch (Exception e) {
            log.error("Admin registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Checks if an employee with the specified email is registered.
     *
     * @param email the email address to check for registration
     * @return a ResponseEntity containing a map with a boolean value under the key "registered"
     *         indicating whether the email is already registered
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean registered = employeeService.getEmployeeByEmail(email).isPresent();
        return ResponseEntity.ok(java.util.Map.of("registered", registered));
    }

    /**
     * Allows an authenticated user to change their password.
     * Endpoint: POST /api/auth/change-password
     * Request: { newPassword }
     * Response: Success or error message
     */
    /**
     * Allows password change via invitation code/token (for password reset links).
     * Accepts: { code, token, newPassword }
     * If code/token are present, validates invitation and updates password for the
     * invited user.
     * If authenticated, allows normal password change.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal Employee employee,
            @Valid @RequestBody com.hotel.scheduler.dto.auth.ChangePasswordRequest request) {
        String newPassword = request.getNewPassword();
        String code = request.getCode();
        String token = request.getToken();
        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};':\\\"\\\\|,.<>/?-]).{8,}$";
        if (!java.util.regex.Pattern.matches(passwordPattern, newPassword)) {
            return ResponseEntity.badRequest().body(new MessageResponse(
                    "Password must be at least 8 characters, include 1 uppercase letter, 1 number, and 1 special character."));
        }
        // If code/token are provided, handle password reset via invitation
        if (code != null && token != null) {
            log.info("[DEBUG] Password reset attempt with code={}, token={}", code, token);
            var invitationOpt = invitationService.validateInvitation(code, token);
            if (invitationOpt.isEmpty()) {
                log.warn("[DEBUG] Password reset failed - Invalid or expired invitation: code={}, token={}", code, token);
                return ResponseEntity.status(401).body(new MessageResponse("Invalid or expired password reset link."));
            }
            Invitation invitation = invitationOpt.get();
            log.info("[DEBUG] Valid invitation found for email={}, type={}, expiresAt={}", 
                invitation.getEmail(), invitation.getRole(), invitation.getExpiresAt());
            // Find employee by email from invitation
            var employeeOpt = employeeService.getEmployeeByEmail(invitation.getEmail());
            if (employeeOpt.isEmpty()) {
                log.warn("[DEBUG] Password reset failed - User not found for email={}", invitation.getEmail());
                return ResponseEntity.status(404).body(new MessageResponse("User not found for this invitation."));
            }
            Employee invitedEmployee = employeeOpt.get();
            log.info("[DEBUG] Found employee for password reset: id={}, email={}", 
                invitedEmployee.getId(), invitedEmployee.getEmail());
            employeeService.updatePassword(invitedEmployee, newPassword);
            invitationService.markInvitationUsed(code); // Mark/reset link as used
            userActionLogService.logAction("CHANGE_PASSWORD_BY_LINK", invitedEmployee.getId());
            log.info("[DEBUG] Password reset successful for employee id={}", invitedEmployee.getId());
            return ResponseEntity
                    .ok(new MessageResponse("Password changed successfully! The reset link is now invalid."));
        }
        // Check if user is authenticated
        if (employee == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Not authenticated"));
        }
        // Allow password change for authenticated users (especially those with mustChangePassword=true)
        employeeService.updatePassword(employee, newPassword);
        userActionLogService.logAction("CHANGE_PASSWORD", employee.getId());
        return ResponseEntity.ok(new MessageResponse("Password changed successfully!"));
    }

    private final com.hotel.scheduler.service.UserActionLogService userActionLogService;

    /**
     * Returns the current authenticated user's info, including department.
     * Endpoint: GET /api/auth/me
     * Response: { email, firstName, lastName, role, departmentId, departmentName }
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Employee employee) {
        if (employee == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Not authenticated"));
        }
        Long departmentId = null;
        String departmentName = null;
        if (employee.getDepartment() != null) {
            departmentId = employee.getDepartment().getId();
            departmentName = employee.getDepartment().getName();
        }
        Long buildingId = null;
        String buildingName = null;
        if (employee.getBuilding() != null) {
            try {
                buildingId = employee.getBuilding().getId();
                buildingName = employee.getBuilding().getName();
            } catch (Exception e) {
                log.warn("Could not initialize building for employee: " + e.getMessage());
            }
        }
        return ResponseEntity.ok(java.util.Map.of(
                "id", employee.getId(),
                "email", employee.getEmail(),
                "firstName", employee.getFirstName(),
                "lastName", employee.getLastName(),
                "role", employee.getRole().name(),
                "departmentId", departmentId,
                "departmentName", departmentName,
                "buildingId", buildingId,
                "buildingName", buildingName));
    }

    private final AuthenticationManager authenticationManager;
    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;
    private final com.hotel.scheduler.repository.BuildingRepository buildingRepository;
    private final JwtUtils jwtUtils;
    private final InvitationService invitationService;
    /**
     * Authenticates a user and returns a JWT token if credentials are valid.
     * Endpoint: POST /api/auth/login
     * Request: LoginRequest (email, password)
     * Response: JWT token and user info
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Employee employee = (Employee) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(employee);
            // Get building info if available
            Long buildingId = null;
            String buildingName = null;
            if (employee.getBuilding() != null) {
                buildingId = employee.getBuilding().getId();
                buildingName = employee.getBuilding().getName();
            }
            // Add mustChangePassword and building info to the response
            String responseJson = String.format(
                    "{\"token\":\"%s\",\"id\":%d,\"type\":\"Bearer\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"role\":\"%s\",\"mustChangePassword\":%s,\"buildingId\":%s,\"buildingName\":%s}",
                    jwt,
                    employee.getId(),
                    employee.getEmail(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getRole().name(),
                    employee.isMustChangePassword() ? "true" : "false",
                    buildingId == null ? "null" : buildingId.toString(),
                    buildingName == null ? "null" : String.format("\"%s\"", buildingName));
            userActionLogService.logAction("LOGIN_SUCCESS", employee.getId());
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(responseJson);
        } catch (Exception e) {
            userActionLogService.logAction("LOGIN_FAILED", null);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid credentials!"));
        }
    }

    /**
     * Registers a new user using an invitation code and token.
     * Endpoint: POST /api/auth/register
     * Request: RegisterRequest (form data)
     * Response: Success or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @ModelAttribute RegisterRequest signUpRequest) {
        try {
            String code = signUpRequest.getInvitationCode();
            String token = signUpRequest.getInvitationToken();
            if (code == null || token == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Missing invitation code or token."));
            }
            var invitationOpt = invitationService.validateInvitation(code, token);
            if (invitationOpt.isEmpty()) {
                return ResponseEntity.status(401).body(new MessageResponse("Invalid or expired invitation."));
            }
            Invitation invitation = invitationOpt.get();
            if (employeeService.getEmployeeByEmail(signUpRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Email is already taken!"));
            }
            // Enforce multi-tenant: invited user must be assigned to a department/building
            // owned by the inviting admin
            Department department = null;
            if (signUpRequest.getDepartmentId() != null) {
                department = departmentRepository.findById(signUpRequest.getDepartmentId()).orElse(null);
                if (department == null) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Invalid department for registration."));
                }
                // Check that the department's building admin matches the invitation's admin (if
                // present)
                if (invitation.getAdminId() != null && department.getBuilding() != null
                        && department.getBuilding().getAdmin() != null
                        && !department.getBuilding().getAdmin().getId().equals(invitation.getAdminId())) {
                    return ResponseEntity.status(403)
                            .body(new MessageResponse("Forbidden: Department does not belong to inviting admin."));
                }
            }
            // Create new employee
            Employee employee = new Employee();
            employee.setEmail(signUpRequest.getEmail());
            employee.setPassword(signUpRequest.getPassword());
            employee.setFirstName(signUpRequest.getFirstName());
            employee.setLastName(signUpRequest.getLastName());
            employee.setPhoneNumber(
                    signUpRequest.getPhoneNumber() != null ? signUpRequest.getPhoneNumber().trim() : null);
            employee.setRole(Employee.Role.valueOf(invitation.getRole()));
            employee.setDateOfBirth(signUpRequest.getDateOfBirth());
            employee.setAddress(signUpRequest.getAddress());
            employee.setEmergencyContactName(signUpRequest.getEmergencyContactName());
            employee.setEmergencyContactRelation(signUpRequest.getEmergencyContactRelation());
            employee.setEmergencyContactPhone(signUpRequest.getEmergencyContactPhone());
            if (department != null) {
                employee.setDepartment(department);
            }
            employeeService.createEmployee(employee, false);
            invitationService.markInvitationUsed(code);
            userActionLogService.logAction("REGISTER_SUCCESS", employee.getId());
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            userActionLogService.logAction("REGISTER_FAILED", null);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Validates an invitation code and token.
     * Endpoint: GET /api/auth/validate-invitation
     * Request params: code, token
     * Response: Invitation details or error
     */
    @GetMapping("/validate-invitation")
    public ResponseEntity<?> validateInvitation(@RequestParam String code, @RequestParam String token) {
        try {
            var invitationOpt = invitationService.validateInvitation(code, token);
            if (invitationOpt.isEmpty()) {
                return ResponseEntity.status(401).body(new MessageResponse("Invalid or expired invitation."));
            }
            Invitation invitation = invitationOpt.get();
            return ResponseEntity.ok(java.util.Map.of(
                    "valid", true,
                    "email", invitation.getEmail(),
                    "role", invitation.getRole(),
                    "departmentName", invitation.getDepartmentName(),
                    "invitedBy", invitation.getInvitedBy(),
                    "invitationDate", invitation.getCreatedAt(),
                    "hotelName", "Grand Hotel"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid invitation"));
        }
    }

    /**
     * Generates a new invitation for employee registration (manager/admin only).
     * Endpoint: POST /api/auth/generate-invitation
     * Request: departmentId, email, role, departmentName
     * Response: Invitation code and token
     */
    @PostMapping("/generate-invitation")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> generateInvitation(@AuthenticationPrincipal Employee currentUser,
            @RequestBody java.util.Map<String, Object> request) {
        try {
            String departmentId = request.get("departmentId") != null ? request.get("departmentId").toString() : null;
            String invitationCode = "INV-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String invitationToken = java.util.UUID.randomUUID().toString();

            Long buildingId = null;
            String buildingName = null;
            String type = request.get("type") != null ? request.get("type").toString() : null;

            // Defensive: log incoming request for debugging
            log.info("generateInvitation request: type={}, payload={}", type, request);

            // Defensive: check required fields for PASSWORD_RESET
            if ("PASSWORD_RESET".equals(type)) {
                String email = (String) request.get("email");
                if (email == null || email.trim().isEmpty()) {
                    log.warn("Password reset invitation missing email");
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Email is required for password reset invitation."));
                }
                // Optionally check employeeId if needed
            }

            // Manager: assign their building automatically
            if (currentUser.getRole().name().equals("MANAGER")) {
                if (currentUser.getBuilding() == null) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Manager is not assigned to any building."));
                }
                buildingId = currentUser.getBuilding().getId();
                buildingName = currentUser.getBuilding().getName();
            }
            // Admin: require buildingId/buildingName in request, validate ownership
            else if (currentUser.getRole().name().equals("ADMIN")) {
                // Only require building for non-password-reset invitations
                if (!"PASSWORD_RESET".equals(type)) {
                    if (request.get("buildingId") == null || request.get("buildingName") == null) {
                        return ResponseEntity.badRequest()
                                .body(new MessageResponse("Admin must select a building to assign."));
                    }
                    buildingId = Long.valueOf(request.get("buildingId").toString());
                    buildingName = request.get("buildingName").toString();
                    // TODO: Optionally validate admin owns this building
                }
            }

            // Defensive: ensure all required fields are present for invitation
            String email = (String) request.getOrDefault("email", "");
            String role = (String) request.getOrDefault("role", "EMPLOYEE");
            String departmentName = (String) request.getOrDefault("departmentName", "");
            String invitedBy = currentUser.getFirstName() + " " + currentUser.getLastName();

            if (email == null || email.trim().isEmpty()) {
                log.warn("Invitation creation failed: missing email");
                return ResponseEntity.badRequest().body(new MessageResponse("Email is required."));
            }

            Invitation invitation = Invitation.builder()
                    .code(invitationCode)
                    .token(invitationToken)
                    .email(email)
                    .role(role)
                    .departmentName(departmentName)
                    .invitedBy(invitedBy)
                    .buildingId(buildingId)
                    .buildingName(buildingName)
                    .build();
            try {
                invitationService.createInvitation(invitation);
            } catch (Exception ex) {
                log.error("Error creating invitation: {}", ex.getMessage(), ex);
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Failed to create invitation: " + ex.getMessage()));
            }
            userActionLogService.logAction("GENERATED_INVITATION", currentUser.getId());
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("invitationCode", invitationCode);
            response.put("invitationToken", invitationToken);
            response.put("expiresIn", "7 days");
            response.put("createdBy", invitedBy);
            // Only add departmentId, buildingId, buildingName if not null
            if (departmentId != null)
                response.put("departmentId", departmentId);
            if (buildingId != null)
                response.put("buildingId", buildingId);
            if (buildingName != null)
                response.put("buildingName", buildingName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Exception in generateInvitation: {}", e.getMessage(), e);
            userActionLogService.logAction("FAILED_GENERATE_INVITATION", currentUser.getId());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Validates the JWT token for the current user.
     * Endpoint: GET /api/auth/validate
     * Response: Success or error message
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@AuthenticationPrincipal Employee employee) {
        if (employee != null) {
            Long buildingId = null;
            String buildingName = null;
            if (employee.getBuilding() != null) {
                try {
                    buildingId = employee.getBuilding().getId();
                    buildingName = employee.getBuilding().getName();
                } catch (Exception e) {
                    log.warn("Could not initialize building for employee: " + e.getMessage());
                }
            }
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", employee.getId());
            response.put("email", employee.getEmail());
            response.put("firstName", employee.getFirstName());
            response.put("lastName", employee.getLastName());
            response.put("role", employee.getRole().name());
            response.put("mustChangePassword", employee.isMustChangePassword());
            response.put("buildingId", buildingId);
            response.put("buildingName", buildingName);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid token"));
    }

    /**
     * Refreshes a JWT token for an authenticated user.
     * Endpoint: POST /api/auth/refresh
     * Response: New JWT token and user info
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@AuthenticationPrincipal Employee employee) {
        try {
            java.io.File logDir = new java.io.File("logs");
            if (!logDir.exists()) logDir.mkdirs();
            FileWriter fw = new FileWriter("logs/auth-refresh.log", true);
            if (employee == null) {
                String msg = "/auth/refresh called but @AuthenticationPrincipal is null. Likely due to expired or invalid JWT.";
                log.warn(msg);
                fw.write(msg + "\n");
                fw.close();
                return ResponseEntity.status(401).body(new MessageResponse("Invalid token: no principal. JWT may be expired or invalid."));
            }

            String infoMsg = String.format("/auth/refresh called for employee id %d email %s", employee.getId(), employee.getEmail());
            log.info(infoMsg);
            fw.write(infoMsg + "\n");
            // Generate new JWT token
            String jwt = jwtUtils.generateTokenFromUsername(employee.getEmail());

            // Get building info
            Long buildingId = null;
            String buildingName = null;
            if (employee.getBuilding() != null) {
                try {
                    buildingId = employee.getBuilding().getId();
                    buildingName = employee.getBuilding().getName();
                } catch (Exception e) {
                    String warnMsg = "Could not initialize building for employee during refresh: " + e.getMessage();
                    log.warn(warnMsg);
                    fw.write(warnMsg + "\n");
                }
            }

            // Return refreshed token and user info
            String responseJson = String.format(
                    "{\"token\":\"%s\",\"id\":%d,\"type\":\"Bearer\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"role\":\"%s\",\"mustChangePassword\":%s,\"buildingId\":%s,\"buildingName\":%s}",
                    jwt,
                    employee.getId(),
                    employee.getEmail(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getRole().name(),
                    employee.isMustChangePassword() ? "true" : "false",
                    buildingId == null ? "null" : buildingId.toString(),
                    buildingName == null ? "null" : String.format("\"%s\"", buildingName));

            userActionLogService.logAction("TOKEN_REFRESH", employee.getId());
            fw.write("Token refresh successful for employee id " + employee.getId() + "\n");
            fw.close();
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(responseJson);
        } catch (Exception e) {
            log.error("Token refresh failed: " + e.getMessage(), e);
            try {
                FileWriter fw = new FileWriter("logs/auth-refresh.log", true);
                fw.write("Token refresh failed: " + e.getMessage() + "\n");
                fw.close();
            } catch (IOException ioe) {
                log.error("Failed to write to auth-refresh.log: " + ioe.getMessage(), ioe);
            }
            return ResponseEntity.status(401).body(new MessageResponse("Token refresh failed: " + e.getMessage()));
        }
    }

    /**
     * Lists all active (not used, not expired) invitations (manager/admin only).
     * Endpoint: GET /api/auth/active-invitations
     * Response: List of active invitations
     */
    @GetMapping("/active-invitations")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getActiveInvitations() {
        try {
            var invitations = invitationService.getActiveInvitations();
            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Deletes an invitation by code (manager/admin only).
     * Endpoint: DELETE /api/auth/delete-invitation/{code}
     * Response: Success or error message
     */
    @DeleteMapping("/delete-invitation/{code}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteInvitation(@PathVariable String code,
            @AuthenticationPrincipal Employee currentUser) {
        try {
            boolean deleted = invitationService.deleteInvitationByCode(code);
            if (deleted) {
                userActionLogService.logAction("DELETED_INVITATION", currentUser.getId());
                return ResponseEntity.ok(new MessageResponse("Invitation deleted successfully."));
            } else {
                userActionLogService.logAction("FAILED_DELETE_INVITATION", currentUser.getId());
                return ResponseEntity.status(404)
                        .body(new MessageResponse("Invitation not found or already used/expired."));
            }
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_DELETE_INVITATION", currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Helper class for response messages (used for error/success responses).
     */
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
    /**
     * AuthController handles authentication, registration, and invitation
     * management for hotel employees.
     * 
     * Key Endpoints:
     * - /api/auth/login: Authenticate and get JWT
     * - /api/auth/register: Register new user with invitation
     * - /api/auth/validate-invitation: Validate invitation code/token
     * - /api/auth/generate-invitation: Manager/Admin creates invitation
     * - /api/auth/validate: Validate JWT token
     * - /api/auth/active-invitations: List active invitations
     * - /api/auth/delete-invitation/{code}: Delete invitation by code
     * 
     * Security:
     * - All endpoints except /api/auth/login and /api/auth/register require
     * authentication
     * - Role-based access for invitation management
     * 
     * Dependencies:
     * - EmployeeService: User management
     * - InvitationService: Invitation logic
     * - JwtUtils: JWT token generation/validation
     * - DepartmentRepository: Department lookup
     */
    // ...existing code...

}
