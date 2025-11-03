package com.useshiftly.scheduler.repository;

import java.util.List;
import java.util.Optional;

import com.useshiftly.scheduler.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    Optional<Building> findByName(String name);

    @Query("SELECT b FROM Building b JOIN b.employees e WHERE e.id = :adminId AND e.role = 'ADMIN'")
    List<Building> findByAdminId(@Param("adminId") Long adminId);


    // Find buildings by managers (many-to-many)
    List<Building> findByManagers_Id(Long managerId);

    @Query("SELECT COUNT(DISTINCT b) FROM Building b JOIN b.employees e WHERE e.id = :adminId AND e.role = 'ADMIN'")
    long countByAdminId(@Param("adminId") Long adminId);
}