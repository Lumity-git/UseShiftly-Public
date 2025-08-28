package com.hotel.scheduler.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced security event logging service with structured logging and monitoring.
 * 
 * Features:
 * - Structured JSON logging for security events
 * - Event categorization (authentication, authorization, abuse, etc.)
 * - Detailed request context capture
 * - Anomaly detection and alerting
 * - Centralized log formatting
 * - Performance metrics tracking
 * 
 * Log Format:
 * {
 *   "timestamp": "2025-01-01T12:00:00Z",
 *   "event_type": "AUTHENTICATION_FAILURE",
 *   "severity": "WARN",
 *   "client_ip": "192.168.1.100",
 *   "user_agent": "Mozilla/5.0...",
 *   "endpoint": "/api/auth/login",
 *   "method": "POST",
 *   "reason": "Invalid credentials",
 *   "session_id": "abc123",
 *   "request_id": "req-456",
 *   "additional_data": {}
 * }
 */
@Service
@Slf4j
public class SecurityEventService {

    @Value("${security.monitoring.alert-threshold:100}")
    private int alertThreshold;

    @Value("${security.monitoring.time-window-minutes:10}")
    private int timeWindowMinutes;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ISO_INSTANT;
    
    // Counters for anomaly detection
    private final AtomicLong authFailureCount = new AtomicLong(0);
    private final AtomicLong abuseAttemptCount = new AtomicLong(0);
    private final AtomicLong rateLimitViolationCount = new AtomicLong(0);
    private volatile long lastAlertTime = 0;
    
    /**
     * Security event types for categorization.
     */
    public enum EventType {
        AUTHENTICATION_SUCCESS,
        AUTHENTICATION_FAILURE,
        AUTHORIZATION_FAILURE,
        RATE_LIMIT_VIOLATION,
        SUSPICIOUS_ACTIVITY,
        IP_BLOCKED,
        IP_UNBLOCKED,
        BRUTE_FORCE_ATTEMPT,
        MALICIOUS_REQUEST,
        SYSTEM_ACCESS,
        ADMIN_ACTION
    }
    
    /**
     * Event severity levels.
     */
    public enum Severity {
        INFO, WARN, ERROR, CRITICAL
    }

    /**
     * Logs an authentication failure event.
     */
    public void logAuthenticationFailure(HttpServletRequest request, String username, String reason) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("username", username);
        additionalData.put("attempted_endpoint", request.getRequestURI());
        
        logSecurityEvent(
            EventType.AUTHENTICATION_FAILURE,
            Severity.WARN,
            request,
            reason,
            additionalData
        );
        
        // Increment counter for anomaly detection
        long count = authFailureCount.incrementAndGet();
        checkForAnomalies("authentication_failures", count);
    }

    /**
     * Logs an authentication success event.
     */
    public void logAuthenticationSuccess(HttpServletRequest request, String username) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("username", username);
        
        logSecurityEvent(
            EventType.AUTHENTICATION_SUCCESS,
            Severity.INFO,
            request,
            "User authenticated successfully",
            additionalData
        );
    }

    /**
     * Logs authorization failure (403 Forbidden).
     */
    public void logAuthorizationFailure(HttpServletRequest request, String username, String requiredRole) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("username", username);
        additionalData.put("required_role", requiredRole);
        additionalData.put("attempted_endpoint", request.getRequestURI());
        
        logSecurityEvent(
            EventType.AUTHORIZATION_FAILURE,
            Severity.WARN,
            request,
            "Insufficient permissions",
            additionalData
        );
    }

    /**
     * Logs rate limiting violations.
     */
    public void logRateLimitViolation(HttpServletRequest request, int requestCount, int limit) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("request_count", requestCount);
        additionalData.put("rate_limit", limit);
        additionalData.put("time_window", "1 minute");
        
        logSecurityEvent(
            EventType.RATE_LIMIT_VIOLATION,
            Severity.WARN,
            request,
            "Rate limit exceeded",
            additionalData
        );
        
        long count = rateLimitViolationCount.incrementAndGet();
        checkForAnomalies("rate_limit_violations", count);
    }

    /**
     * Logs IP blocking events.
     */
    public void logIPBlocked(String clientIP, String reason, int durationMinutes) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("block_duration_minutes", durationMinutes);
        additionalData.put("block_reason", reason);
        
        logSecurityEvent(
            EventType.IP_BLOCKED,
            Severity.ERROR,
            clientIP,
            null,
            null,
            null,
            "IP address blocked",
            additionalData
        );
    }

    /**
     * Logs suspicious activity detection.
     */
    public void logSuspiciousActivity(HttpServletRequest request, String activityType, String details) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("activity_type", activityType);
        additionalData.put("details", details);
        
        logSecurityEvent(
            EventType.SUSPICIOUS_ACTIVITY,
            Severity.ERROR,
            request,
            "Suspicious activity detected",
            additionalData
        );
        
        long count = abuseAttemptCount.incrementAndGet();
        checkForAnomalies("suspicious_activities", count);
    }

    /**
     * Logs malicious request patterns.
     */
    public void logMaliciousRequest(HttpServletRequest request, String pattern, String details) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("attack_pattern", pattern);
        additionalData.put("details", details);
        
        logSecurityEvent(
            EventType.MALICIOUS_REQUEST,
            Severity.CRITICAL,
            request,
            "Malicious request detected",
            additionalData
        );
    }

    /**
     * Logs admin actions for audit trail.
     */
    public void logAdminAction(HttpServletRequest request, String adminUsername, String action, String target) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("admin_username", adminUsername);
        additionalData.put("action", action);
        additionalData.put("target", target);
        
        logSecurityEvent(
            EventType.ADMIN_ACTION,
            Severity.INFO,
            request,
            "Administrative action performed",
            additionalData
        );
    }

    /**
     * Core method to log security events with structured format.
     */
    public void logSecurityEvent(EventType eventType, Severity severity, HttpServletRequest request, 
                                String reason, Map<String, Object> additionalData) {
        String clientIP = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        
        logSecurityEvent(eventType, severity, clientIP, userAgent, endpoint, method, reason, additionalData);
    }
    
    /**
     * Core method to log security events with all parameters.
     */
    public void logSecurityEvent(EventType eventType, Severity severity, String clientIP, 
                                String userAgent, String endpoint, String method, String reason, 
                                Map<String, Object> additionalData) {
        try {
            Map<String, Object> logEntry = new HashMap<>();
            logEntry.put("timestamp", timestampFormatter.format(Instant.now().atOffset(ZoneOffset.UTC)));
            logEntry.put("event_type", eventType.name());
            logEntry.put("severity", severity.name());
            logEntry.put("client_ip", clientIP);
            logEntry.put("user_agent", userAgent);
            logEntry.put("endpoint", endpoint);
            logEntry.put("method", method);
            logEntry.put("reason", reason);
            logEntry.put("request_id", generateRequestId());
            
            if (additionalData != null && !additionalData.isEmpty()) {
                logEntry.put("additional_data", additionalData);
            }
            
            String jsonLog = objectMapper.writeValueAsString(logEntry);
            
            // Log with appropriate level
            switch (severity) {
                case INFO -> log.info("SECURITY_EVENT: {}", jsonLog);
                case WARN -> log.warn("SECURITY_EVENT: {}", jsonLog);
                case ERROR -> log.error("SECURITY_EVENT: {}", jsonLog);
                case CRITICAL -> log.error("CRITICAL_SECURITY_EVENT: {}", jsonLog);
            }
            
        } catch (Exception e) {
            log.error("Failed to log security event", e);
        }
    }

    /**
     * Extracts client IP from request, handling proxy headers.
     */
    private String getClientIP(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        // Try Cloudflare headers first
        String clientIP = request.getHeader("CF-Connecting-IP");
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getHeader("X-Forwarded-For");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getHeader("X-Real-IP");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getHeader("X-Originating-IP");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getHeader("X-Client-IP");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getRemoteAddr();
        }
        
        // Handle comma-separated IPs in X-Forwarded-For (get the first one - original client)
        if (clientIP != null && clientIP.contains(",")) {
            clientIP = clientIP.split(",")[0].trim();
        }
        
        return clientIP;
    }

    /**
     * Generates a unique request ID for tracking.
     */
    private String generateRequestId() {
        return "req-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * Checks for anomalous patterns and triggers alerts.
     */
    private void checkForAnomalies(String eventCategory, long count) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastAlert = currentTime - lastAlertTime;
        
        // Only check for anomalies if enough time has passed since last alert
        if (timeSinceLastAlert > timeWindowMinutes * 60 * 1000) {
            if (count > alertThreshold) {
                triggerSecurityAlert(eventCategory, count);
                lastAlertTime = currentTime;
                
                // Reset counter after alert
                switch (eventCategory) {
                    case "authentication_failures" -> authFailureCount.set(0);
                    case "rate_limit_violations" -> rateLimitViolationCount.set(0);
                    case "suspicious_activities" -> abuseAttemptCount.set(0);
                }
            }
        }
    }

    /**
     * Triggers a security alert for unusual activity.
     */
    private void triggerSecurityAlert(String eventCategory, long count) {
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("category", eventCategory);
        alertData.put("count", count);
        alertData.put("threshold", alertThreshold);
        alertData.put("time_window_minutes", timeWindowMinutes);
        
        logSecurityEvent(
            EventType.SUSPICIOUS_ACTIVITY,
            Severity.CRITICAL,
            "SYSTEM",
            "Security Monitor",
            "/system/alert",
            "ALERT",
            "Anomalous activity detected - threshold exceeded",
            alertData
        );
        
        // In a production system, this could also:
        // - Send email alerts
        // - Trigger webhook notifications
        // - Update monitoring dashboards
        // - Automatically apply additional security measures
    }

    /**
     * Gets current security statistics.
     */
    public Map<String, Long> getSecurityStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("authentication_failures", authFailureCount.get());
        stats.put("rate_limit_violations", rateLimitViolationCount.get());
        stats.put("abuse_attempts", abuseAttemptCount.get());
        return stats;
    }
}
