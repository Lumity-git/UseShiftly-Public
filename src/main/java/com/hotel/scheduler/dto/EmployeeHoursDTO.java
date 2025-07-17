package com.hotel.scheduler.dto;
import lombok.Data;

@Data
public class EmployeeHoursDTO {
    private String name;
    private String department;
    private int totalHours;
    private int regularHours;
    private int overtimeHours;
    private int shifts;
    private double avgHours;
}
