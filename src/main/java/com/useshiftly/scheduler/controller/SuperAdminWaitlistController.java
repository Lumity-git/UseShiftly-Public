package com.useshiftly.scheduler.controller;

import com.useshiftly.scheduler.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for managing waitlist (super admin only).
 */
@RestController
@RequestMapping("/api/super-admin/waitlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminWaitlistController {

    private final WaitlistService waitlistService;

    /**
     * Get all waitlist entries (super admin only).
     */
    @GetMapping
    public ResponseEntity<?> getAllWaitlistEntries() {
        return ResponseEntity.ok(waitlistService.getAllWaitlistEntries());
    }

    /**
     * Get waitlist count (super admin only).
     */
    @GetMapping("/count")
    public ResponseEntity<?> getWaitlistCount() {
        return ResponseEntity.ok(java.util.Map.of("count", waitlistService.getWaitlistCount()));
    }
}
