package com.useshiftly.scheduler.dto;
import lombok.Data;

@Data
public class ReportStatisticsDTO {
    private int totalShifts;
    private int totalHours;
    private double averageHoursPerEmployee;
    private int overtimeHours;
    private double shiftCoverage;
    private int activeEmployees;
    private int pendingTrades;
    private int completedTrades;
}
