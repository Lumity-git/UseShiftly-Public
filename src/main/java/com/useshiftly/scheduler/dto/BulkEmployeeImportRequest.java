package com.useshiftly.scheduler.dto;

import lombok.Data;
import java.util.List;

@Data
public class BulkEmployeeImportRequest {
    private List<EmployeeImportRow> employees;

    @Data
    public static class EmployeeImportRow {
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String dateOfBirth;
        private String address;
        private String emergencyContactName;
        private String emergencyContactRelation;
        private String emergencyContactPhone;
        private String role;
        private Long departmentId;
        private Boolean active;
    }
}
