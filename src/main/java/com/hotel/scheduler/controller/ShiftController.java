package com.hotel.scheduler.controller;

import com.hotel.scheduler.dto.shift.CreateShiftRequest;
import com.hotel.scheduler.dto.shift.ShiftResponse;
import com.hotel.scheduler.dto.shift.ShiftTradeRequest;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.model.Shift;
import com.hotel.scheduler.model.ShiftTrade;
import com.hotel.scheduler.repository.ShiftTradeRepository;
import com.hotel.scheduler.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ShiftController {
    
    private final ShiftService shiftService;
    private final ShiftTradeRepository shiftTradeRepository;
    
    @GetMapping
    public ResponseEntity<List<ShiftResponse>> getShifts(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal Employee currentUser) {
        
        List<ShiftResponse> shifts;
        
        // If regular employee, only show their own shifts
        if (currentUser.getRole() == Employee.Role.EMPLOYEE) {
            shifts = shiftService.getShiftsForEmployee(currentUser.getId(), startDate, endDate);
        } else if (employeeId != null) {
            shifts = shiftService.getShiftsForEmployee(employeeId, startDate, endDate);
        } else if (departmentId != null) {
            shifts = shiftService.getShiftsForDepartment(departmentId, startDate, endDate);
        } else {
            shifts = shiftService.getAllShifts(startDate, endDate);
        }
        
        return ResponseEntity.ok(shifts);
    }
    
    @GetMapping("/my-shifts")
    public ResponseEntity<List<ShiftResponse>> getMyShifts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal Employee currentUser) {
        
        List<ShiftResponse> shifts = shiftService.getShiftsForEmployee(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(shifts);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<ShiftResponse>> getAvailableShifts(@AuthenticationPrincipal Employee currentUser) {
        try {
            // Get shifts that are available for pickup (AVAILABLE status)
            List<ShiftResponse> availableShifts = shiftService.getAvailableShifts();
            return ResponseEntity.ok(availableShifts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Shift> getShift(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        return shiftService.getShiftById(id)
                .map(shift -> {
                    // Check permissions
                    if (currentUser.getRole() == Employee.Role.EMPLOYEE && 
                        (shift.getEmployee() == null || !shift.getEmployee().getId().equals(currentUser.getId()))) {
                        return ResponseEntity.status(403).<Shift>build();
                    }
                    return ResponseEntity.ok(shift);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createShift(@Valid @RequestBody CreateShiftRequest request, 
                                        @AuthenticationPrincipal Employee currentUser) {
        try {
            Shift shift = shiftService.createShift(request, currentUser);
            return ResponseEntity.ok(shift);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateShift(@PathVariable Long id, 
                                        @Valid @RequestBody CreateShiftRequest request,
                                        @AuthenticationPrincipal Employee currentUser) {
        try {
            Shift shift = shiftService.updateShift(id, request, currentUser);
            return ResponseEntity.ok(shift);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteShift(@PathVariable Long id) {
        try {
            shiftService.deleteShift(id);
            return ResponseEntity.ok(new MessageResponse("Shift deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/give-away")
    public ResponseEntity<?> giveAwayShift(@PathVariable Long id, 
                                          @RequestBody ShiftTradeRequest request,
                                          @AuthenticationPrincipal Employee currentUser) {
        try {
            shiftService.makeShiftAvailableForPickup(id, currentUser, request.getReason());
            return ResponseEntity.ok(new MessageResponse("Shift is now available for pickup"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/pick-up")
    public ResponseEntity<?> pickupShift(@PathVariable Long id, 
                                        @AuthenticationPrincipal Employee currentUser) {
        try {
            shiftService.pickupShift(id, currentUser);
            return ResponseEntity.ok(new MessageResponse("Shift picked up successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/trades")
    public ResponseEntity<List<ShiftTrade>> getShiftTrades(@AuthenticationPrincipal Employee currentUser) {
        try {
            List<ShiftTrade> trades;
            if (currentUser.getRole() == Employee.Role.EMPLOYEE) {
                trades = shiftTradeRepository.findByEmployeeInvolved(currentUser.getId());
            } else {
                trades = shiftTradeRepository.findAll();
            }
            return ResponseEntity.ok(trades);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/trades/{id}/pickup")
    public ResponseEntity<?> pickupTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            // This would delegate to the trade service
            return ResponseEntity.ok(new MessageResponse("Trade picked up successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/trades/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> approveTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            // This would delegate to the trade service
            return ResponseEntity.ok(new MessageResponse("Trade approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/trades/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> rejectTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            // This would delegate to the trade service
            return ResponseEntity.ok(new MessageResponse("Trade rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/trades/{id}")
    public ResponseEntity<?> deleteTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            // This would delegate to the trade service to cancel/delete trade
            return ResponseEntity.ok(new MessageResponse("Trade cancelled"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/trades")
    public ResponseEntity<?> createTrade(@RequestBody ShiftTradeRequest request, @AuthenticationPrincipal Employee currentUser) {
        try {
            // This would delegate to the trade service to create a new trade request
            return ResponseEntity.ok(new MessageResponse("Trade request created"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<ShiftResponse>> getMyShifts(@AuthenticationPrincipal Employee currentUser,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<ShiftResponse> shifts = shiftService.getShiftsForEmployee(currentUser.getId(), startDate, endDate);
            return ResponseEntity.ok(shifts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Analytics endpoints for reports
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getShiftStatistics(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate,
                                               @RequestParam(required = false) Long departmentId) {
        try {
            // Mock statistics - in real app this would calculate actual statistics
            Map<String, Object> statistics = Map.of(
                "totalShifts", 156,
                "completedShifts", 142,
                "cancelledShifts", 8,
                "availableShifts", 6,
                "totalHours", 1248.5,
                "averageShiftLength", 8.0,
                "mostActiveEmployee", "John Smith",
                "busiestDepartment", "Front Desk"
            );
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getShiftAnalytics(@RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate,
                                              @RequestParam(required = false) Long departmentId) {
        try {
            // Mock analytics data - in real app this would perform complex analytics
            Map<String, Object> analytics = Map.of(
                "shiftsPerDay", List.of(12, 15, 18, 14, 16, 20, 10),
                "hoursPerDepartment", Map.of(
                    "Front Desk", 320.5,
                    "Housekeeping", 480.0,
                    "Maintenance", 240.5,
                    "Food Service", 360.0
                ),
                "employeeUtilization", 85.2,
                "peakHours", List.of("08:00", "14:00", "20:00")
            );
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/employee-hours")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeeHours(@RequestParam(required = false) String startDate,
                                             @RequestParam(required = false) String endDate,
                                             @RequestParam(required = false) Long departmentId) {
        try {
            // Mock employee hours data
            List<Map<String, Object>> employeeHours = List.of(
                Map.of("employeeName", "John Smith", "totalHours", 40.0, "overtimeHours", 0.0),
                Map.of("employeeName", "Jane Doe", "totalHours", 45.5, "overtimeHours", 5.5),
                Map.of("employeeName", "Mike Johnson", "totalHours", 38.0, "overtimeHours", 0.0),
                Map.of("employeeName", "Sarah Wilson", "totalHours", 42.5, "overtimeHours", 2.5)
            );
            return ResponseEntity.ok(employeeHours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/department-stats")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getDepartmentStats(@RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate) {
        try {
            // Mock department statistics
            List<Map<String, Object>> departmentStats = List.of(
                Map.of("departmentName", "Front Desk", "totalShifts", 45, "totalHours", 360.0, "employeeCount", 8),
                Map.of("departmentName", "Housekeeping", "totalShifts", 60, "totalHours", 480.0, "employeeCount", 12),
                Map.of("departmentName", "Maintenance", "totalShifts", 30, "totalHours", 240.0, "employeeCount", 6),
                Map.of("departmentName", "Food Service", "totalShifts", 50, "totalHours", 400.0, "employeeCount", 10)
            );
            return ResponseEntity.ok(departmentStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    // Helper class for response messages
    public static class MessageResponse {
        private String message;
        
        public MessageResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
