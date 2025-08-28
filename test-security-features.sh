#!/bin/bash

# Security Features Testing Script
echo "=== Testing Enhanced Security Features ==="
echo

# Wait for application to start
echo "Waiting for application to start..."

# Test Rate Limiting
echo "1. Testing Rate Limiting (sending 10 rapid requests):"
for i in {1..10}; do
    response=$(curl -s -o /dev/null -w "%{http_code}" https://useshiftly.com/api/auth/login)
    echo "Request $i: HTTP $response"
    sleep 0.1
done
echo

# Test Abuse Detection with malicious patterns
echo "2. Testing Abuse Detection:"

# SQL Injection attempt
echo "  Testing SQL Injection detection:"
response=$(curl -s -o /dev/null -w "%{http_code}" "https://useshiftly.com/api/auth/login?username='OR%201=1--")
echo "  SQL Injection attempt: HTTP $response"

# XSS attempt
echo "  Testing XSS detection:"
response=$(curl -s -o /dev/null -w "%{http_code}" "https://useshiftly.com/api/auth/login?data=<script>alert('xss')</script>")
echo "  XSS attempt: HTTP $response"

# Path traversal attempt
echo "  Testing Path Traversal detection:"
response=$(curl -s -o /dev/null -w "%{http_code}" "https://useshiftly.com/api/../../../etc/passwd")
echo "  Path Traversal attempt: HTTP $response"
echo

# Test Security Headers
echo "3. Testing Security Headers:"
headers=$(curl -s -I https://useshiftly.com/api/auth/login | grep -E "(X-Content-Type-Options|X-Frame-Options|X-XSS-Protection|Cache-Control)")
echo "$headers"
echo

# Check logs for security events
echo "4. Checking security logs:"
if [ -f "logs/hotel-scheduler.log" ]; then
    echo "Recent security events:"
    tail -20 logs/hotel-scheduler.log | grep -i "security\|rate\|abuse\|blocked"
else
    echo "No log file found yet"
fi
echo

# Test Admin Security API
echo "5. Testing Security Management API:"
response=$(curl -s -o /dev/null -w "%{http_code}" https://useshiftly.com/api/admin/security/blocked-ips)
echo "Get blocked IPs: HTTP $response"

response=$(curl -s -o /dev/null -w "%{http_code}" https://useshiftly.com/api/admin/security/rate-limit-stats)
echo "Get rate limit stats: HTTP $response"
echo

echo "=== Security Testing Complete ==="
echo "Note: For full testing, authenticate as admin and test management endpoints"
echo "Stopping application..."

# Clean up
kill $APP_PID
echo "Application stopped."
