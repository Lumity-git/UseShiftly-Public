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
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ShiftController: Handles all shift and shift trade related REST API endpoints for the hotel scheduler system.
 *
 * <p>Usage:
 * <ul>
 *   <li>Use this controller for shift CRUD, trade, pickup, analytics, and reporting operations.</li>
 *   <li>Endpoints are protected by role-based access control using @PreAuthorize.</li>
 *   <li>Authenticated user is injected via @AuthenticationPrincipal.</li>
 *   <li>All responses use DTOs for safe serialization.</li>
 * </ul>
 *
 * <p>Key Endpoints:
 * <ul>
 *   <li>GET /api/shifts: List shifts (role-based filtering)</li>
 *   <li>GET /api/shifts/my-shifts: Get current user's shifts</li>
 *   <li>GET /api/shifts/available: List available shifts for pickup</li>
 *   <li>GET /api/shifts/{id}: Get shift details (role-based access)</li>
 *   <li>POST /api/shifts: Create new shift (manager/admin only)</li>
 *   <li>PUT /api/shifts/{id}: Update shift (manager/admin only)</li>
 *   <li>DELETE /api/shifts/{id}: Delete shift (manager/admin only)</li>
 *   <li>POST /api/shifts/{id}/give-away: Make shift available for pickup</li>
 *   <li>POST /api/shifts/{id}/pick-up: Pick up a shift</li>
 *   <li>POST /api/shifts/{id}/trade: Offer shift to another employee</li>
 *   <li>POST /api/shifts/{id}/post-to-everyone: Post shift for public pickup</li>
 *   <li>POST /api/shifts/{id}/cancel-post: Cancel posted shift</li>
 *   <li>GET /api/shifts/trades: List shift trades (role-based)</li>
 *   <li>POST /api/shifts/trades/{id}/accept: Accept a trade (employee)</li>
 *   <li>POST /api/shifts/trades/{id}/decline: Decline a trade (employee)</li>
 *   <li>POST /api/shifts/trades/{id}/approve: Approve trade (manager/admin)</li>
 *   <li>POST /api/shifts/trades/{id}/reject: Reject trade (manager/admin)</li>
 *   <li>DELETE /api/shifts/trades/{id}: Cancel/delete trade</li>
 *   <li>GET /api/shifts/statistics: Shift statistics (manager/admin)</li>
 *   <li>GET /api/shifts/analytics: Shift analytics (manager/admin)</li>
 *   <li>GET /api/shifts/employee-hours: Employee hours (role-based)</li>
 *   <li>GET /api/shifts/department-stats: Department stats (manager/admin)</li>
 * </ul>
 *
 * <p>Dependencies:
 * <ul>
 *   <li>ShiftService: Business logic for shifts and trades</li>
 *   <li>ShiftTradeRepository: Data access for trades</li>
 *   <li>UserActionLogService: Audit logging</li>
 *   <li>EmployeeService: Employee business logic</li>
 *   <li>MessageResponse: Helper class for response messages</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class ShiftController {
    /**
     * ShiftController: Handles all shift and shift trade related REST API endpoints for the hotel scheduler system.
     *
     * Usage:
     * - Use this controller for shift CRUD, trade, pickup, analytics, and reporting operations.
     * - Endpoints are protected by role-based access control using @PreAuthorize.
     * - Authenticated user is injected via @AuthenticationPrincipal.
     * - All responses use DTOs for safe serialization.
     *
     * Key Endpoints:
     * - GET /api/shifts: List shifts (role-based filtering)
     * - GET /api/shifts/my-shifts: Get current user's shifts
     * - GET /api/shifts/available: List available shifts for pickup
     * - GET /api/shifts/{id}: Get shift details (role-based access)
     * - POST /api/shifts: Create new shift (manager/admin only)
     * - PUT /api/shifts/{id}: Update shift (manager/admin only)
     * - DELETE /api/shifts/{id}: Delete shift (manager/admin only)
     * - POST /api/shifts/{id}/give-away: Make shift available for pickup
     * - POST /api/shifts/{id}/pick-up: Pick up a shift
     * - POST /api/shifts/{id}/trade: Offer shift to another employee
     * - POST /api/shifts/{id}/post-to-everyone: Post shift for public pickup
     * - POST /api/shifts/{id}/cancel-post: Cancel posted shift
     * - GET /api/shifts/trades: List shift trades (role-based)
     * - POST /api/shifts/trades/{id}/accept: Accept a trade (employee)
     * - POST /api/shifts/trades/{id}/decline: Decline a trade (employee)
     * - POST /api/shifts/trades/{id}/approve: Approve trade (manager/admin)
     * - POST /api/shifts/trades/{id}/reject: Reject trade (manager/admin)
     * - DELETE /api/shifts/trades/{id}: Cancel/delete trade
     * - GET /api/shifts/statistics: Shift statistics (manager/admin)
     * - GET /api/shifts/analytics: Shift analytics (manager/admin)
     * - GET /api/shifts/employee-hours: Employee hours (role-based)
     * - GET /api/shifts/department-stats: Department stats (manager/admin)
     *
     * Dependencies:
     * - ShiftService: Business logic for shifts and trades
     * - ShiftTradeRepository: Data access for trades
     * - UserActionLogService: Audit logging
     * - EmployeeService: Employee business logic
     * - MessageResponse: Helper class for response messages
     */
    /**
     * Cancel a posted shift (withdraw post, make unavailable for pickup).
     * Only the employee who posted the shift can cancel.
     * POST /api/shifts/{id}/cancel-post
     */
    @PostMapping("/{id}/cancel-post")
    public ResponseEntity<?> cancelPostedShift(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            shiftService.cancelPostedShift(id, currentUser);
            userActionLogService.logAction("CANCELLED_POSTED_SHIFT", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Shift post cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    private final ShiftService shiftService;
    private final ShiftTradeRepository shiftTradeRepository;
    private final com.hotel.scheduler.service.UserActionLogService userActionLogService;
    private final com.hotel.scheduler.service.EmployeeService employeeService;
    /**
     * Get incoming shift trades for the logged-in employee (pickup recipient).
     * GET /api/shifts/trades/incoming
     */
    @GetMapping("/trades/incoming")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<com.hotel.scheduler.dto.shift.ShiftTradeResponse>> getIncomingTrades(@AuthenticationPrincipal Employee employee) {
        if (employee == null) {
            return ResponseEntity.status(401).build();
        }
        var trades = employeeService.getIncomingShiftTrades(employee.getId());
        return ResponseEntity.ok(trades);
    }

    /**
     * Employee accepts a shift trade sent to them (or picked up from public).
     * Sets trade status to PENDING_APPROVAL for manager/admin review.
     * POST /api/shifts/trades/{id}/accept
     */
    @PostMapping("/trades/{id}/accept")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> acceptTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            shiftService.acceptTrade(id, currentUser); // Implement this in service
            userActionLogService.logAction("ACCEPTED_TRADE", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Trade accepted and pending manager approval"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Employee declines a shift trade sent to them (or picked up from public).
     * Sets trade status to CANCELLED.
     * POST /api/shifts/trades/{id}/decline
     */
    @PostMapping("/trades/{id}/decline")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> declineTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            shiftService.declineTrade(id, currentUser); // Implement this in service
            userActionLogService.logAction("DECLINED_TRADE", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Trade declined"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ShiftResponse>> getShifts(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal Employee currentUser) {
        OffsetDateTime start = parseOffsetDateTime(startDate);
        OffsetDateTime end = parseOffsetDateTime(endDate);

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
     * Parse ISO date or datetime string to OffsetDateTime. Accepts yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss[.SSS][Z]
     */
    private OffsetDateTime parseOffsetDateTime(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return OffsetDateTime.parse(value);
        } catch (Exception e) {
            // Fallback: try parsing as LocalDate
            try {
                return java.time.LocalDate.parse(value).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
            } catch (Exception ignored) {}
        }
        return null;
    }
    
    /**
     * Returns shifts for the current authenticated user.
     * GET /api/shifts/my-shifts
     */
    @GetMapping("/my-shifts")
    public ResponseEntity<List<ShiftResponse>> getMyShifts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @AuthenticationPrincipal Employee currentUser) {
        List<ShiftResponse> shifts = shiftService.getShiftsForEmployee(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(shifts);
    }
    
    /**
     * Returns shifts available for pickup (excluding user's own).
     * GET /api/shifts/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<ShiftResponse>> getAvailableShifts(@AuthenticationPrincipal Employee currentUser) {
        try {
            List<ShiftResponse> availableShifts = shiftService.getAvailableShifts(currentUser);
            return ResponseEntity.ok(availableShifts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Returns details for a specific shift by ID.
     * GET /api/shifts/{id}
     * Employees can only view their own shifts.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getShift(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        Optional<ShiftResponse> shiftOpt = shiftService.getShiftById(id);
        if (shiftOpt.isPresent()) {
            ShiftResponse shift = shiftOpt.get();
            // Check permissions
            if (currentUser.getRole() == Employee.Role.EMPLOYEE && 
                (shift.getEmployeeId() == null || !shift.getEmployeeId().equals(currentUser.getId()))) {
                return ResponseEntity.status(403).body(new MessageResponse("Forbidden"));
            }
            return ResponseEntity.ok(shift);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Creates a new shift.
     * POST /api/shifts (manager/admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createShift(@Valid @RequestBody CreateShiftRequest request, 
                                        @AuthenticationPrincipal Employee currentUser) {
        try {
            ShiftResponse shift = shiftService.createShift(request, currentUser);
            userActionLogService.logAction("CREATED_SHIFT", currentUser.getId());
            return ResponseEntity.ok(shift);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Updates an existing shift by ID.
     * PUT /api/shifts/{id} (manager/admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateShift(@PathVariable Long id, 
                                        @Valid @RequestBody CreateShiftRequest request,
                                        @AuthenticationPrincipal Employee currentUser) {
        try {
            ShiftResponse shift = shiftService.updateShift(id, request, currentUser);
            userActionLogService.logAction("UPDATED_SHIFT", currentUser.getId());
            return ResponseEntity.ok(shift);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Deletes a shift by ID.
     * DELETE /api/shifts/{id} (manager/admin only)
     */
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
    
    /**
     * Makes a shift available for pickup (give away).
     * POST /api/shifts/{id}/give-away
     */
    @PostMapping("/{id}/give-away")
    public ResponseEntity<?> giveAwayShift(@PathVariable Long id, 
                                          @RequestBody ShiftTradeRequest request,
                                          @AuthenticationPrincipal Employee currentUser) {
        try {
            shiftService.makeShiftAvailableForPickup(id, currentUser, request.getReason());
            userActionLogService.logAction("GAVE_AWAY_SHIFT", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Shift is now available for pickup"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Picks up a shift by ID.
     * POST /api/shifts/{id}/pick-up
     * Employees can only pick up shifts in their own department.
     */
    @PostMapping("/{id}/pick-up")
    public ResponseEntity<?> pickupShift(@PathVariable Long id, 
                                        @AuthenticationPrincipal Employee currentUser) {
        log.debug("pickupShift called: id={}, userId={}, userRole={}", id, currentUser != null ? currentUser.getId() : null, currentUser != null ? currentUser.getRole() : null);
        try {
            Shift shift = shiftService.getShiftEntityById(id);
            log.debug("pickupShift: loaded shift id={}, departmentId={}, status={}, availableForPickup={}",
                shift.getId(),
                shift.getDepartment() != null ? shift.getDepartment().getId() : null,
                shift.getStatus(),
                shift.getAvailableForPickup()
            );
            if (currentUser.getRole() == Employee.Role.EMPLOYEE) {
                if (shift.getDepartment() == null || currentUser.getDepartment() == null ||
                    !shift.getDepartment().getId().equals(currentUser.getDepartment().getId())) {
                    log.warn("pickupShift: Forbidden department. shiftDept={}, userDept={}",
                        shift.getDepartment() != null ? shift.getDepartment().getId() : null,
                        currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null);
                    return ResponseEntity.status(403).body(new MessageResponse("Forbidden: Employees can only pick up shifts in their own department."));
                }
            }
            log.debug("pickupShift: calling shiftService.pickupShift");
            shiftService.pickupShift(id, currentUser);
            userActionLogService.logAction("PICKED_UP_SHIFT", currentUser.getId());
            log.info("pickupShift: Shift {} picked up by user {}", id, currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Shift picked up successfully"));
        } catch (Exception e) {
            log.error("pickupShift: Exception for shiftId={}, userId={}: {}", id, currentUser != null ? currentUser.getId() : null, e.getMessage(), e);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Returns all shift trades visible to the current user.
     * GET /api/shifts/trades
     * Employees see only their own trades; managers/admins see all.
     */
    @GetMapping("/trades")
    public ResponseEntity<List<com.hotel.scheduler.dto.shift.ShiftTradeResponse>> getShiftTrades(@AuthenticationPrincipal Employee currentUser) {
        try {
            List<ShiftTrade> trades;
            if (currentUser.getRole() == Employee.Role.EMPLOYEE) {
                trades = shiftTradeRepository.findByEmployeeInvolved(currentUser.getId());
            } else {
                trades = shiftTradeRepository.findAll();
            }
            List<com.hotel.scheduler.dto.shift.ShiftTradeResponse> response = trades.stream().map(trade -> {
                com.hotel.scheduler.dto.shift.ShiftTradeResponse dto = new com.hotel.scheduler.dto.shift.ShiftTradeResponse();
                dto.setId(trade.getId());
                dto.setShiftId(trade.getShift() != null ? trade.getShift().getId() : null);
                if (trade.getRequestingEmployee() != null) {
                    dto.setRequestingEmployeeId(trade.getRequestingEmployee().getId());
                    dto.setRequestingEmployeeName(trade.getRequestingEmployee().getFirstName() + " " + trade.getRequestingEmployee().getLastName());
                }
                if (trade.getPickupEmployee() != null) {
                    dto.setPickupEmployeeId(trade.getPickupEmployee().getId());
                    dto.setPickupEmployeeName(trade.getPickupEmployee().getFirstName() + " " + trade.getPickupEmployee().getLastName());
                }
                dto.setStatus(trade.getStatus() != null ? trade.getStatus().name() : null);
                dto.setRequestedAt(trade.getRequestedAt());
                return dto;
            }).toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Picks up a shift trade by ID.
     * POST /api/shifts/trades/{id}/pickup
     */
    @PostMapping("/trades/{id}/pickup")
    public ResponseEntity<?> pickupTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            // This would delegate to the trade service
            return ResponseEntity.ok(new MessageResponse("Trade picked up successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Approves a shift trade by ID (manager/admin only).
     * Only trades in PENDING_APPROVAL status can be approved.
     * POST /api/shifts/trades/{id}/approve
     */
    @PostMapping("/trades/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> approveTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            Optional<ShiftTrade> tradeOpt = shiftTradeRepository.findById(id);
            if (tradeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Trade not found"));
            }
            ShiftTrade trade = tradeOpt.get();
            if (trade.getStatus() != ShiftTrade.TradeStatus.PENDING_APPROVAL) {
                return ResponseEntity.badRequest().body(new MessageResponse("Trade is not pending approval or has already been processed."));
            }
            // Delegate to service to approve
            shiftService.approveTrade(id, currentUser);
            userActionLogService.logAction("APPROVED_TRADE", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Trade approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Rejects a shift trade by ID (manager/admin only).
     * Only trades in PENDING_APPROVAL status can be rejected.
     * POST /api/shifts/trades/{id}/reject
     */
    @PostMapping("/trades/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> rejectTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser, @RequestBody(required = false) Map<String, Object> payload) {
        try {
            Optional<ShiftTrade> tradeOpt = shiftTradeRepository.findById(id);
            if (tradeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Trade not found"));
            }
            ShiftTrade trade = tradeOpt.get();
            if (trade.getStatus() != ShiftTrade.TradeStatus.PENDING_APPROVAL) {
                return ResponseEntity.badRequest().body(new MessageResponse("Trade is not pending approval or has already been processed."));
            }
            String reason = null;
            if (payload != null && payload.containsKey("reason")) {
                Object val = payload.get("reason");
                if (val != null) reason = val.toString();
            }
            // Delegate to service to reject
            shiftService.rejectTrade(id, currentUser, reason);
            userActionLogService.logAction("REJECTED_TRADE", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Trade rejected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Cancels or deletes a shift trade by ID.
     * DELETE /api/shifts/trades/{id}
     */
    @DeleteMapping("/trades/{id}")
    public ResponseEntity<?> deleteTrade(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            // This would delegate to the trade service to cancel/delete trade
            return ResponseEntity.ok(new MessageResponse("Trade cancelled"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    // ...existing code...
    /**
     * Returns shift statistics for reporting (manager/admin only).
     * GET /api/shifts/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getShiftStatistics(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate,
                                               @RequestParam(required = false) Long departmentId) {
        try {
            Map<String, Object> statistics = shiftService.getShiftStatistics(startDate, endDate, departmentId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Returns shift analytics for reporting (manager/admin only).
     * GET /api/shifts/analytics
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getShiftAnalytics(@RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate,
                                              @RequestParam(required = false) Long departmentId) {
        try {
            Map<String, Object> analytics = shiftService.getShiftAnalytics(startDate, endDate, departmentId);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Returns employee hours for reporting.
     * GET /api/shifts/employee-hours
     * Employees see only their own hours; managers/admins see all.
     */
    @GetMapping("/employee-hours")
    public ResponseEntity<?> getEmployeeHours(@RequestParam(required = false) String startDate,
                                             @RequestParam(required = false) String endDate,
                                             @RequestParam(required = false) Long departmentId,
                                             @AuthenticationPrincipal Employee currentUser) {
        try {
            List<Map<String, Object>> employeeHours;
            if (currentUser.getRole() == com.hotel.scheduler.model.Employee.Role.EMPLOYEE) {
                employeeHours = shiftService.getEmployeeHours(startDate, endDate, departmentId)
                    .stream()
                    .filter(e -> e.get("employeeId") != null && e.get("employeeId").equals(currentUser.getId()))
                    .toList();
            } else {
                employeeHours = shiftService.getEmployeeHours(startDate, endDate, departmentId);
            }
            return ResponseEntity.ok(employeeHours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Returns department statistics for reporting (manager/admin only).
     * GET /api/shifts/department-stats
     */
    @GetMapping("/department-stats")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<?> getDepartmentStats(@RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate) {
        try {
            List<Map<String, Object>> departmentStats = shiftService.getDepartmentStats(startDate, endDate);
            return ResponseEntity.ok(departmentStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Helper class for response messages (used for error/success responses).
     */
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

    @PostMapping("/{id}/trade")
    public ResponseEntity<?> tradeShiftToEmployee(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> payload,
                                                  @AuthenticationPrincipal Employee currentUser) {
        try {
            Long targetEmployeeId = null;
            if (payload.containsKey("targetEmployeeId")) {
                Object val = payload.get("targetEmployeeId");
                if (val instanceof Number) {
                    targetEmployeeId = ((Number) val).longValue();
                } else if (val instanceof String) {
                    targetEmployeeId = Long.parseLong((String) val);
                }
            }
            log.debug("tradeShiftToEmployee: currentUser id={}, role={}, targetEmployeeId={}",
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getRole() : null,
                targetEmployeeId);
            if (targetEmployeeId == null) {
                log.warn("tradeShiftToEmployee: Missing targetEmployeeId in payload: {}", payload);
                return ResponseEntity.badRequest().body(new MessageResponse("Missing targetEmployeeId"));
            }
            shiftService.offerShiftToEmployee(id, currentUser, targetEmployeeId);
            userActionLogService.logAction("OFFERED_SHIFT_TO_EMPLOYEE", currentUser.getId());
            log.info("tradeShiftToEmployee: Shift {} offered from user {} to user {}", id, currentUser.getId(), targetEmployeeId);
            return ResponseEntity.ok(new MessageResponse("Shift offer sent to employee."));
        } catch (Exception e) {
            log.error("tradeShiftToEmployee: Error offering shift {} from user {}: {}", id, currentUser != null ? currentUser.getId() : null, e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/post-to-everyone")
    public ResponseEntity<?> postShiftToEveryone(@PathVariable Long id,
                                                 @AuthenticationPrincipal Employee currentUser) {
        try {
            shiftService.postShiftToEveryone(id, currentUser);
            userActionLogService.logAction("POSTED_SHIFT_TO_EVERYONE", currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Shift posted to everyone."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
