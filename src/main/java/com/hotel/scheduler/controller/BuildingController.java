package com.hotel.scheduler.controller;

import com.hotel.scheduler.model.Building;
import com.hotel.scheduler.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BuildingController {
    private final BuildingRepository buildingRepository;

    /**
     * Get buildings by admin id
     */
    @GetMapping("/by-admin/{adminId}")
    public List<Building> getBuildingsByAdmin(@PathVariable Long adminId) {
        return buildingRepository.findByAdminId(adminId);
    }

    /**
     * Get all buildings (id, name)
     */
    @GetMapping
    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }
}
