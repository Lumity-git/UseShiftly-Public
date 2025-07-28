package com.hotel.scheduler.service;

import com.hotel.scheduler.model.Building;
import com.hotel.scheduler.repository.BuildingRepository;
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
