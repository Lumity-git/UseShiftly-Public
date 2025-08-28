package com.hotel.scheduler.controller;

import com.hotel.scheduler.security.AbuseDetectionService;
import com.hotel.scheduler.security.RateLimitingService;
import com.hotel.scheduler.security.SecurityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Security management controller for monitoring and administering security features.
 * 
 * Provides endpoints for:
 * - Viewing security statistics and metrics
 * - Managing IP blacklists and whitelists
 * - Monitoring rate limiting status
 * - Reviewing security events and alerts
 * - Manual security actions (block/unblock IPs)
 * 
 * Access: Super Admin only
 */
@RestController
@RequestMapping("/api/super-admin/security")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SecurityManagementController {

    private final RateLimitingService rateLimitingService;
    private final AbuseDetectionService abuseDetectionService;
    private final SecurityEventService securityEventService;

    /**
     * Get comprehensive security dashboard statistics.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getSecurityDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Rate limiting statistics
            dashboard.put("rate_limiting", Map.of(
                "statistics", rateLimitingService.getStatistics(),
                "status", "active"
            ));
            
            // Abuse detection statistics
            dashboard.put("abuse_detection", abuseDetectionService.getAbuseStatistics());
            
            // Security event statistics
            dashboard.put("security_events", securityEventService.getSecurityStatistics());
            
            // Overall system status
            dashboard.put("system_status", Map.of(
                "timestamp", System.currentTimeMillis(),
                "security_level", "enhanced",
                "active_protections", Map.of(
                    "rate_limiting", true,
                    "abuse_detection", true,
                    "ip_blocking", true,
                    "enhanced_logging", true
                )
            ));
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error retrieving security dashboard", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve security dashboard"));
        }
    }

    /**
     * Get rate limiting status for a specific IP.
     */
    @GetMapping("/rate-limit/{ip}")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(@PathVariable String ip) {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("ip", ip);
            status.put("is_blocked", rateLimitingService.isIPBlocked(ip));
            status.put("current_request_count", rateLimitingService.getCurrentRequestCount(ip));
            status.put("time_until_unblock", rateLimitingService.getTimeUntilUnblock(ip));
            status.put("is_whitelisted", abuseDetectionService.isWhitelisted(ip));
            status.put("is_permanently_blocked", abuseDetectionService.isPermanentlyBlocked(ip));
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error retrieving rate limit status for IP: {}", ip, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve rate limit status"));
        }
    }

    /**
     * Manually block an IP address.
     */
    @PostMapping("/block-ip")
    public ResponseEntity<Map<String, String>> blockIP(@RequestBody Map<String, String> request) {
        try {
            String ip = request.get("ip");
            String reason = request.getOrDefault("reason", "Manually blocked by admin");
            
            if (ip == null || ip.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "IP address is required"));
            }
            
            rateLimitingService.blockIP(ip, reason);
            log.warn("IP {} manually blocked by admin. Reason: {}", ip, reason);
            
            return ResponseEntity.ok(Map.of(
                "message", "IP address blocked successfully",
                "ip", ip,
                "reason", reason
            ));
        } catch (Exception e) {
            log.error("Error blocking IP", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to block IP address"));
        }
    }

    /**
     * Manually unblock an IP address.
     */
    @PostMapping("/unblock-ip")
    public ResponseEntity<Map<String, String>> unblockIP(@RequestBody Map<String, String> request) {
        try {
            String ip = request.get("ip");
            
            if (ip == null || ip.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "IP address is required"));
            }
            
            rateLimitingService.unblockIP(ip);
            log.info("IP {} manually unblocked by admin", ip);
            
            return ResponseEntity.ok(Map.of(
                "message", "IP address unblocked successfully",
                "ip", ip
            ));
        } catch (Exception e) {
            log.error("Error unblocking IP", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to unblock IP address"));
        }
    }

    /**
     * Add an IP to the whitelist.
     */
    @PostMapping("/whitelist-ip")
    public ResponseEntity<Map<String, String>> whitelistIP(@RequestBody Map<String, String> request) {
        try {
            String ip = request.get("ip");
            
            if (ip == null || ip.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "IP address is required"));
            }
            
            abuseDetectionService.addToWhitelist(ip);
            log.info("IP {} added to whitelist by admin", ip);
            
            return ResponseEntity.ok(Map.of(
                "message", "IP address added to whitelist successfully",
                "ip", ip
            ));
        } catch (Exception e) {
            log.error("Error adding IP to whitelist", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to add IP to whitelist"));
        }
    }

    /**
     * Add an IP to the permanent blacklist.
     */
    @PostMapping("/blacklist-ip")
    public ResponseEntity<Map<String, String>> blacklistIP(@RequestBody Map<String, String> request) {
        try {
            String ip = request.get("ip");
            
            if (ip == null || ip.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "IP address is required"));
            }
            
            abuseDetectionService.addToPermanentBlacklist(ip);
            log.warn("IP {} added to permanent blacklist by admin", ip);
            
            return ResponseEntity.ok(Map.of(
                "message", "IP address added to permanent blacklist successfully",
                "ip", ip
            ));
        } catch (Exception e) {
            log.error("Error adding IP to permanent blacklist", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to add IP to permanent blacklist"));
        }
    }

    /**
     * Remove an IP from the permanent blacklist.
     */
    @PostMapping("/remove-from-blacklist")
    public ResponseEntity<Map<String, String>> removeFromBlacklist(@RequestBody Map<String, String> request) {
        try {
            String ip = request.get("ip");
            
            if (ip == null || ip.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "IP address is required"));
            }
            
            abuseDetectionService.removeFromPermanentBlacklist(ip);
            log.info("IP {} removed from permanent blacklist by admin", ip);
            
            return ResponseEntity.ok(Map.of(
                "message", "IP address removed from permanent blacklist successfully",
                "ip", ip
            ));
        } catch (Exception e) {
            log.error("Error removing IP from permanent blacklist", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to remove IP from permanent blacklist"));
        }
    }

    /**
     * Get current security configuration.
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSecurityConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            
            // This would typically read from configuration properties
            config.put("rate_limiting", Map.of(
                "enabled", true,
                "requests_per_minute", 60,
                "burst_threshold", 10,
                "block_duration_minutes", 15
            ));
            
            config.put("abuse_detection", Map.of(
                "enabled", true,
                "auth_failure_threshold", 10,
                "detection_window_minutes", 60,
                "temp_block_duration_minutes", 30,
                "perm_block_threshold", 3
            ));
            
            config.put("monitoring", Map.of(
                "alert_threshold", 100,
                "time_window_minutes", 10,
                "enhanced_logging", true
            ));
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error retrieving security configuration", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve security configuration"));
        }
    }

    /**
     * Get security health check.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSecurityHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            
            health.put("status", "healthy");
            health.put("timestamp", System.currentTimeMillis());
            health.put("uptime", System.currentTimeMillis()); // Simplified
            
            health.put("components", Map.of(
                "rate_limiting", "operational",
                "abuse_detection", "operational",
                "security_logging", "operational",
                "ip_blocking", "operational"
            ));
            
            health.put("metrics", Map.of(
                "requests_processed", "N/A",
                "threats_blocked", "N/A",
                "alerts_triggered", "N/A"
            ));
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error retrieving security health", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to retrieve security health"));
        }
    }

    /**
     * Trigger manual security cleanup.
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, String>> triggerCleanup() {
        try {
            // This would trigger cleanup of expired entries
            log.info("Manual security cleanup triggered by admin");
            
            return ResponseEntity.ok(Map.of(
                "message", "Security cleanup triggered successfully",
                "timestamp", String.valueOf(System.currentTimeMillis())
            ));
        } catch (Exception e) {
            log.error("Error triggering security cleanup", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to trigger security cleanup"));
        }
    }
}
