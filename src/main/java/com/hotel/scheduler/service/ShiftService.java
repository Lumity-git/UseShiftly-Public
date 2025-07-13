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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftService {
    
    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationService notificationService;
    
    public Shift createShift(CreateShiftRequest request, Employee createdBy) {
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
        
        return savedShift;
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
    
    public Optional<Shift> getShiftById(Long id) {
        return shiftRepository.findById(id);
    }
    
    public Shift updateShift(Long id, CreateShiftRequest request, Employee updatedBy) {
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
        
        return updatedShift;
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
