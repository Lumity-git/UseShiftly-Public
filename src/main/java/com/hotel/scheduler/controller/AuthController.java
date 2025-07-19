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
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AuthController {
    /**
     * Allows an authenticated user to change their password.
     * Endpoint: POST /api/auth/change-password
     * Request: { newPassword }
     * Response: Success or error message
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal Employee employee,
                                            @Valid @RequestBody com.hotel.scheduler.dto.auth.ChangePasswordRequest request) {
        if (employee == null) {
            return ResponseEntity.status(401).body(new MessageResponse("Not authenticated"));
        }
        String newPassword = request.getNewPassword();
        // Password policy: 1 special char, 1 uppercase, 1 number, min 8 chars
        // Fixed regex: properly escape special characters and brackets
        // Fixed regex: dash moved to the end, all special chars escaped, no unclosed class
        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};':\\\"\\\\|,.<>/?-]).{8,}$";
        if (!java.util.regex.Pattern.matches(passwordPattern, newPassword)) {
            return ResponseEntity.badRequest().body(new MessageResponse(
                "Password must be at least 8 characters, include 1 uppercase letter, 1 number, and 1 special character."));
        }
        employeeService.updatePassword(employee, newPassword);
        userActionLogService.logAction("CHANGE_PASSWORD", employee);
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
        return ResponseEntity.ok(java.util.Map.of(
            "email", employee.getEmail(),
            "firstName", employee.getFirstName(),
            "lastName", employee.getLastName(),
            "role", employee.getRole().name(),
            "departmentId", departmentId,
            "departmentName", departmentName
        ));
    }
    
    private final AuthenticationManager authenticationManager;
    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;
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
            String jwt = jwtUtils.generateJwtToken((Employee) authentication.getPrincipal());
            Employee employee = (Employee) authentication.getPrincipal();
            // Add mustChangePassword to the response
            String responseJson = String.format(
                "{\"token\":\"%s\",\"type\":\"Bearer\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"role\":\"%s\",\"mustChangePassword\":%s}",
                jwt,
                employee.getEmail(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getRole().name(),
                employee.isMustChangePassword() ? "true" : "false"
            );
            userActionLogService.logAction("LOGIN_SUCCESS", employee);
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
            // Create new employee
            Employee employee = new Employee();
            employee.setEmail(signUpRequest.getEmail());
            employee.setPassword(signUpRequest.getPassword());
            employee.setFirstName(signUpRequest.getFirstName());
            employee.setLastName(signUpRequest.getLastName());
            employee.setPhoneNumber(signUpRequest.getPhoneNumber());
            employee.setRole(Employee.Role.valueOf(invitation.getRole()));
            employee.setDateOfBirth(signUpRequest.getDateOfBirth());
            employee.setAddress(signUpRequest.getAddress());
            employee.setEmergencyContactName(signUpRequest.getEmergencyContactName());
            employee.setEmergencyContactRelation(signUpRequest.getEmergencyContactRelation());
            employee.setEmergencyContactPhone(signUpRequest.getEmergencyContactPhone());
            // Set department if provided
            if (signUpRequest.getDepartmentId() != null) {
                Department department = departmentRepository.findById(signUpRequest.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
                employee.setDepartment(department);
            }
            employeeService.createEmployee(employee, false);
            invitationService.markInvitationUsed(code);
            userActionLogService.logAction("REGISTER_SUCCESS", employee);
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
                "hotelName", "Grand Hotel"
            ));
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
            String departmentId = request.get("departmentId").toString();
            String invitationCode = "INV-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String invitationToken = java.util.UUID.randomUUID().toString();
            // Persist invitation to database
            Invitation invitation = Invitation.builder()
                .code(invitationCode)
                .token(invitationToken)
                .email((String) request.getOrDefault("email", ""))
                .role((String) request.getOrDefault("role", "EMPLOYEE"))
                .departmentName((String) request.getOrDefault("departmentName", ""))
                .invitedBy(currentUser.getFirstName() + " " + currentUser.getLastName())
                .build();
            invitationService.createInvitation(invitation);
            userActionLogService.logAction("GENERATED_INVITATION", currentUser);
            return ResponseEntity.ok(java.util.Map.of(
                "invitationCode", invitationCode,
                "invitationToken", invitationToken,
                "expiresIn", "7 days",
                "departmentId", departmentId,
                "createdBy", currentUser.getFirstName() + " " + currentUser.getLastName()
            ));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_GENERATE_INVITATION", currentUser);
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
            return ResponseEntity.ok(new MessageResponse("Token is valid"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid token"));
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
    public ResponseEntity<?> deleteInvitation(@PathVariable String code, @AuthenticationPrincipal Employee currentUser) {
        try {
            boolean deleted = invitationService.deleteInvitationByCode(code);
            if (deleted) {
                userActionLogService.logAction("DELETED_INVITATION", currentUser);
                return ResponseEntity.ok(new MessageResponse("Invitation deleted successfully."));
            } else {
                userActionLogService.logAction("FAILED_DELETE_INVITATION", currentUser);
                return ResponseEntity.status(404).body(new MessageResponse("Invitation not found or already used/expired."));
            }
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_DELETE_INVITATION", currentUser);
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
     * AuthController handles authentication, registration, and invitation management for hotel employees.
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
     * - All endpoints except /api/auth/login and /api/auth/register require authentication
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
