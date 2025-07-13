package com.hotel.scheduler.dto;

import com.hotel.scheduler.model.Employee;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role;
    private Long departmentId;
    private String departmentName;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static EmployeeDTO fromEntity(Employee employee) {
        return new EmployeeDTO(
            employee.getId(),
            employee.getEmail(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getPhoneNumber(),
            employee.getRole().name(),
            employee.getDepartment() != null ? employee.getDepartment().getId() : null,
            employee.getDepartment() != null ? employee.getDepartment().getName() : null,
            employee.getActive(),
            employee.getCreatedAt(),
            employee.getUpdatedAt()
        );
    }
}
