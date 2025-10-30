package com.useshiftly.scheduler.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for creating or updating a ShiftRequirement via API.
 */
@Data
public class ShiftRequirementDTO {
    private Long id;
    private Long departmentId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int requiredEmployees;
    private String notes;
}
