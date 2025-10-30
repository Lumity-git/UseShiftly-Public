package com.useshiftly.scheduler.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced security filter that implements comprehensive threat protection.
 * 
 * This filter runs before the JWT authentication filter and provides:
 * - Rate limiting per IP address
 * - Abuse detection and automatic blocking
 * - Malicious request pattern detection
 * - Enhanced security event logging
 * - Request sanitization and validation
 * 
 * Order: Runs first in the filter chain (Order 1) to block threats early
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class EnhancedSecurityFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final AbuseDetectionService abuseDetectionService;
    private final SecurityEventService securityEventService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String clientIP = getClientIP(request);
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        
        // Debug logging for IP detection (remove in production)
        if (log.isDebugEnabled()) {
            log.debug("IP Headers - CF-Connecting-IP: {}, X-Forwarded-For: {}, X-Real-IP: {}, RemoteAddr: {}, Final IP: {}", 
                request.getHeader("CF-Connecting-IP"),
                request.getHeader("X-Forwarded-For"), 
                request.getHeader("X-Real-IP"),
                request.getRemoteAddr(),
                clientIP);
        }
        
        log.debug("EnhancedSecurityFilter: Processing request from {} to {} {}", clientIP, method, endpoint);

        try {
            // Skip security checks for static resources, whitelisted paths, and auth endpoints
            if (isStaticResource(endpoint) || abuseDetectionService.isWhitelisted(clientIP) || isAuthEndpoint(endpoint)) {
                log.debug("EnhancedSecurityFilter: Skipping security checks for {} (static/whitelisted/auth)", endpoint);
                filterChain.doFilter(request, response);
                return;
            }

            // 1. Check if IP is permanently blocked
            if (abuseDetectionService.isPermanentlyBlocked(clientIP)) {
                handleBlockedIP(response, clientIP, "Permanently blacklisted");
                return;
            }

            // 2. Rate limiting check
            if (!rateLimitingService.isRequestAllowed(clientIP)) {
                handleRateLimitViolation(request, response, clientIP);
                return;
            }

            // 3. Abuse detection analysis
            Map<String, String> parameters = extractParameters(request);
            if (!abuseDetectionService.analyzeRequest(request, clientIP, endpoint, userAgent, method, parameters)) {
                handleAbuseDetection(response, clientIP, "Malicious request pattern detected");
                return;
            }

            // 4. Additional security headers
            addSecurityHeaders(response);

            // 5. Log successful security check for monitoring
            if (isSecuritySensitiveEndpoint(endpoint)) {
                securityEventService.logSecurityEvent(
                    SecurityEventService.EventType.SYSTEM_ACCESS,
                    SecurityEventService.Severity.INFO,
                    request,
                    "Access to security-sensitive endpoint",
                    Map.of("endpoint_category", "sensitive")
                );
            }

            // Continue with filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("EnhancedSecurityFilter: Error processing request from {} to {}: {}", 
                clientIP, endpoint, e.getMessage(), e);
            
            securityEventService.logSecurityEvent(
                SecurityEventService.EventType.SUSPICIOUS_ACTIVITY,
                SecurityEventService.Severity.ERROR,
                request,
                "Security filter error: " + e.getMessage(),
                Map.of("error_type", e.getClass().getSimpleName())
            );
            
            handleInternalError(response);
        }
    }

    /**
     * Handles rate limit violations.
     */
    private void handleRateLimitViolation(HttpServletRequest request, HttpServletResponse response, 
                                          String clientIP) throws IOException {
        int currentCount = rateLimitingService.getCurrentRequestCount(clientIP);
        long timeUntilUnblock = rateLimitingService.getTimeUntilUnblock(clientIP);
        
        securityEventService.logRateLimitViolation(request, currentCount, 60); // 60 requests per minute limit
        abuseDetectionService.recordRateLimitViolation(clientIP);
        
        response.setStatus(429); // 429 Too Many Requests
        response.setContentType("application/json");
        response.setHeader("Retry-After", String.valueOf(timeUntilUnblock));
        response.setHeader("X-Rate-Limit-Remaining", "0");
        response.setHeader("X-Rate-Limit-Reset", String.valueOf(System.currentTimeMillis() + (timeUntilUnblock * 1000)));
        
        String jsonResponse = String.format(
            "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests from IP %s\",\"retry_after\":%d}",
            clientIP, timeUntilUnblock);
        response.getWriter().write(jsonResponse);
        
        log.warn("EnhancedSecurityFilter: Rate limit exceeded for IP {} ({}s until unblock)", clientIP, timeUntilUnblock);
    }

    /**
     * Handles blocked IPs.
     */
    private void handleBlockedIP(HttpServletResponse response, String clientIP, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        
        String jsonResponse = String.format(
            "{\"error\":\"Access denied\",\"message\":\"IP address %s is blocked\",\"reason\":\"%s\"}",
            clientIP, reason);
        response.getWriter().write(jsonResponse);
        
        log.warn("EnhancedSecurityFilter: Blocked IP {} attempted access. Reason: {}", clientIP, reason);
    }

    /**
     * Handles abuse detection blocks.
     */
    private void handleAbuseDetection(HttpServletResponse response, String clientIP, String reason) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        
        String jsonResponse = String.format(
            "{\"error\":\"Security violation\",\"message\":\"Request blocked due to security policy\",\"ip\":\"%s\"}",
            clientIP);
        response.getWriter().write(jsonResponse);
        
        log.warn("EnhancedSecurityFilter: Abuse detected from IP {}. Reason: {}", clientIP, reason);
    }

    /**
     * Handles internal errors.
     */
    private void handleInternalError(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Internal security error\",\"message\":\"Please try again later\"}");
    }

    /**
     * Adds security headers to the response.
     */
    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        
        // Only add HSTS for HTTPS
        if ("https".equals(response.getHeader("X-Forwarded-Proto"))) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
    }

    /**
     * Extracts request parameters for analysis.
     */
    private Map<String, String> extractParameters(HttpServletRequest request) {
        Map<String, String> parameters = new HashMap<>();
        
        // Get query parameters
        if (request.getQueryString() != null) {
            String[] pairs = request.getQueryString().split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0 && idx < pair.length() - 1) {
                    String key = pair.substring(0, idx);
                    String value = pair.substring(idx + 1);
                    parameters.put(key, value);
                }
            }
        }
        
        // Get form parameters for POST requests
        if ("POST".equalsIgnoreCase(request.getMethod()) && 
            request.getContentType() != null && 
            request.getContentType().contains("application/x-www-form-urlencoded")) {
            
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    parameters.put(key, values[0]);
                }
            });
        }
        
        return parameters;
    }

    /**
     * Extracts the real client IP, handling proxy headers.
     */
    private String getClientIP(HttpServletRequest request) {
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
            clientIP = request.getHeader("Proxy-Client-IP");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getRemoteAddr();
        }
        
        // Handle comma-separated IPs in X-Forwarded-For (get the first one - original client)
        if (clientIP != null && clientIP.contains(",")) {
            clientIP = clientIP.split(",")[0].trim();
        }
        
        return clientIP != null ? clientIP : "unknown";
    }

    /**
     * Checks if the endpoint is a static resource.
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/frontend/") ||
               path.startsWith("/static/") ||
               path.startsWith("/public/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.matches(".*\\.(html|css|js|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$");
    }

    /**
     * Checks if the endpoint is an authentication endpoint that should skip security checks.
     */
    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/super-admin/auth/") ||
               path.equals("/h2-console");
    }

    /**
     * Checks if the endpoint is security-sensitive.
     */
    private boolean isSecuritySensitiveEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/admin/") ||
               path.startsWith("/api/super-admin/") ||
               path.contains("/password") ||
               path.contains("/reset") ||
               path.contains("/change");
    }

    /**
     * Determines if this filter should be applied to the request.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Always apply security filter except for specific exclusions
        return path.equals("/health") || 
               path.equals("/actuator/health") ||
               path.equals("/favicon.ico");
    }
}
