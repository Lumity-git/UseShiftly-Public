package com.hotel.scheduler.controller;

import com.hotel.scheduler.model.Building;
import com.hotel.scheduler.dto.BuildingDTO;
import com.hotel.scheduler.repository.BuildingRepository;
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
@CrossOrigin(origins = "*", maxAge = 3600)
public class BuildingController {
    private BuildingDTO toDTO(Building building) {
        if (building == null) return null;
        BuildingDTO dto = new BuildingDTO();
        dto.setId(building.getId());
        dto.setName(building.getName());
        dto.setAdminId(building.getAdmin() != null ? building.getAdmin().getId() : null);
        dto.setManagerId(building.getManager() != null ? building.getManager().getId() : null);
        if (building.getEmployees() != null) {
            dto.setEmployeeIds(building.getEmployees().stream().map(Employee::getId).toList());
        }
        return dto;
    }
    private final BuildingRepository buildingRepository;


    /**
     * Create a new building and associate it with the authenticated admin
     * Only admins can create buildings
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createBuilding(@RequestBody Map<String, String> request,
                                           @AuthenticationPrincipal Employee currentUser) {
        String name = request.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Building name is required");
        }
        Building building = new Building();
        building.setName(name.trim());
        building.setAdmin(currentUser);
        try {
            Building saved = buildingRepository.save(building);
            return ResponseEntity.ok(toDTO(saved));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Check for duplicate key violation
            return ResponseEntity.status(409).body("Building name already exists");
        }
    }

    /**
     * Get buildings for the current manager (only their building)
     */
    @GetMapping("/my-building")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> getMyBuilding(@AuthenticationPrincipal Employee currentUser) {
        // Assuming manager is assigned to one building
        List<Building> buildings = buildingRepository.findByManager_Id(currentUser.getId());
        if (buildings == null || buildings.isEmpty()) {
            return ResponseEntity.ok().body("No building assigned to this manager");
        }
        return ResponseEntity.ok(toDTO(buildings.get(0)));
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
    @PostMapping("/assign-building")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> assignBuildingToUser(@RequestBody Map<String, Object> request,
                                                  @AuthenticationPrincipal Employee currentUser) {
        // request: { userId, buildingId }
        // TODO: Implement actual assignment logic in the relevant service/controller
        return ResponseEntity.ok(Map.of("status", "stub - not implemented"));
    }


    /**
     * Get buildings by admin id (legacy, for frontend selection)
     */
    @GetMapping("/by-admin/{adminId}")
    public List<BuildingDTO> getBuildingsByAdmin(@PathVariable Long adminId) {
        List<Building> buildings = buildingRepository.findByAdminId(adminId);
        return buildings.stream().map(this::toDTO).toList();
    }

    /**
     * Get all buildings (id, name)
     */
    @GetMapping
    public List<BuildingDTO> getAllBuildings() {
        return buildingRepository.findAll().stream().map(this::toDTO).toList();
    }
}
