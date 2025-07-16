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
            // Create response manually to avoid serialization issues
            String responseJson = String.format(
                "{\"token\":\"%s\",\"type\":\"Bearer\",\"email\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\",\"role\":\"%s\"}",
                jwt,
                employee.getEmail(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getRole().name()
            );
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(responseJson);
        } catch (Exception e) {
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
            // Set department if provided
            if (signUpRequest.getDepartmentId() != null) {
                Department department = departmentRepository.findById(signUpRequest.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
                employee.setDepartment(department);
            }
            employeeService.createEmployee(employee);
            invitationService.markInvitationUsed(code);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
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
            return ResponseEntity.ok(java.util.Map.of(
                "invitationCode", invitationCode,
                "invitationToken", invitationToken,
                "expiresIn", "7 days",
                "departmentId", departmentId,
                "createdBy", currentUser.getFirstName() + " " + currentUser.getLastName()
            ));
        } catch (Exception e) {
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
    public ResponseEntity<?> deleteInvitation(@PathVariable String code) {
        try {
            boolean deleted = invitationService.deleteInvitationByCode(code);
            if (deleted) {
                return ResponseEntity.ok(new MessageResponse("Invitation deleted successfully."));
            } else {
                return ResponseEntity.status(404).body(new MessageResponse("Invitation not found or already used/expired."));
            }
        } catch (Exception e) {
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
