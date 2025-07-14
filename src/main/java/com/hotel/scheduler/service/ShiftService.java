package com.hotel.scheduler.service;

import com.hotel.scheduler.dto.shift.CreateShiftRequest;
import com.hotel.scheduler.dto.shift.ShiftResponse;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.model.Shift;
import com.hotel.scheduler.model.Department;
import com.hotel.scheduler.repository.ShiftRepository;
import com.hotel.scheduler.repository.EmployeeRepository;
import com.hotel.scheduler.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShiftService {
    /**
     * Cancel a posted shift (withdraw post, make unavailable for pickup).
     * Only the employee who posted the shift can cancel.
     */
    public void cancelPostedShift(Long shiftId, Employee currentUser) {
        log.info("User {} is attempting to cancel post for shift {}", currentUser.getId(), shiftId);
        Shift shift = shiftRepository.findById(shiftId)
            .orElseThrow(() -> new RuntimeException("Shift not found"));
        // Only allow if current user is the one who posted the shift and it's still posted
        if (shift.getStatus() != Shift.ShiftStatus.AVAILABLE_FOR_PICKUP) {
            throw new RuntimeException("Only posted shifts can be cancelled");
        }
        if (!shift.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only cancel your own posted shifts");
        }
        // Mark as scheduled again, make unavailable for pickup
        shift.setStatus(Shift.ShiftStatus.SCHEDULED);
        shift.setAvailableForPickup(false);
        shiftRepository.save(shift);
        // Fallback: find all trades for this shift and cancel POSTED_TO_EVERYONE
        List<com.hotel.scheduler.model.ShiftTrade> trades;
        try {
            trades = shiftTradeRepository.findByShiftId(shiftId);
        } catch (Exception e) {
            log.warn("findByShiftId not available, falling back to findAll");
            trades = shiftTradeRepository.findAll();
        }
        for (com.hotel.scheduler.model.ShiftTrade trade : trades) {
            if (trade.getShift() != null && trade.getShift().getId().equals(shiftId)
                && trade.getStatus() == com.hotel.scheduler.model.ShiftTrade.TradeStatus.POSTED_TO_EVERYONE) {
                trade.setStatus(com.hotel.scheduler.model.ShiftTrade.TradeStatus.CANCELLED);
                shiftTradeRepository.save(trade);
            }
        }
        // Log notification fallback
        log.info("Shift post cancelled notification would be sent to user {} for shift {}", currentUser.getId(), shiftId);
        log.info("Shift {} post cancelled by user {}", shiftId, currentUser.getId());
    }
    /**
     * Aggregates department statistics for reporting endpoints.
     * @param startDate ISO date string (optional)
     * @param endDate ISO date string (optional)
     * @return List of department stats maps (production ready)
     */
    public java.util.List<java.util.Map<String, Object>> getDepartmentStats(String startDate, String endDate) {
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;
        try {
            if (startDate != null && !startDate.isBlank()) {
                start = java.time.LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isBlank()) {
                end = java.time.LocalDateTime.parse(endDate);
            }
        } catch (Exception e) {
            // Invalid date format, return empty result
            return result;
        }

        java.util.List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            java.util.List<Shift> shifts;
            if (start != null && end != null) {
                shifts = shiftRepository.findByDepartmentAndDateRange(dept.getId(), start, end);
            } else {
                shifts = shiftRepository.findByDepartmentId(dept.getId());
            }
            int totalShifts = shifts.size();
            double totalHours = shifts.stream()
                .mapToDouble(s -> java.time.Duration.between(s.getStartTime(), s.getEndTime()).toHours())
                .sum();
            int employeeCount = (int) shifts.stream()
                .map(Shift::getEmployee)
                .filter(java.util.Objects::nonNull)
                .map(Employee::getId)
                .distinct()
                .count();

            java.util.Map<String, Object> deptMap = new java.util.HashMap<>();
            deptMap.put("departmentName", dept.getName());
            deptMap.put("totalShifts", totalShifts);
            deptMap.put("totalHours", totalHours);
            deptMap.put("employeeCount", employeeCount);
            result.add(deptMap);
        }
        return result;
    }
    /**
     * Aggregates employee hours for reporting endpoints.
     * @param startDate ISO date string (optional)
     * @param endDate ISO date string (optional)
     * @param departmentId Department filter (optional)
     * @return List of employee hour maps (to be implemented with real aggregation)
     */
    public java.util.List<java.util.Map<String, Object>> getEmployeeHours(String startDate, String endDate, Long departmentId) {
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;
        try {
            if (startDate != null && !startDate.isBlank()) {
                start = java.time.LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isBlank()) {
                end = java.time.LocalDateTime.parse(endDate);
            }
        } catch (Exception e) {
            // Invalid date format, return empty result or handle as needed
            return result;
        }

        java.util.List<Employee> employees;
        if (departmentId != null) {
            employees = employeeRepository.findByDepartmentId(departmentId);
        } else {
            employees = employeeRepository.findAll();
        }

        for (Employee emp : employees) {
            java.util.List<Shift> shifts;
            if (start != null && end != null) {
                shifts = shiftRepository.findByEmployeeAndDateRange(emp.getId(), start, end);
            } else {
                shifts = shiftRepository.findByEmployeeId(emp.getId());
            }
            double totalHours = shifts.stream()
                .mapToDouble(s -> java.time.Duration.between(s.getStartTime(), s.getEndTime()).toHours())
                .sum();
            double overtimeHours = Math.max(0, totalHours - 40); // Overtime if > 40 hours

            java.util.Map<String, Object> empMap = new java.util.HashMap<>();
            empMap.put("employeeName", emp.getFirstName() + " " + emp.getLastName());
            empMap.put("totalHours", totalHours);
            empMap.put("overtimeHours", overtimeHours);
            result.add(empMap);
        }
        return result;
    }
    /**
     * Aggregates shift analytics for reporting endpoints.
     * @param startDate ISO date string (optional)
     * @param endDate ISO date string (optional)
     * @param departmentId Department filter (optional)
     * @return Map of analytics (to be implemented with real aggregation)
     */
    public java.util.Map<String, Object> getShiftAnalytics(String startDate, String endDate, Long departmentId) {
        java.util.Map<String, Object> analytics = new java.util.HashMap<>();
        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;
        try {
            if (startDate != null && !startDate.isBlank()) {
                start = java.time.LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isBlank()) {
                end = java.time.LocalDateTime.parse(endDate);
            }
        } catch (Exception e) {
            // Invalid date format, return empty analytics
            return analytics;
        }

        java.util.List<Shift> shifts;
        if (departmentId != null && start != null && end != null) {
            shifts = shiftRepository.findByDepartmentAndDateRange(departmentId, start, end);
        } else if (departmentId != null) {
            shifts = shiftRepository.findByDepartmentId(departmentId);
        } else if (start != null && end != null) {
            shifts = shiftRepository.findByDateRange(start, end);
        } else {
            shifts = shiftRepository.findAll();
        }

        // Shifts per day
        java.util.Map<java.time.LocalDate, Long> shiftsPerDay = shifts.stream()
            .collect(java.util.stream.Collectors.groupingBy(s -> s.getStartTime().toLocalDate(), java.util.stream.Collectors.counting()));
        analytics.put("shiftsPerDay", shiftsPerDay);

        // Hours per department
        java.util.Map<String, Double> hoursPerDepartment = new java.util.HashMap<>();
        for (Shift s : shifts) {
            if (s.getDepartment() != null) {
                String deptName = s.getDepartment().getName();
                double hours = java.time.Duration.between(s.getStartTime(), s.getEndTime()).toHours();
                hoursPerDepartment.put(deptName, hoursPerDepartment.getOrDefault(deptName, 0.0) + hours);
            }
        }
        analytics.put("hoursPerDepartment", hoursPerDepartment);

        // Employee utilization (average hours per employee)
        java.util.Map<Long, Double> hoursByEmployee = new java.util.HashMap<>();
        for (Shift s : shifts) {
            if (s.getEmployee() != null) {
                Long empId = s.getEmployee().getId();
                double hours = java.time.Duration.between(s.getStartTime(), s.getEndTime()).toHours();
                hoursByEmployee.put(empId, hoursByEmployee.getOrDefault(empId, 0.0) + hours);
            }
        }
        double employeeUtilization = hoursByEmployee.isEmpty() ? 0.0 : hoursByEmployee.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        analytics.put("employeeUtilization", employeeUtilization);

        // Peak hours (most common shift start times, rounded to hour)
        java.util.Map<Integer, Long> hourCounts = shifts.stream()
            .collect(java.util.stream.Collectors.groupingBy(s -> s.getStartTime().getHour(), java.util.stream.Collectors.counting()));
        java.util.List<Integer> peakHours = hourCounts.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(3)
            .map(java.util.Map.Entry::getKey)
            .toList();
        analytics.put("peakHours", peakHours);

        return analytics;
    }
    /**
     * Aggregates shift statistics for reporting endpoints.
     * @param startDate ISO date string (optional)
     * @param endDate ISO date string (optional)
     * @param departmentId Department filter (optional)
     * @return Map of statistics (to be implemented with real aggregation)
     */
    public java.util.Map<String, Object> getShiftStatistics(String startDate, String endDate, Long departmentId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;
        try {
            if (startDate != null && !startDate.isBlank()) {
                start = java.time.LocalDateTime.parse(startDate);
            }
            if (endDate != null && !endDate.isBlank()) {
                end = java.time.LocalDateTime.parse(endDate);
            }
        } catch (Exception e) {
            // Invalid date format, return empty stats
            return stats;
        }

        java.util.List<Shift> shifts;
        if (departmentId != null && start != null && end != null) {
            shifts = shiftRepository.findByDepartmentAndDateRange(departmentId, start, end);
        } else if (departmentId != null) {
            shifts = shiftRepository.findByDepartmentId(departmentId);
        } else if (start != null && end != null) {
            shifts = shiftRepository.findByDateRange(start, end);
        } else {
            shifts = shiftRepository.findAll();
        }

        int totalShifts = shifts.size();
        int completedShifts = (int) shifts.stream().filter(s -> s.getStatus() == Shift.ShiftStatus.COMPLETED).count();
        int cancelledShifts = (int) shifts.stream().filter(s -> s.getStatus() == Shift.ShiftStatus.CANCELLED).count();
        int availableShifts = (int) shifts.stream().filter(s -> s.getStatus() == Shift.ShiftStatus.AVAILABLE_FOR_PICKUP).count();
        double totalHours = shifts.stream()
            .mapToDouble(s -> java.time.Duration.between(s.getStartTime(), s.getEndTime()).toHours())
            .sum();
        double averageShiftLength = totalShifts > 0 ? totalHours / totalShifts : 0.0;

        // Most active employee
        java.util.Map<Long, Long> shiftCountByEmployee = shifts.stream()
            .filter(s -> s.getEmployee() != null)
            .collect(java.util.stream.Collectors.groupingBy(s -> s.getEmployee().getId(), java.util.stream.Collectors.counting()));
        Long mostActiveEmployeeId = shiftCountByEmployee.entrySet().stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey)
            .orElse(null);
        String mostActiveEmployee = mostActiveEmployeeId != null
            ? employeeRepository.findById(mostActiveEmployeeId)
                .map(e -> e.getFirstName() + " " + e.getLastName())
                .orElse("")
            : "";

        // Busiest department
        java.util.Map<Long, Long> shiftCountByDept = shifts.stream()
            .filter(s -> s.getDepartment() != null)
            .collect(java.util.stream.Collectors.groupingBy(s -> s.getDepartment().getId(), java.util.stream.Collectors.counting()));
        Long busiestDeptId = shiftCountByDept.entrySet().stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey)
            .orElse(null);
        String busiestDepartment = busiestDeptId != null
            ? departmentRepository.findById(busiestDeptId)
                .map(d -> d.getName())
                .orElse("")
            : "";

        stats.put("totalShifts", totalShifts);
        stats.put("completedShifts", completedShifts);
        stats.put("cancelledShifts", cancelledShifts);
        stats.put("availableShifts", availableShifts);
        stats.put("totalHours", totalHours);
        stats.put("averageShiftLength", averageShiftLength);
        stats.put("mostActiveEmployee", mostActiveEmployee);
        stats.put("busiestDepartment", busiestDepartment);
        return stats;
    }
    
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;
    private final com.hotel.scheduler.repository.ShiftTradeRepository shiftTradeRepository;
    
    public ShiftResponse createShift(CreateShiftRequest request, Employee createdBy) {
        // Validate department exists
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
        // Validate employee exists if assigned
        Employee assignedEmployee = null;
        if (request.getEmployeeId() != null) {
            assignedEmployee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            // Check for scheduling conflicts
            if (hasSchedulingConflict(request.getEmployeeId(), request.getStartTime(), request.getEndTime())) {
                throw new RuntimeException("Employee already has a shift scheduled during this time");
            }
        }
        Shift shift = new Shift();
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setEmployee(assignedEmployee);
        shift.setDepartment(department);
        shift.setNotes(request.getNotes());
        shift.setCreatedBy(createdBy);
        Shift savedShift = shiftRepository.save(shift);
        // Send notification to assigned employee
        if (assignedEmployee != null) {
            notificationService.sendShiftAssignmentNotification(assignedEmployee, savedShift);
        }
        return convertToResponse(savedShift);
    }
    
    public List<ShiftResponse> getShiftsForEmployee(Long employeeId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Shift> shifts;
        if (startDate != null && endDate != null) {
            shifts = shiftRepository.findByEmployeeAndDateRange(employeeId, startDate, endDate);
        } else {
            shifts = shiftRepository.findByEmployeeId(employeeId);
        }
        return shifts.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public List<ShiftResponse> getShiftsForDepartment(Long departmentId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Shift> shifts;
        if (startDate != null && endDate != null) {
            shifts = shiftRepository.findByDepartmentAndDateRange(departmentId, startDate, endDate);
        } else {
            shifts = shiftRepository.findByDepartmentId(departmentId);
        }
        return shifts.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public List<ShiftResponse> getAllShifts(LocalDateTime startDate, LocalDateTime endDate) {
        List<Shift> shifts;
        if (startDate != null && endDate != null) {
            shifts = shiftRepository.findByDateRange(startDate, endDate);
        } else {
            shifts = shiftRepository.findAll();
        }
        return shifts.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    public Optional<ShiftResponse> getShiftById(Long id) {
        return shiftRepository.findById(id).map(this::convertToResponse);
    }
    
    public ShiftResponse updateShift(Long id, CreateShiftRequest request, Employee updatedBy) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        
        // Validate department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));
        
        // Validate employee if being assigned
        Employee assignedEmployee = null;
        if (request.getEmployeeId() != null) {
            assignedEmployee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            // Check for conflicts (excluding current shift)
            if (!request.getEmployeeId().equals(shift.getEmployee() != null ? shift.getEmployee().getId() : null)) {
                if (hasSchedulingConflict(request.getEmployeeId(), request.getStartTime(), request.getEndTime())) {
                    throw new RuntimeException("Employee already has a shift scheduled during this time");
                }
            }
        }
        
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setEmployee(assignedEmployee);
        shift.setDepartment(department);
        shift.setNotes(request.getNotes());
        
        Shift updatedShift = shiftRepository.save(shift);
        
        // Send notification about shift update
        if (assignedEmployee != null) {
            notificationService.sendShiftUpdateNotification(assignedEmployee, updatedShift);
        }
        
        return convertToResponse(updatedShift);
    }
    
    public void deleteShift(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        
        // Notify assigned employee about cancellation
        if (shift.getEmployee() != null) {
            notificationService.sendShiftCancellationNotification(shift.getEmployee(), shift);
        }
        
        shiftRepository.delete(shift);
    }
    
    public Shift makeShiftAvailableForPickup(Long shiftId, Employee requestingEmployee, String reason) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        
        // Verify the requesting employee owns this shift
        if (!shift.getEmployee().getId().equals(requestingEmployee.getId())) {
            throw new RuntimeException("You can only give away your own shifts");
        }
        
        shift.setAvailableForPickup(true);
        shift.setStatus(Shift.ShiftStatus.AVAILABLE_FOR_PICKUP);
        
        return shiftRepository.save(shift);
    }
    
    public Shift pickupShift(Long shiftId, Employee pickupEmployee) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        
        if (!shift.getAvailableForPickup()) {
            throw new RuntimeException("This shift is not available for pickup");
        }
        
        // Check for scheduling conflicts
        if (hasSchedulingConflict(pickupEmployee.getId(), shift.getStartTime(), shift.getEndTime())) {
            throw new RuntimeException("You already have a shift scheduled during this time");
        }
        
        Employee originalEmployee = shift.getEmployee();
        shift.setEmployee(pickupEmployee);
        shift.setAvailableForPickup(false);
        shift.setStatus(Shift.ShiftStatus.SCHEDULED);
        
        Shift updatedShift = shiftRepository.save(shift);
        
        // Send notifications
        notificationService.sendShiftPickupNotification(originalEmployee, pickupEmployee, updatedShift);
        
        return updatedShift;
    }
    
    public List<ShiftResponse> getAvailableShifts() {
        List<Shift> shifts = shiftRepository.findAvailableForPickup();
        return shifts.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * Offer a shift to a specific employee (trade request).
     */
    public void offerShiftToEmployee(Long shiftId, Employee requestingEmployee, Long targetEmployeeId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        if (shift.getEmployee() == null || !shift.getEmployee().getId().equals(requestingEmployee.getId())) {
            throw new RuntimeException("You can only offer your own shifts");
        }
        if (shift.getStatus() != Shift.ShiftStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled shifts can be traded");
        }
        Employee targetEmployee = employeeRepository.findById(targetEmployeeId)
                .orElseThrow(() -> new RuntimeException("Target employee not found"));
        // Create and save ShiftTrade entity for this offer
        com.hotel.scheduler.model.ShiftTrade trade = new com.hotel.scheduler.model.ShiftTrade();
        trade.setShift(shift);
        trade.setRequestingEmployee(requestingEmployee);
        trade.setPickupEmployee(targetEmployee);
        trade.setStatus(com.hotel.scheduler.model.ShiftTrade.TradeStatus.PENDING);
        trade.setRequestedAt(java.time.LocalDateTime.now());
        shiftTradeRepository.save(trade);
        notificationService.sendShiftTradeOfferNotification(targetEmployee, shift, requestingEmployee);
        // Notify the requesting employee that they are still responsible until accepted
        notificationService.sendShiftTradeResponsibilityNotification(requestingEmployee, targetEmployee, shift);
        shift.setStatus(Shift.ShiftStatus.PENDING);
        shiftRepository.save(shift);
    }

    /**
     * Post a shift to all employees (make available for pickup).
     */
    public void postShiftToEveryone(Long shiftId, Employee requestingEmployee) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        if (shift.getEmployee() == null || !shift.getEmployee().getId().equals(requestingEmployee.getId())) {
            throw new RuntimeException("You can only post your own shifts");
        }
        if (shift.getStatus() != Shift.ShiftStatus.SCHEDULED) {
            throw new RuntimeException("Only scheduled shifts can be posted");
        }
        shift.setAvailableForPickup(true);
        shift.setStatus(Shift.ShiftStatus.AVAILABLE_FOR_PICKUP);
        shiftRepository.save(shift);
        // Persist a ShiftTrade record for 'post to everyone' action
        com.hotel.scheduler.model.ShiftTrade trade = new com.hotel.scheduler.model.ShiftTrade();
        trade.setShift(shift);
        trade.setRequestingEmployee(requestingEmployee);
        trade.setPickupEmployee(null); // No specific pickup employee
        trade.setStatus(com.hotel.scheduler.model.ShiftTrade.TradeStatus.POSTED_TO_EVERYONE);
        trade.setRequestedAt(java.time.LocalDateTime.now());
        shiftTradeRepository.save(trade);
        // Notify all employees except the requester
        List<Employee> allEmployees = employeeRepository.findAll();
        notificationService.sendShiftPostedToEveryoneNotification(shift, requestingEmployee, allEmployees);
        // Notify the requesting employee that they are still responsible until someone picks up
        notificationService.sendShiftPostedResponsibilityNotification(requestingEmployee, shift);
    }
    
    private boolean hasSchedulingConflict(Long employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        return shiftRepository.countConflictingShifts(employeeId, startTime, endTime) > 0;
    }
    
    private ShiftResponse convertToResponse(Shift shift) {
        ShiftResponse response = new ShiftResponse();
        response.setId(shift.getId());
        response.setStartTime(shift.getStartTime());
        response.setEndTime(shift.getEndTime());
        response.setNotes(shift.getNotes());
        response.setStatus(shift.getStatus().name());
        response.setAvailableForPickup(shift.getAvailableForPickup());
        response.setCreatedAt(shift.getCreatedAt());
        
        if (shift.getEmployee() != null) {
            response.setEmployeeId(shift.getEmployee().getId());
            response.setEmployeeName(shift.getEmployee().getFirstName() + " " + shift.getEmployee().getLastName());
            response.setEmployeeEmail(shift.getEmployee().getEmail());
        }
        
        if (shift.getDepartment() != null) {
            response.setDepartmentId(shift.getDepartment().getId());
            response.setDepartmentName(shift.getDepartment().getName());
        }
        
        if (shift.getCreatedBy() != null) {
            response.setCreatedByName(shift.getCreatedBy().getFirstName() + " " + shift.getCreatedBy().getLastName());
        }
        
        return response;
    }
}
