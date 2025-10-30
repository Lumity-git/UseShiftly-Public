package com.useshiftly.scheduler.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiting service that implements IP-based request throttling with configurable policies.
 * 
 * Features:
 * - Sliding window rate limiting per IP address
 * - Exponential backoff for repeat offenders
 * - Configurable thresholds and time windows
 * - Automatic cleanup of expired entries
 * - Thread-safe operations
 * 
 * Configuration properties:
 * - security.rate-limit.requests-per-minute: Maximum requests per minute per IP (default: 60)
 * - security.rate-limit.burst-threshold: Maximum burst requests (default: 10)
 * - security.rate-limit.block-duration-minutes: Block duration in minutes (default: 15)
 * - security.rate-limit.cleanup-interval-minutes: Cleanup interval in minutes (default: 5)
 */
@Service
@Slf4j
public class RateLimitingService {

    @Value("${security.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${security.rate-limit.burst-threshold:10}")
    private int burstThreshold;

    @Value("${security.rate-limit.block-duration-minutes:15}")
    private int blockDurationMinutes;

    @Value("${security.rate-limit.cleanup-interval-minutes:5}")
    private int cleanupIntervalMinutes;

    // Track request counts per IP with sliding window
    private final ConcurrentHashMap<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    
    // Track blocked IPs with expiration times
    private final ConcurrentHashMap<String, BlockInfo> blockedIPs = new ConcurrentHashMap<>();
    
    private AtomicLong lastCleanup = new AtomicLong(System.currentTimeMillis());

    /**
     * Checks if a request from the given IP should be allowed.
     * 
     * @param clientIP the client IP address
     * @return true if request is allowed, false if rate limited
     */
    public boolean isRequestAllowed(String clientIP) {
        // Clean up expired entries periodically
        performPeriodicCleanup();
        
        // Check if IP is currently blocked
        if (isIPBlocked(clientIP)) {
            log.warn("Rate limit: Blocked IP {} attempted request", clientIP);
            return false;
        }
        
        // Get or create rate limit info for this IP
        RateLimitInfo rateLimitInfo = rateLimitMap.computeIfAbsent(clientIP, 
            k -> new RateLimitInfo());
        
        Instant now = Instant.now();
        
        synchronized (rateLimitInfo) {
            // Reset window if enough time has passed
            if (now.isAfter(rateLimitInfo.windowStart.plus(1, ChronoUnit.MINUTES))) {
                rateLimitInfo.requestCount.set(0);
                rateLimitInfo.windowStart = now;
            }
            
            int currentCount = rateLimitInfo.requestCount.incrementAndGet();
            
            // Check for rate limit violation
            if (currentCount > requestsPerMinute) {
                // Block the IP
                blockIP(clientIP, "Rate limit exceeded");
                log.warn("Rate limit: IP {} exceeded limit ({} requests/minute), blocking for {} minutes", 
                    clientIP, requestsPerMinute, blockDurationMinutes);
                return false;
            }
            
            // Check for burst threshold
            if (currentCount > burstThreshold) {
                log.warn("Rate limit: IP {} approaching limit ({} requests in current window)", 
                    clientIP, currentCount);
            }
            
            return true;
        }
    }
    
    /**
     * Checks if an IP is currently blocked.
     * 
     * @param clientIP the client IP address
     * @return true if IP is blocked, false otherwise
     */
    public boolean isIPBlocked(String clientIP) {
        BlockInfo blockInfo = blockedIPs.get(clientIP);
        if (blockInfo != null) {
            if (Instant.now().isBefore(blockInfo.unblockTime)) {
                return true;
            } else {
                // Block has expired, remove it
                blockedIPs.remove(clientIP);
                log.info("Rate limit: Unblocked IP {} (block expired)", clientIP);
            }
        }
        return false;
    }
    
    /**
     * Manually block an IP address.
     * 
     * @param clientIP the client IP address
     * @param reason the reason for blocking
     */
    public void blockIP(String clientIP, String reason) {
        BlockInfo existingBlock = blockedIPs.get(clientIP);
        int blockDuration = blockDurationMinutes;
        
        // Implement exponential backoff for repeat offenders
        if (existingBlock != null) {
            blockDuration = Math.min(blockDurationMinutes * 4, 240); // Max 4 hours
            log.warn("Rate limit: Repeat offender {} detected, extending block to {} minutes", 
                clientIP, blockDuration);
        }
        
        Instant unblockTime = Instant.now().plus(blockDuration, ChronoUnit.MINUTES);
        blockedIPs.put(clientIP, new BlockInfo(unblockTime, reason));
        
        log.warn("Rate limit: Blocked IP {} for {} minutes. Reason: {}", 
            clientIP, blockDuration, reason);
    }
    
    /**
     * Manually unblock an IP address.
     * 
     * @param clientIP the client IP address
     */
    public void unblockIP(String clientIP) {
        if (blockedIPs.remove(clientIP) != null) {
            log.info("Rate limit: Manually unblocked IP {}", clientIP);
        }
    }
    
    /**
     * Get current request count for an IP.
     * 
     * @param clientIP the client IP address
     * @return current request count in the current window
     */
    public int getCurrentRequestCount(String clientIP) {
        RateLimitInfo info = rateLimitMap.get(clientIP);
        return info != null ? info.requestCount.get() : 0;
    }
    
    /**
     * Get time remaining until IP is unblocked.
     * 
     * @param clientIP the client IP address
     * @return seconds until unblock, or 0 if not blocked
     */
    public long getTimeUntilUnblock(String clientIP) {
        BlockInfo blockInfo = blockedIPs.get(clientIP);
        if (blockInfo != null) {
            long seconds = Instant.now().until(blockInfo.unblockTime, ChronoUnit.SECONDS);
            return Math.max(0, seconds);
        }
        return 0;
    }
    
    /**
     * Performs periodic cleanup of expired entries.
     */
    private void performPeriodicCleanup() {
        long now = System.currentTimeMillis();
        long timeSinceLastCleanup = now - lastCleanup.get();
        
        if (timeSinceLastCleanup > cleanupIntervalMinutes * 60 * 1000) {
            if (lastCleanup.compareAndSet(now - timeSinceLastCleanup, now)) {
                cleanupExpiredEntries();
            }
        }
    }
    
    /**
     * Cleans up expired rate limit and block entries.
     */
    private void cleanupExpiredEntries() {
        Instant now = Instant.now();
        AtomicInteger rateLimitCleaned = new AtomicInteger(0);
        AtomicInteger blocksCleaned = new AtomicInteger(0);
        
        // Clean up old rate limit entries (older than 2 minutes)
        rateLimitMap.entrySet().removeIf(entry -> {
            if (now.isAfter(entry.getValue().windowStart.plus(2, ChronoUnit.MINUTES))) {
                rateLimitCleaned.incrementAndGet();
                return true;
            }
            return false;
        });
        
        // Clean up expired blocks
        blockedIPs.entrySet().removeIf(entry -> {
            if (now.isAfter(entry.getValue().unblockTime)) {
                blocksCleaned.incrementAndGet();
                return true;
            }
            return false;
        });
        
        if (rateLimitCleaned.get() > 0 || blocksCleaned.get() > 0) {
            log.debug("Rate limit cleanup: Removed {} rate limit entries and {} expired blocks", 
                rateLimitCleaned.get(), blocksCleaned.get());
        }
    }
    
    /**
     * Get statistics about current rate limiting state.
     * 
     * @return formatted statistics string
     */
    public String getStatistics() {
        return String.format("Active rate limits: %d, Blocked IPs: %d", 
            rateLimitMap.size(), blockedIPs.size());
    }
    
    /**
     * Internal class to track rate limiting information per IP.
     */
    private static class RateLimitInfo {
        AtomicInteger requestCount = new AtomicInteger(0);
        volatile Instant windowStart = Instant.now();
    }
    
    /**
     * Internal class to track blocked IP information.
     */
    private static class BlockInfo {
        final Instant unblockTime;
        final String reason;
        
        BlockInfo(Instant unblockTime, String reason) {
            this.unblockTime = unblockTime;
            this.reason = reason;
        }
    }
}
