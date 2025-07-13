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
    
    private final DepartmentRepository departmentRepository;
    
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentDTO> departmentDTOs = departments.stream()
                .map(DepartmentDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(departmentDTOs);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<DepartmentDTO> getDepartment(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(department -> ResponseEntity.ok(DepartmentDTO.fromEntity(department)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createDepartment(@RequestBody Map<String, Object> request, 
                                            @AuthenticationPrincipal Employee currentUser) {
        try {
            Department department = new Department();
            department.setName((String) request.get("name"));
            department.setDescription((String) request.get("description"));
            
            Department saved = departmentRepository.save(department);
            return ResponseEntity.ok(DepartmentDTO.fromEntity(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
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
            
            Department saved = departmentRepository.save(existing);
            return ResponseEntity.ok(DepartmentDTO.fromEntity(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            departmentRepository.deleteById(id);
            return ResponseEntity.ok(new MessageResponse("Department deleted successfully"));
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
