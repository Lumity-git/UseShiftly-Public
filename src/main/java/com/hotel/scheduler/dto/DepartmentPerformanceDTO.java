package com.hotel.scheduler.dto;
import lombok.Data;

@Data
public class DepartmentPerformanceDTO {
    private String name;
    private int totalShifts;
    private int totalHours;
    private int employees;
    private double avgHours;
    private double coverage;
}
