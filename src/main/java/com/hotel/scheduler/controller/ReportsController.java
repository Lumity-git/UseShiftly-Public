package com.hotel.scheduler.controller;

import com.hotel.scheduler.dto.*;
import com.hotel.scheduler.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
public class ReportsController {
    private final ReportsService reportsService;

    @GetMapping("/statistics")
    public ResponseEntity<ReportStatisticsDTO> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(reportsService.getStatistics(startDate, endDate, departmentId, employeeId));
    }

    @GetMapping("/shifts-by-day")
    public ResponseEntity<ShiftsByDayDTO> getShiftsByDay(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(reportsService.getShiftsByDay(startDate, endDate, departmentId, employeeId));
    }

    @GetMapping("/hours-by-department")
    public ResponseEntity<HoursByDepartmentDTO> getHoursByDepartment(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(reportsService.getHoursByDepartment(startDate, endDate));
    }

    @GetMapping("/shift-distribution")
    public ResponseEntity<ShiftDistributionDTO> getShiftDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(reportsService.getShiftDistribution(startDate, endDate, departmentId));
    }

    @GetMapping("/employee-hours")
    public ResponseEntity<List<EmployeeHoursDTO>> getEmployeeHours(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(reportsService.getEmployeeHours(startDate, endDate, departmentId, employeeId));
    }

    @GetMapping("/department-performance")
    public ResponseEntity<List<DepartmentPerformanceDTO>> getDepartmentPerformance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return ResponseEntity.ok(reportsService.getDepartmentPerformance(startDate, endDate));
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<MonthlyTrendDTO> getMonthlyTrend(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(reportsService.getMonthlyTrend(year, departmentId));
    }
}
