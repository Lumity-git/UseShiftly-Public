package com.useshiftly.scheduler.dto;

import com.useshiftly.scheduler.model.Department;

import java.time.LocalDateTime;

public class DepartmentDTO {
    public DepartmentDTO(Long id, String name, String description, Boolean active, int employeeCount, Integer minStaffing, Integer maxStaffing, Integer totalShifts, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
        this.employeeCount = employeeCount;
        this.minStaffing = minStaffing;
        this.maxStaffing = maxStaffing;
        this.totalShifts = totalShifts;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public Long id;
    public String name;
    public String description;
    public Boolean active;
    public int employeeCount;
    public Integer minStaffing;
    public Integer maxStaffing;
    public Integer totalShifts;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static DepartmentDTO fromEntity(Department department) {
        return new DepartmentDTO(
            department.id,
            department.name,
            department.description,
            department.active,
            department.employees != null ? department.employees.size() : 0,
            department.minStaffing,
            department.maxStaffing,
            department.totalShifts,
            department.createdAt,
            department.updatedAt
        );
    }
}
