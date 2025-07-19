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
    private Long buildingId;
    private String buildingName;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String dateOfBirth;
    private String address;
    private String emergencyContactName;
    private String emergencyContactRelation;
    private String emergencyContactPhone;
    private String role;
    private Long departmentId;
    private String departmentName;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean mustChangePassword;

    public static EmployeeDTO fromEntity(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBuildingId(employee.getBuilding() != null ? employee.getBuilding().getId() : null);
        dto.setBuildingName(employee.getBuilding() != null ? employee.getBuilding().getName() : null);
        dto.setId(employee.getId());
        dto.setEmail(employee.getEmail());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setPhoneNumber(employee.getPhoneNumber());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setAddress(employee.getAddress());
        dto.setEmergencyContactName(employee.getEmergencyContactName());
        dto.setEmergencyContactRelation(employee.getEmergencyContactRelation());
        dto.setEmergencyContactPhone(employee.getEmergencyContactPhone());
        dto.setRole(employee.getRole().name());
        dto.setDepartmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null);
        dto.setDepartmentName(employee.getDepartment() != null ? employee.getDepartment().getName() : null);
        dto.setActive(employee.getActive());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());
        dto.setMustChangePassword(employee.isMustChangePassword());
        return dto;
    }
}
