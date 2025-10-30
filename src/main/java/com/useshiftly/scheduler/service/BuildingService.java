package com.useshiftly.scheduler.service;

import com.useshiftly.scheduler.model.Building;
import com.useshiftly.scheduler.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private final BuildingRepository buildingRepository;

    public Building createBuilding(Building building) {
        return buildingRepository.save(building);
    }
}
