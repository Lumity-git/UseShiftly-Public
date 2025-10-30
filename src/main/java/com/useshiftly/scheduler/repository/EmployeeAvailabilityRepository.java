package com.useshiftly.scheduler.repository;

import com.useshiftly.scheduler.model.EmployeeAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeAvailabilityRepository extends JpaRepository<EmployeeAvailability, Long> {
    List<EmployeeAvailability> findByEmployeeId(Long employeeId);
    void deleteByEmployeeId(Long employeeId);
    List<EmployeeAvailability> findByEmployeeIdIn(List<Long> employeeIds);
}
