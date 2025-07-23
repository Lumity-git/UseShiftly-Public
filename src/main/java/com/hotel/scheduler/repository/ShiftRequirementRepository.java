package com.hotel.scheduler.repository;

import com.hotel.scheduler.model.ShiftRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftRequirementRepository extends JpaRepository<ShiftRequirement, Long> {
    List<ShiftRequirement> findByDepartmentId(Long departmentId);
    List<ShiftRequirement> findByDepartmentIdAndShiftDateBetween(Long departmentId, LocalDate start, LocalDate end);
}
