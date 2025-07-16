package com.hotel.scheduler.controller;

import com.hotel.scheduler.model.UserActionLog;
import com.hotel.scheduler.repository.UserActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class UserActionLogController {
    /**
     * UserActionLogController: Handles REST API endpoints for querying user action logs.
     *
     * Usage:
     * - Use this controller to audit and review user actions in the system.
     * - All endpoints require ADMIN role for access.
     * - Returns lists of UserActionLog entities based on filter criteria.
     *
     * Key Endpoints:
     * - GET /api/logs: Get all user action logs
     * - GET /api/logs/building/{buildingId}: Get logs by building ID
     * - GET /api/logs/user/{userUuid}: Get logs by user UUID
     * - GET /api/logs/role/{role}: Get logs by user role
     *
     * Dependencies:
     * - UserActionLogRepository: Data access for user action logs
     * - UserActionLog: Entity representing a user action log entry
     */
    private final UserActionLogRepository userActionLogRepository;

    /**
     * Returns user action logs for a specific building.
     * GET /api/logs/building/{buildingId}
     * ADMIN only.
     */
    @GetMapping("/building/{buildingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserActionLog> getLogsByBuilding(@PathVariable Long buildingId) {
        return userActionLogRepository.findByBuildingId(buildingId);
    }

    /**
     * Returns user action logs for a specific user by UUID.
     * GET /api/logs/user/{userUuid}
     * ADMIN only.
     */
    @GetMapping("/user/{userUuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserActionLog> getLogsByUser(@PathVariable String userUuid) {
        return userActionLogRepository.findByUserUuid(userUuid);
    }

    /**
     * Returns user action logs for a specific role.
     * GET /api/logs/role/{role}
     * ADMIN only.
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserActionLog> getLogsByRole(@PathVariable String role) {
        return userActionLogRepository.findByRole(role);
    }

    /**
     * Returns all user action logs in the system.
     * GET /api/logs
     * ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserActionLog> getAllLogs() {
        return userActionLogRepository.findAll();
    }
}
