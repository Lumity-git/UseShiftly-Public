package com.hotel.scheduler.controller;

import com.hotel.scheduler.dto.DepartmentDTO;
import com.hotel.scheduler.model.Department;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.repository.DepartmentRepository;
import com.hotel.scheduler.model.Building;
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
    private final com.hotel.scheduler.repository.BuildingRepository buildingRepository;
    
    /**
     * Returns a list of all hotel departments.
     * Only accessible by managers and admins.
     */
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments(@AuthenticationPrincipal Employee currentUser) {
        List<Department> departments;
        if (currentUser.getRole() == Employee.Role.ADMIN) {
            departments = departmentRepository.findAllByAdminId(currentUser.getId());
        } else if (currentUser.getRole() == Employee.Role.MANAGER) {
            List<Building> buildings = buildingRepository.findByManagers_Id(currentUser.getId());
            departments = buildings.stream()
                .flatMap(b -> departmentRepository.findAllByBuildingId(b.getId()).stream())
                .toList();
        } else {
            return ResponseEntity.status(403).build();
        }
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
    public ResponseEntity<DepartmentDTO> getDepartment(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        return departmentRepository.findById(id)
                .filter(department -> {
                    if (currentUser.getRole() == Employee.Role.ADMIN) {
                        return department.getBuilding().getAdmin().getId().equals(currentUser.getId());
                    } else if (currentUser.getRole() == Employee.Role.MANAGER) {
                        return department.getBuilding().getManagers() != null &&
                               department.getBuilding().getManagers().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
                    }
                    return false;
                })
                .map(department -> ResponseEntity.ok(DepartmentDTO.fromEntity(department)))
                .orElse(ResponseEntity.status(403).build());
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
            Long buildingId = request.containsKey("buildingId") ? Long.valueOf(request.get("buildingId").toString()) : null;
            if (buildingId == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Missing buildingId"));
            }
            var buildingOpt = buildingRepository.findById(buildingId);
            if (buildingOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Building not found"));
            }
            var building = buildingOpt.get();
            boolean allowed = false;
            if (currentUser.getRole() == Employee.Role.ADMIN) {
                allowed = building.getAdmin().getId().equals(currentUser.getId());
            } else if (currentUser.getRole() == Employee.Role.MANAGER) {
                allowed = building.getManagers() != null && building.getManagers().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
            }
            if (!allowed) {
                return ResponseEntity.status(403).body(new MessageResponse("Forbidden: Not your building"));
            }
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
            department.setBuilding(building);
            Department saved = departmentRepository.save(department);
            userActionLogService.logAction("CREATED_DEPARTMENT", currentUser.getId());
            return ResponseEntity.ok(DepartmentDTO.fromEntity(saved));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_CREATE_DEPARTMENT", currentUser.getId());
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
            boolean allowed = false;
            if (currentUser.getRole() == Employee.Role.ADMIN) {
                allowed = existing.getBuilding().getAdmin().getId().equals(currentUser.getId());
            } else if (currentUser.getRole() == Employee.Role.MANAGER) {
                allowed = existing.getBuilding().getManagers() != null &&
                          existing.getBuilding().getManagers().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
            }
            if (!allowed) {
                return ResponseEntity.status(403).body(new MessageResponse("Forbidden: Not your department/building"));
            }
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
            userActionLogService.logAction("UPDATED_DEPARTMENT", currentUser.getId());
            return ResponseEntity.ok(DepartmentDTO.fromEntity(saved));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_UPDATE_DEPARTMENT", currentUser.getId());
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
            Department existing = departmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            boolean allowed = false;
            if (currentUser.getRole() == Employee.Role.ADMIN) {
                allowed = existing.getBuilding().getAdmin().getId().equals(currentUser.getId());
            } else if (currentUser.getRole() == Employee.Role.MANAGER) {
                allowed = existing.getBuilding().getManagers() != null &&
                          existing.getBuilding().getManagers().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
            }
            if (!allowed) {
                return ResponseEntity.status(403).body(new MessageResponse("Forbidden: Not your department/building"));
            }
            departmentRepository.deleteById(id);
            userActionLogService.logAction("DELETED_DEPARTMENT", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Department deleted successfully"));
        } catch (Exception e) {
            userActionLogService.logAction("FAILED_DELETE_DEPARTMENT", currentUser.getId());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Returns departments for a specific building.
     * Called by frontend when building selection changes in employee creation.
     * Endpoint: /api/admin/departments/by-building/{buildingId}
     */
    @GetMapping("/by-building/{buildingId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsByBuilding(@PathVariable Long buildingId, 
                                                                        @AuthenticationPrincipal Employee currentUser) {
        try {
            // Check if user has access to this building
            Building building = buildingRepository.findById(buildingId).orElse(null);
            if (building == null) {
                return ResponseEntity.notFound().build();
            }
            
            boolean hasAccess = false;
            if (currentUser.getRole() == Employee.Role.ADMIN) {
                hasAccess = building.getAdmin().getId().equals(currentUser.getId());
            } else if (currentUser.getRole() == Employee.Role.MANAGER) {
                hasAccess = building.getManagers() != null &&
                           building.getManagers().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
            }
            
            if (!hasAccess) {
                return ResponseEntity.status(403).build();
            }
            
            List<Department> departments = departmentRepository.findAllByBuildingId(buildingId);
            List<DepartmentDTO> departmentDTOs = departments.stream()
                    .map(DepartmentDTO::fromEntity)
                    .toList();
            return ResponseEntity.ok(departmentDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
