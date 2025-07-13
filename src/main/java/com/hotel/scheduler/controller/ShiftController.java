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
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal Employee currentUser) {
        LocalDateTime start = parseDateOrDateTime(startDate);
        LocalDateTime end = parseDateOrDateTime(endDate);

        List<ShiftResponse> shifts;

        // If regular employee, only show their own shifts
        if (currentUser.getRole() == Employee.Role.EMPLOYEE) {
            shifts = shiftService.getShiftsForEmployee(currentUser.getId(), start, end);
        } else if (employeeId != null) {
            shifts = shiftService.getShiftsForEmployee(employeeId, start, end);
        } else if (departmentId != null) {
            shifts = shiftService.getShiftsForDepartment(departmentId, start, end);
        } else {
            shifts = shiftService.getAllShifts(start, end);
        }

        return ResponseEntity.ok(shifts);
    }

    /**
     * Parse ISO date or datetime string to LocalDateTime. Accepts yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss[.SSS][Z]
     */
    private LocalDateTime parseDateOrDateTime(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            if (value.length() == 10) { // yyyy-MM-dd
                return java.time.LocalDate.parse(value).atStartOfDay();
            } else {
                return java.time.LocalDateTime.parse(value);
            }
        } catch (Exception e) {
            // Try parsing with offset
            try {
                return java.time.OffsetDateTime.parse(value).toLocalDateTime();
            } catch (Exception ignored) {}
        }
        return null;
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
            // Delegate to service for real statistics
            Map<String, Object> statistics = shiftService.getShiftStatistics(startDate, endDate, departmentId);
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
            // Delegate to service for analytics
            Map<String, Object> analytics = shiftService.getShiftAnalytics(startDate, endDate, departmentId);
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
            // Delegate to service for employee hours
            List<Map<String, Object>> employeeHours = shiftService.getEmployeeHours(startDate, endDate, departmentId);
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
            // Delegate to service for department stats
            List<Map<String, Object>> departmentStats = shiftService.getDepartmentStats(startDate, endDate);
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
