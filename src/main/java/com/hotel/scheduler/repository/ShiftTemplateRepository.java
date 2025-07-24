package com.hotel.scheduler.repository;

import com.hotel.scheduler.model.ShiftTemplate;
import com.hotel.scheduler.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {
    List<ShiftTemplate> findByDepartment(Department department);
    List<ShiftTemplate> findByDepartmentId(Long departmentId);
}
