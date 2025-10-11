package com.hotel.scheduler.repository;

import java.util.List;
import java.util.Optional;

import com.hotel.scheduler.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    Optional<Building> findByName(String name);

    List<Building> findByAdminId(Long adminId);


    // Find buildings by managers (many-to-many)
    List<Building> findByManagers_Id(Long managerId);

    long countByAdminId(Long adminId);
}