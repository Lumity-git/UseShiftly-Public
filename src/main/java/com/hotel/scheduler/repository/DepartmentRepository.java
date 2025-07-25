package com.hotel.scheduler.repository;

import com.hotel.scheduler.model.Department;
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

    @Query("SELECT d FROM Department d WHERE d.building.admin.id = :adminId")
    List<Department> findAllByAdminId(@Param("adminId") Long adminId);

    @Query("SELECT d FROM Department d WHERE d.building.id = :buildingId")
    List<Department> findAllByBuildingId(@Param("buildingId") Long buildingId);
}
