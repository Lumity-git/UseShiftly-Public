package com.useshiftly.scheduler.repository;

import com.useshiftly.scheduler.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);

    List<Department> findByActiveTrue();

    Optional<Department> findByNameAndActiveTrue(String name);

    boolean existsByName(String name);

    @Query("SELECT d FROM Department d WHERE d.building.id IN (SELECT b.id FROM Building b JOIN b.employees admin WHERE admin.id = :adminId AND admin.role = 'ADMIN')")
    List<Department> findAllByAdminId(@Param("adminId") Long adminId);

    @Query("SELECT d FROM Department d WHERE d.building.id = :buildingId")
    List<Department> findAllByBuildingId(@Param("buildingId") Long buildingId);
}
