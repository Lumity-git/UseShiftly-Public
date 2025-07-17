package com.hotel.scheduler.dto;

import com.hotel.scheduler.model.Department;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private int employeeCount;
    private Integer minStaffing;
    private Integer maxStaffing;
    private Integer totalShifts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepartmentDTO fromEntity(Department department) {
        return new DepartmentDTO(
            department.getId(),
            department.getName(),
            department.getDescription(),
            department.getActive(),
            department.getEmployees() != null ? department.getEmployees().size() : 0,
            department.getMinStaffing(),
            department.getMaxStaffing(),
            department.getTotalShifts(),
            department.getCreatedAt(),
            department.getUpdatedAt()
        );
    }
}
