package com.hotel.scheduler.controller;

import com.hotel.scheduler.dto.DepartmentDTO;
import com.hotel.scheduler.model.Department;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DepartmentController {
    private final com.hotel.scheduler.service.UserActionLogService userActionLogService;
    /**
     * DepartmentController manages hotel departments (CRUD operations).
     * 
     * Key Endpoints:
     * - GET /api/departments: List all departments (manager/admin only)
     * - GET /api/departments/{id}: Get department by ID (manager/admin only)
     * - POST /api/departments: Create new department (manager/admin only)
     * - PUT /api/departments/{id}: Update department (manager/admin only)
     * - DELETE /api/departments/{id}: Delete department (manager/admin only)
     * 
     * Security:
     * - All endpoints require authentication and manager/admin role
     * - Uses @PreAuthorize for role-based access (same as AuthController)
     * 
     * Dependencies:
     * - DepartmentRepository: JPA repository for Department entity
     * - Employee: Used for auditing (created/updated by)
     */
    
    private final DepartmentRepository departmentRepository;
    
    /**
     * Returns a list of all hotel departments.
     * Only accessible by managers and admins.
     */
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(DepartmentDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(departmentDTOs);
    }
    
    /**
     * Returns details for a specific department by ID.
     * Only accessible by managers and admins.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<DepartmentDTO> getDepartment(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(department -> ResponseEntity.ok(DepartmentDTO.fromEntity(department)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Creates a new department.
     * Only accessible by managers and admins.
     * @param request Map with 'name' and 'description' keys
     * @param currentUser Authenticated user (for auditing, if needed)
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createDepartment(@RequestBody Map<String, Object> request, 
                                            @AuthenticationPrincipal Employee currentUser) {
        try {
            Department department = new Department();
            department.setName((String) request.get("name"));
            department.setDescription((String) request.get("description"));
            if (request.containsKey("minStaffing")) {
                department.setMinStaffing((Integer) request.get("minStaffing"));
            }
            if (request.containsKey("maxStaffing")) {
                department.setMaxStaffing((Integer) request.get("maxStaffing"));
            }
            if (request.containsKey("totalShifts")) {
                department.setTotalShifts((Integer) request.get("totalShifts"));
            }
            Department saved = departmentRepository.save(department);
            userActionLogService.logAction("CREATED_DEPARTMENT", currentUser);
            return ResponseEntity.ok(DepartmentDTO.fromEntity(saved));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_CREATE_DEPARTMENT", currentUser);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Updates an existing department by ID.
     * Only accessible by managers and admins.
     * @param id Department ID
     * @param request Map with 'name' and/or 'description' keys
     * @param currentUser Authenticated user (for auditing, if needed)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, 
                                            @RequestBody Map<String, Object> request,
                                            @AuthenticationPrincipal Employee currentUser) {
        try {
            Department existing = departmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            if (request.containsKey("name")) {
                existing.setName((String) request.get("name"));
            }
            if (request.containsKey("description")) {
                existing.setDescription((String) request.get("description"));
            }
            if (request.containsKey("minStaffing")) {
                existing.setMinStaffing((Integer) request.get("minStaffing"));
            }
            if (request.containsKey("maxStaffing")) {
                existing.setMaxStaffing((Integer) request.get("maxStaffing"));
            }
            if (request.containsKey("totalShifts")) {
                existing.setTotalShifts((Integer) request.get("totalShifts"));
            }
            Department saved = departmentRepository.save(existing);
            userActionLogService.logAction("UPDATED_DEPARTMENT", currentUser);
            return ResponseEntity.ok(DepartmentDTO.fromEntity(saved));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_UPDATE_DEPARTMENT", currentUser);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Deletes a department by ID.
     * Only accessible by managers and admins.
     * @param id Department ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            departmentRepository.deleteById(id);
            userActionLogService.logAction("DELETED_DEPARTMENT", currentUser);
            return ResponseEntity.ok(new MessageResponse("Department deleted successfully"));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_DELETE_DEPARTMENT", currentUser);
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
}
