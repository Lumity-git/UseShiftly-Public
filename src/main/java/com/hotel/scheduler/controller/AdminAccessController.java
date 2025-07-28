package com.hotel.scheduler.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.hotel.scheduler.dto.AdminAccessRequest;
import com.hotel.scheduler.service.AdminAccessService;

@RestController
@RequestMapping("/api/auth")
public class AdminAccessController {
    // Simple in-memory rate limiting: max 5 requests per 5 minutes per IP
    private final java.util.concurrent.ConcurrentHashMap<String, java.util.List<Long>> requestTimestamps = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 5 * 60 * 1000; // 5 minutes

    private boolean isRateLimited(String ip) {
        long now = System.currentTimeMillis();
        requestTimestamps.putIfAbsent(ip, new java.util.ArrayList<>());
        java.util.List<Long> timestamps = requestTimestamps.get(ip);
        timestamps.removeIf(ts -> ts < now - WINDOW_MS);
        if (timestamps.size() >= MAX_REQUESTS) {
            return true;
        }
        timestamps.add(now);
        return false;
    }

    @Autowired
    private AdminAccessService adminAccessService;

    @PostMapping("/request-admin-access")
    public ResponseEntity<?> requestAdminAccess(@RequestBody AdminAccessRequest request) {
        String ip = getClientIp();
        if (isRateLimited(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Please wait before trying again.");
        }
        boolean sent = adminAccessService.sendVerificationCode(request);
        if (sent) {
            return ResponseEntity.ok("Verification code sent to email.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send verification code.");
        }
    }

    @PostMapping("/verify-admin-code")
    public ResponseEntity<?> verifyAdminCode(@RequestBody com.hotel.scheduler.dto.AdminCodeVerificationRequest req) {
        String ip = getClientIp();
        if (isRateLimited(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Please wait before trying again.");
        }
        boolean valid = adminAccessService.verifyCode(req.getEmail(), req.getCode());
        if (valid) {
            return ResponseEntity.ok("Code verified successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired code.");
        }

    }

    // Helper to get client IP address from request
    private String getClientIp() {
        try {
            return ((ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                .getRequest().getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
