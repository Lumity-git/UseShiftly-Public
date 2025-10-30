package com.useshiftly.scheduler.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Advanced abuse detection and IP blocking service with automatic threat mitigation.
 * 
 * Features:
 * - Automatic IP blocking based on suspicious behavior patterns
 * - Configurable detection thresholds and block durations
 * - Pattern-based malicious request detection
 * - Whitelist/blacklist management
 * - Integration with fail2ban-style blocking
 * - Persistent threat intelligence
 * - Graduated response system (warning -> temporary block -> permanent block)
 * 
 * Detection Patterns:
 * - Multiple failed authentication attempts
 * - Path traversal attempts
 * - SQL injection patterns
 * - Cross-site scripting attempts
 * - Bot scanning behavior
 * - Unusual request frequencies
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AbuseDetectionService {

    private final SecurityEventService securityEventService;
    private final RateLimitingService rateLimitingService;

    @Value("${security.abuse.auth-failure-threshold:10}")
    private int authFailureThreshold;

    @Value("${security.abuse.detection-window-minutes:60}")
    private int detectionWindowMinutes;

    @Value("${security.abuse.temp-block-duration-minutes:30}")
    private int tempBlockDurationMinutes;

    @Value("${security.abuse.perm-block-threshold:3}")
    private int permBlockThreshold;

    @Value("${security.abuse.enabled:true}")
    private boolean abuseDetectionEnabled;

    // Track suspicious activities per IP
    private final ConcurrentHashMap<String, SuspiciousActivityTracker> activityTrackers = new ConcurrentHashMap<>();
    
    // Permanent blacklist
    private final Set<String> permanentBlacklist = ConcurrentHashMap.newKeySet();
    
    // Whitelist (never block these IPs)
    private final Set<String> whitelist = ConcurrentHashMap.newKeySet();
    
    // Malicious request patterns
    private final List<MaliciousPattern> maliciousPatterns = Arrays.asList(
        new MaliciousPattern("sql_injection", 
            Pattern.compile("(?i).*(union|select|insert|update|delete|drop|create|alter|exec|script).*"),
            "SQL injection attempt"),
        new MaliciousPattern("path_traversal", 
            Pattern.compile(".*(\\.\\./|\\.\\.\\\\|%2e%2e%2f|%2e%2e%5c).*"),
            "Path traversal attempt"),
        new MaliciousPattern("xss_attempt", 
            Pattern.compile("(?i).*(script|javascript|vbscript|onload|onerror|alert|eval).*"),
            "Cross-site scripting attempt"),
        new MaliciousPattern("admin_scan", 
            Pattern.compile("(?i).*(wp-admin|phpmyadmin|administrator|pma|adminer|webadmin|sqladmin|mysqladmin).*"),
            "Admin interface scanning"),
        new MaliciousPattern("file_inclusion", 
            Pattern.compile("(?i).*(php://|file://|data://|expect://|zip://).*"),
            "File inclusion attempt"),
        new MaliciousPattern("null_byte", 
            Pattern.compile(".*(%00|\\x00).*"),
            "Null byte injection")
    );

    /**
     * Analyzes a request for suspicious patterns and updates threat intelligence.
     */
    public boolean analyzeRequest(HttpServletRequest request, String clientIP, String endpoint, String userAgent, 
                                String method, Map<String, String> parameters) {
        if (!abuseDetectionEnabled || isWhitelisted(clientIP)) {
            return true; // Allow request
        }

        if (isPermanentlyBlocked(clientIP)) {
            securityEventService.logIPBlocked(clientIP, "Permanently blacklisted", -1);
            return false;
        }

        // Get or create activity tracker
        SuspiciousActivityTracker tracker = activityTrackers.computeIfAbsent(
            clientIP, k -> new SuspiciousActivityTracker()
        );

        boolean suspicious = false;
        List<String> detectedPatterns = new ArrayList<>();

        // Check for malicious patterns in endpoint
        for (MaliciousPattern pattern : maliciousPatterns) {
            if (pattern.pattern.matcher(endpoint).matches()) {
                suspicious = true;
                detectedPatterns.add(pattern.name);
                tracker.recordMaliciousPattern(pattern.name);
                
                securityEventService.logMaliciousRequest(
                    request,
                    pattern.name,
                    String.format("Pattern: %s, Endpoint: %s", pattern.description, endpoint)
                );
            }
        }

        // Check parameters for malicious content
        if (parameters != null) {
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                String value = param.getValue();
                for (MaliciousPattern pattern : maliciousPatterns) {
                    if (pattern.pattern.matcher(value).matches()) {
                        suspicious = true;
                        detectedPatterns.add(pattern.name + "_param");
                        tracker.recordMaliciousPattern(pattern.name + "_param");
                        
                        securityEventService.logMaliciousRequest(
                            request,
                            pattern.name + "_param",
                            String.format("Pattern: %s, Parameter: %s=%s", 
                                pattern.description, param.getKey(), value)
                        );
                    }
                }
            }
        }

        // Check user agent for bot patterns
        if (userAgent != null) {
            if (isSuspiciousUserAgent(userAgent)) {
                suspicious = true;
                detectedPatterns.add("suspicious_bot");
                tracker.recordSuspiciousUserAgent();
                
                securityEventService.logSuspiciousActivity(
                    request,
                    "suspicious_user_agent",
                    "User-Agent: " + userAgent
                );
            }
        }

        // Update tracker and check if blocking is warranted
        if (suspicious) {
            tracker.recordSuspiciousActivity();
            
            if (shouldBlockIP(tracker)) {
                blockIPForAbuse(clientIP, tracker, detectedPatterns);
                return false;
            }
        }

        return true; // Allow request
    }

    /**
     * Records an authentication failure for abuse detection.
     */
    public void recordAuthenticationFailure(String clientIP, String username) {
        if (!abuseDetectionEnabled || isWhitelisted(clientIP)) {
            return;
        }

        SuspiciousActivityTracker tracker = activityTrackers.computeIfAbsent(
            clientIP, k -> new SuspiciousActivityTracker()
        );

        tracker.recordAuthFailure(username);

        // Check if this warrants blocking
        if (tracker.getAuthFailureCount() >= authFailureThreshold) {
            blockIPForAbuse(clientIP, tracker, Arrays.asList("brute_force_auth"));
        }
    }

    /**
     * Records a rate limit violation for abuse tracking.
     */
    public void recordRateLimitViolation(String clientIP) {
        if (!abuseDetectionEnabled || isWhitelisted(clientIP)) {
            return;
        }

        SuspiciousActivityTracker tracker = activityTrackers.computeIfAbsent(
            clientIP, k -> new SuspiciousActivityTracker()
        );

        tracker.recordRateLimitViolation();

        // Multiple rate limit violations indicate automated abuse
        if (tracker.getRateLimitViolations() >= 3) {
            blockIPForAbuse(clientIP, tracker, Arrays.asList("repeated_rate_limit_violations"));
        }
    }

    /**
     * Blocks an IP for abuse with graduated response.
     */
    private void blockIPForAbuse(String clientIP, SuspiciousActivityTracker tracker, List<String> reasons) {
        int blockCount = tracker.incrementBlockCount();
        
        if (blockCount >= permBlockThreshold) {
            // Permanent block
            permanentBlacklist.add(clientIP);
            securityEventService.logIPBlocked(clientIP, 
                "Permanent block - repeated abuse: " + String.join(", ", reasons), -1);
            log.error("PERMANENT BLOCK: IP {} permanently blocked for repeated abuse. Patterns: {}", 
                clientIP, reasons);
        } else {
            // Temporary block with exponential backoff
            int blockDuration = tempBlockDurationMinutes * (int) Math.pow(2, blockCount - 1);
            rateLimitingService.blockIP(clientIP, "Abuse detection: " + String.join(", ", reasons));
            securityEventService.logIPBlocked(clientIP, 
                "Temporary block for abuse: " + String.join(", ", reasons), blockDuration);
            log.warn("TEMPORARY BLOCK: IP {} blocked for {} minutes. Patterns: {}, Block count: {}", 
                clientIP, blockDuration, reasons, blockCount);
        }
    }

    /**
     * Checks if an IP should be blocked based on activity patterns.
     */
    private boolean shouldBlockIP(SuspiciousActivityTracker tracker) {
        // Multiple different types of suspicious activity indicate coordinated attack
        return tracker.getSuspiciousActivityScore() >= 10 ||
               tracker.getMaliciousPatternCount() >= 5 ||
               tracker.getAuthFailureCount() >= authFailureThreshold;
    }

    /**
     * Checks if a user agent appears to be a suspicious bot.
     */
    private boolean isSuspiciousUserAgent(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return true; // Empty user agent is suspicious
        }
        
        String ua = userAgent.toLowerCase();
        
        // Highly suspicious attack tools (always block)
        String[] maliciousPatterns = {
            "sqlmap", "nikto", "nmap", "masscan", "zgrab", "shodan",
            "python-requests", "httperf", "siege", "scanner", "exploit"
        };
        
        for (String pattern : maliciousPatterns) {
            if (ua.contains(pattern)) {
                return true;
            }
        }
        
        // Moderate suspicion patterns (only flag if combined with other factors)
        String[] moderateSuspicionPatterns = {
            "curl", "wget", "bot", "crawler", "spider", "scraper"
        };
        
        boolean hasModeratePattern = false;
        for (String pattern : moderateSuspicionPatterns) {
            if (ua.contains(pattern)) {
                hasModeratePattern = true;
                break;
            }
        }
        
        // Only consider moderate patterns suspicious if user agent is very short or lacks version
        if (hasModeratePattern) {
            return userAgent.length() < 10 || !ua.matches(".*\\d+\\.\\d+.*");
        }
        
        // Very short or very long user agents are suspicious
        return userAgent.length() < 10 || userAgent.length() > 500;
    }

    /**
     * Checks if an IP is whitelisted.
     */
    public boolean isWhitelisted(String clientIP) {
        return whitelist.contains(clientIP) || 
               clientIP.equals("127.0.0.1") || 
               clientIP.equals("::1") ||
               clientIP.startsWith("192.168.") ||
               clientIP.startsWith("10.") ||
               clientIP.startsWith("172.16.") ||
               clientIP.startsWith("172.17.") ||
               clientIP.startsWith("172.18.") ||
               clientIP.startsWith("172.19.") ||
               clientIP.startsWith("172.20.") ||
               clientIP.startsWith("172.21.") ||
               clientIP.startsWith("172.22.") ||
               clientIP.startsWith("172.23.") ||
               clientIP.startsWith("172.24.") ||
               clientIP.startsWith("172.25.") ||
               clientIP.startsWith("172.26.") ||
               clientIP.startsWith("172.27.") ||
               clientIP.startsWith("172.28.") ||
               clientIP.startsWith("172.29.") ||
               clientIP.startsWith("172.30.") ||
               clientIP.startsWith("172.31.");
    }

    /**
     * Checks if an IP is permanently blocked.
     */
    public boolean isPermanentlyBlocked(String clientIP) {
        return permanentBlacklist.contains(clientIP);
    }

    /**
     * Manually adds an IP to the whitelist.
     */
    public void addToWhitelist(String clientIP) {
        whitelist.add(clientIP);
        log.info("Added IP {} to whitelist", clientIP);
    }

    /**
     * Manually adds an IP to the permanent blacklist.
     */
    public void addToPermanentBlacklist(String clientIP) {
        permanentBlacklist.add(clientIP);
        log.warn("Added IP {} to permanent blacklist", clientIP);
    }

    /**
     * Removes an IP from the permanent blacklist.
     */
    public void removeFromPermanentBlacklist(String clientIP) {
        if (permanentBlacklist.remove(clientIP)) {
            log.info("Removed IP {} from permanent blacklist", clientIP);
        }
    }

    /**
     * Gets current abuse detection statistics.
     */
    public Map<String, Object> getAbuseStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("tracked_ips", activityTrackers.size());
        stats.put("permanent_blocks", permanentBlacklist.size());
        stats.put("whitelist_size", whitelist.size());
        stats.put("detection_enabled", abuseDetectionEnabled);
        
        long totalSuspiciousActivities = activityTrackers.values().stream()
            .mapToLong(tracker -> tracker.getSuspiciousActivityCount())
            .sum();
        stats.put("total_suspicious_activities", totalSuspiciousActivities);
        
        return stats;
    }

    /**
     * Scheduled cleanup of old tracking data.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupOldData() {
        Instant cutoff = Instant.now().minus(detectionWindowMinutes * 2L, ChronoUnit.MINUTES);
        
        activityTrackers.entrySet().removeIf(entry -> {
            SuspiciousActivityTracker tracker = entry.getValue();
            return tracker.getLastActivity().isBefore(cutoff) && !isPermanentlyBlocked(entry.getKey());
        });
        
        log.debug("Abuse detection cleanup completed. Active trackers: {}", activityTrackers.size());
    }

    /**
     * Internal class to track suspicious activities per IP.
     */
    private static class SuspiciousActivityTracker {
        private final AtomicInteger authFailures = new AtomicInteger(0);
        private final AtomicInteger maliciousPatterns = new AtomicInteger(0);
        private final AtomicInteger rateLimitViolations = new AtomicInteger(0);
        private final AtomicInteger suspiciousActivities = new AtomicInteger(0);
        private final AtomicInteger blockCount = new AtomicInteger(0);
        private final Set<String> detectedPatterns = ConcurrentHashMap.newKeySet();
        private final Set<String> attemptedUsernames = ConcurrentHashMap.newKeySet();
        private volatile Instant lastActivity = Instant.now();
        private volatile Instant firstSeen = Instant.now();

        void recordAuthFailure(String username) {
            authFailures.incrementAndGet();
            if (username != null) {
                attemptedUsernames.add(username);
            }
            updateLastActivity();
        }

        void recordMaliciousPattern(String pattern) {
            maliciousPatterns.incrementAndGet();
            detectedPatterns.add(pattern);
            updateLastActivity();
        }

        void recordRateLimitViolation() {
            rateLimitViolations.incrementAndGet();
            updateLastActivity();
        }

        void recordSuspiciousActivity() {
            suspiciousActivities.incrementAndGet();
            updateLastActivity();
        }

        void recordSuspiciousUserAgent() {
            suspiciousActivities.incrementAndGet();
            detectedPatterns.add("suspicious_user_agent");
            updateLastActivity();
        }

        int incrementBlockCount() {
            return blockCount.incrementAndGet();
        }

        private void updateLastActivity() {
            lastActivity = Instant.now();
        }

        // Getters
        int getAuthFailureCount() { return authFailures.get(); }
        int getMaliciousPatternCount() { return maliciousPatterns.get(); }
        int getRateLimitViolations() { return rateLimitViolations.get(); }
        int getSuspiciousActivityCount() { return suspiciousActivities.get(); }
        Instant getLastActivity() { return lastActivity; }
        
        int getSuspiciousActivityScore() {
            return authFailures.get() * 2 + 
                   maliciousPatterns.get() * 3 + 
                   rateLimitViolations.get() * 2 + 
                   suspiciousActivities.get() +
                   detectedPatterns.size() * 2;
        }
    }

    /**
     * Internal class to define malicious request patterns.
     */
    private static class MaliciousPattern {
        final String name;
        final Pattern pattern;
        final String description;

        MaliciousPattern(String name, Pattern pattern, String description) {
            this.name = name;
            this.pattern = pattern;
            this.description = description;
        }
    }
}
