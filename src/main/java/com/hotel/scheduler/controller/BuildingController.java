package com.hotel.scheduler.controller;

import com.hotel.scheduler.model.Building;
import com.hotel.scheduler.dto.BuildingDTO;
import com.hotel.scheduler.repository.BuildingRepository;
import com.hotel.scheduler.repository.EmployeeRepository;
import com.hotel.scheduler.model.Employee;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {
    private BuildingDTO toDTO(Building building) {
        if (building == null) return null;
        BuildingDTO dto = new BuildingDTO();
        dto.setId(building.getId());
        dto.setName(building.getName());
        dto.setAddress(building.getAddress());
        dto.setAdminId(building.getAdmin() != null ? building.getAdmin().getId() : null);
        // Set all manager IDs
        if (building.getManagers() != null) {
            dto.setManagerIds(building.getManagers().stream().map(Employee::getId).toList());
        }
        if (building.getEmployees() != null) {
            dto.setEmployeeIds(building.getEmployees().stream().map(Employee::getId).toList());
        }
        return dto;
    }
    private final BuildingRepository buildingRepository;
    private final EmployeeRepository employeeRepository;


    /**
     * Create a new building and associate it with the authenticated admin
     * Only admins can create buildings
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBuilding(@RequestBody Map<String, String> request,
                                           @AuthenticationPrincipal Employee currentUser) {
        String name = request.get("name");
        String address = request.get("address");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Building name is required");
        }
        if (address == null || address.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Building address is required");
        }
        Building building = new Building();
        building.setName(name.trim());
        building.setAddress(address.trim());
        // Note: Building doesn't have setAdmin anymore - admin is determined by employees with ADMIN role
        try {
            Building saved = buildingRepository.save(building);
            //Assign current user (admin) to the building
            currentUser.setBuilding(saved);
            employeeRepository.save(currentUser);
            return ResponseEntity.ok(toDTO(saved));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return ResponseEntity.status(409).body("Building name already exists for this admin");
        }
    }

    /**
     * Get buildings for the current manager (only their building)
     */
    @GetMapping("/my-building")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getMyBuilding(@AuthenticationPrincipal Employee currentUser) {
        List<Building> buildings = buildingRepository.findByManagers_Id(currentUser.getId());
        if (buildings == null || buildings.isEmpty()) {
            // Always return JSON for frontend compatibility
            return ResponseEntity.status(404).body(Map.of(
                "error", true,
                "message", "No building assigned to this manager"
            ));
        }
        // Return all buildings managed by this manager as DTOs
        List<BuildingDTO> dtos = buildings.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get buildings for the current admin
     */
    @GetMapping("/my-buildings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMyBuildings(@AuthenticationPrincipal Employee currentUser) {
        List<Building> buildings = buildingRepository.findByAdminId(currentUser.getId());
        List<BuildingDTO> dtos = buildings.stream().map(this::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Check if admin has any buildings
     */
    @GetMapping("/admin-has-buildings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminHasBuildings(@AuthenticationPrincipal Employee currentUser) {
        List<Building> buildings = buildingRepository.findByAdminId(currentUser.getId());
        boolean hasBuildings = buildings != null && !buildings.isEmpty();
        return ResponseEntity.ok(Map.of("hasBuildings", hasBuildings));
    }

    /**
     * Assign building to new user during invitation creation (stub)
     * This would be called by the invitation controller, not here, but stub for reference
     */
    // Assignment of buildings to users should be handled in a secure, admin-scoped service/controller only.

    /**
     * Set manager for a building (admin only)
     */
    @PutMapping("/{buildingId}/add-manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addManagerToBuilding(@PathVariable Long buildingId, @RequestBody Map<String, Object> request) {
        Long managerId = null;
        try {
            managerId = Long.valueOf(request.get("managerId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Invalid managerId"));
        }
        Building building = buildingRepository.findById(buildingId).orElse(null);
        if (building == null) {
            return ResponseEntity.status(404).body(Map.of("error", true, "message", "Building not found"));
        }
        com.hotel.scheduler.model.Employee manager = employeeRepository.findById(managerId).orElse(null);
        if (manager == null) {
            return ResponseEntity.status(404).body(Map.of("error", true, "message", "Manager employee not found"));
        }
        if (manager.getRole() != com.hotel.scheduler.model.Employee.Role.MANAGER) {
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Employee is not a MANAGER"));
        }
        if (building.getManagers().contains(manager)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Manager already assigned"));
        }
        building.getManagers().add(manager);
        buildingRepository.save(building);
        return ResponseEntity.ok(Map.of("success", true, "message", "Manager added", "buildingId", buildingId, "managerId", managerId));
    }

    @PutMapping("/{buildingId}/remove-manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeManagerFromBuilding(@PathVariable Long buildingId, @RequestBody Map<String, Object> request) {
        Long managerId = null;
        try {
            managerId = Long.valueOf(request.get("managerId").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Invalid managerId"));
        }
        Building building = buildingRepository.findById(buildingId).orElse(null);
        if (building == null) {
            return ResponseEntity.status(404).body(Map.of("error", true, "message", "Building not found"));
        }
        com.hotel.scheduler.model.Employee manager = employeeRepository.findById(managerId).orElse(null);
        if (manager == null) {
            return ResponseEntity.status(404).body(Map.of("error", true, "message", "Manager employee not found"));
        }
        if (!building.getManagers().contains(manager)) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Manager not assigned to building"));
        }
        building.getManagers().remove(manager);
        buildingRepository.save(building);
        return ResponseEntity.ok(Map.of("success", true, "message", "Manager removed", "buildingId", buildingId, "managerId", managerId));
    }


    /**
     * Get buildings by admin id (legacy, for frontend selection)
     */
    // Removed endpoint to prevent cross-admin access. Use /my-buildings for admin's own buildings.

    /**
     * Get all buildings (id, name)
     */
    // Removed endpoint to prevent cross-admin access. Use /my-buildings for admin's own buildings.
}
