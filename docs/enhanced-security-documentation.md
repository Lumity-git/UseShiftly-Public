# Enhanced Security Layer Documentation

This document describes the comprehensive security enhancements implemented for the UseShiftly Shift Scheduler application.

## Overview

The security enhancements provide multiple layers of protection against common threats including brute force attacks, abuse, scanning, and various injection attempts. The system implements defensive measures with configurable thresholds and automatic response capabilities.

## Security Components

### 1. Rate Limiting Service (`RateLimitingService`)

**Purpose**: Prevents abuse by limiting the number of requests from each IP address.

**Features**:
- Sliding window rate limiting (60 requests/minute by default)
- Burst detection with configurable thresholds
- Exponential backoff for repeat offenders
- Automatic cleanup of expired entries
- Thread-safe implementation

**Configuration**:
```yaml
security:
  rate-limit:
    requests-per-minute: 60        # Max requests per minute per IP
    burst-threshold: 10            # Warning threshold
    block-duration-minutes: 15     # Block duration
    cleanup-interval-minutes: 5    # Cleanup interval
```

**How it works**:
1. Tracks requests per IP in sliding windows
2. Blocks IPs exceeding the limit for configured duration
3. Implements exponential backoff (15 min → 1 hour → 4 hours)
4. Returns 429 (Too Many Requests) with retry-after header

### 2. Abuse Detection Service (`AbuseDetectionService`)

**Purpose**: Automatically detects and blocks malicious behavior patterns.

**Detection Patterns**:
- SQL injection attempts
- Path traversal attempts
- Cross-site scripting (XSS)
- Admin interface scanning
- File inclusion attempts
- Null byte injection
- Suspicious user agents (bots, scanners)

**Configuration**:
```yaml
security:
  abuse:
    enabled: true                    # Enable/disable detection
    auth-failure-threshold: 10       # Failed auth before block
    detection-window-minutes: 60     # Detection time window
    temp-block-duration-minutes: 30  # Temporary block duration
    perm-block-threshold: 3          # Violations before permanent block
```

**Graduated Response**:
1. **First violation**: Temporary block (30 minutes)
2. **Second violation**: Extended block (1 hour)
3. **Third violation**: Permanent blacklist

### 3. Security Event Service (`SecurityEventService`)

**Purpose**: Centralized logging with structured format and anomaly detection.

**Event Types**:
- Authentication success/failure
- Authorization failures
- Rate limit violations
- Suspicious activities
- IP blocking events
- Admin actions

**Log Format** (JSON):
```json
{
  "timestamp": "2025-01-01T12:00:00Z",
  "event_type": "AUTHENTICATION_FAILURE",
  "severity": "WARN",
  "client_ip": "192.168.1.100",
  "user_agent": "Mozilla/5.0...",
  "endpoint": "/api/auth/login",
  "method": "POST",
  "reason": "Invalid credentials",
  "additional_data": {}
}
```

**Anomaly Detection**:
- Monitors event frequencies
- Triggers alerts when thresholds exceeded
- Configurable alert thresholds and time windows

### 4. Enhanced Security Filter (`EnhancedSecurityFilter`)

**Purpose**: First line of defense that runs before authentication.

**Protection Features**:
- IP-based rate limiting
- Malicious request detection
- Automatic threat blocking
- Security header injection
- Request sanitization

**Security Headers Added**:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Strict-Transport-Security` (HTTPS only)

## IP Management

### Whitelist (Never Blocked)
- Localhost (127.0.0.1, ::1)
- Private networks (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
- Manually whitelisted IPs

### Blacklist Management
- **Temporary blocks**: Rate limit violations, failed auth attempts
- **Permanent blocks**: Repeated abuse, malicious patterns
- **Manual management**: Admin can add/remove IPs

## API Endpoints for Security Management

### Dashboard and Monitoring
```
GET /api/super-admin/security/dashboard
GET /api/super-admin/security/health
GET /api/super-admin/security/config
```

### IP Management
```
GET  /api/super-admin/security/rate-limit/{ip}
POST /api/super-admin/security/block-ip
POST /api/super-admin/security/unblock-ip
POST /api/super-admin/security/whitelist-ip
POST /api/super-admin/security/blacklist-ip
POST /api/super-admin/security/remove-from-blacklist
```

### System Maintenance
```
POST /api/super-admin/security/cleanup
```

## Configuration Guide

### Environment Variables

**Rate Limiting**:
- `RATE_LIMIT_RPM=60` - Requests per minute per IP
- `RATE_LIMIT_BURST=10` - Burst detection threshold
- `RATE_LIMIT_BLOCK=15` - Block duration (minutes)

**Abuse Detection**:
- `ABUSE_DETECTION_ENABLED=true` - Enable detection
- `AUTH_FAILURE_THRESHOLD=10` - Failed auth threshold
- `ABUSE_WINDOW=60` - Detection window (minutes)
- `TEMP_BLOCK_DURATION=30` - Temporary block duration
- `PERM_BLOCK_THRESHOLD=3` - Permanent block threshold

**Monitoring**:
- `SECURITY_ALERT_THRESHOLD=100` - Alert threshold
- `SECURITY_ALERT_WINDOW=10` - Alert window (minutes)
- `ENHANCED_LOGGING=true` - Enable structured logging

### Application.yml Configuration

```yaml
security:
  rate-limit:
    requests-per-minute: ${RATE_LIMIT_RPM:60}
    burst-threshold: ${RATE_LIMIT_BURST:10}
    block-duration-minutes: ${RATE_LIMIT_BLOCK:15}
    cleanup-interval-minutes: 5
  
  abuse:
    enabled: ${ABUSE_DETECTION_ENABLED:true}
    auth-failure-threshold: ${AUTH_FAILURE_THRESHOLD:10}
    detection-window-minutes: ${ABUSE_WINDOW:60}
    temp-block-duration-minutes: ${TEMP_BLOCK_DURATION:30}
    perm-block-threshold: ${PERM_BLOCK_THRESHOLD:3}
  
  monitoring:
    alert-threshold: ${SECURITY_ALERT_THRESHOLD:100}
    time-window-minutes: ${SECURITY_ALERT_WINDOW:10}
    
  enhanced-logging:
    enabled: ${ENHANCED_LOGGING:true}
    log-level: ${SECURITY_LOG_LEVEL:WARN}
```

## Deployment Considerations

### Logging

**Production Setup**:
1. Configure log aggregation (ELK stack, Graylog, etc.)
2. Set up log rotation and retention policies
3. Configure alerting for security events
4. Monitor log volume and performance impact

**Log Analysis**:
- Use log parsing tools to extract security metrics
- Set up dashboards for real-time monitoring
- Create alerts for unusual patterns

### Performance Impact

**Memory Usage**:
- Rate limit data: ~100 bytes per tracked IP
- Abuse tracking: ~200 bytes per suspicious IP
- Automatic cleanup prevents memory leaks

**CPU Impact**:
- Minimal overhead per request (~1-2ms)
- Pattern matching optimized with compiled regex
- Background cleanup runs every 5 minutes

### Scaling Considerations

**Multi-Instance Deployment**:
- Rate limiting is per-instance (consider Redis for shared state)
- IP blocks are per-instance (use external firewall for global blocks)
- Logs should be centralized for correlation

**Load Balancer Integration**:
- Configure load balancer to pass real client IPs
- Use X-Forwarded-For or X-Real-IP headers
- Consider rate limiting at load balancer level

## WAF Integration (Optional)

### Cloudflare Setup
1. Enable Cloudflare proxy for your domain
2. Configure security rules:
   ```
   (http.request.uri.path contains "/api/auth/login" and 
    cf.threat_score > 10) then block
   ```
3. Set up rate limiting rules
4. Enable bot fight mode

### AWS WAF Setup
1. Create Web ACL with rules:
   - SQL injection protection
   - XSS protection
   - Rate limiting by IP
   - Geo-blocking if needed
2. Associate with Application Load Balancer
3. Configure logging to CloudWatch

### ModSecurity (Self-hosted)
1. Install ModSecurity with OWASP Core Rule Set
2. Configure custom rules for your application
3. Set up log aggregation
4. Fine-tune rules to reduce false positives

## Monitoring and Alerting

### Key Metrics to Monitor
- Authentication failure rate
- Rate limit violations per hour
- Blocked IPs count
- Security event frequency
- Response time impact

### Recommended Alerts
- More than 100 failed auth attempts in 10 minutes
- More than 50 IPs blocked in 1 hour
- Critical security events (malicious patterns)
- Security filter errors or exceptions

### Dashboard Metrics
- Real-time threat activity
- Top blocked IPs
- Security event breakdown
- System health metrics

## Incident Response

### Automated Responses
1. **Rate limit exceeded**: Temporary IP block
2. **Malicious patterns**: Immediate block + alert
3. **Brute force detected**: Progressive blocking
4. **System overload**: Emergency rate limiting

### Manual Response Procedures
1. **Investigate security alerts**
2. **Review blocked IPs for false positives**
3. **Adjust thresholds if needed**
4. **Update security rules for new threats**

### Recovery Procedures
1. **Unblock legitimate IPs**
2. **Reset rate limit counters if needed**
3. **Review and update security configuration**
4. **Document incidents and improvements**

## Testing and Validation

### Security Testing
1. **Rate limiting**: Use tools like `ab` or `wrk` to test limits
2. **Abuse detection**: Test with malicious payloads
3. **IP blocking**: Verify block/unblock functionality
4. **Performance**: Load test with security filters enabled

### Monitoring Validation
1. **Log format**: Verify structured logs are generated
2. **Alerting**: Test alert thresholds and notifications
3. **Dashboard**: Confirm metrics are updating correctly
4. **API endpoints**: Test security management APIs

This enhanced security layer provides comprehensive protection while maintaining system performance and usability. Regular monitoring and tuning ensure optimal security posture.
