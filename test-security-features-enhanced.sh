#!/bin/bash

# Enhanced Security Testing Script with IP Debugging
echo "=== Enhanced Security Features Testing ==="
echo

# Test IP Detection First
echo "1. Testing IP Detection:"
echo "Fetching IP information from debug endpoint..."
curl -s "https://useshiftly.com/api/debug/ip-info" | jq '.' 2>/dev/null || curl -s "https://useshiftly.com/api/debug/ip-info"
echo
echo

# Test Rate Limiting
echo "2. Testing Rate Limiting (sending 10 rapid requests):"
for i in {1..10}; do
    response=$(curl -s -o /dev/null -w "%{http_code}" https://useshiftly.com/api/auth/login)
    echo "Request $i: HTTP $response"
    sleep 0.1
done
echo

# Test Abuse Detection with malicious patterns
echo "3. Testing Abuse Detection:"

# SQL Injection attempt
echo "  Testing SQL Injection detection:"
response=$(curl -s -o /dev/null -w "%{http_code}" "https://useshiftly.com/api/auth/login?username='OR%201=1--")
echo "  SQL Injection attempt: HTTP $response"

# XSS attempt (URL encoded)
echo "  Testing XSS detection:"
response=$(curl -s -o /dev/null -w "%{http_code}" "https://useshiftly.com/api/auth/login?data=%3Cscript%3Ealert%28%27xss%27%29%3C%2Fscript%3E")
echo "  XSS attempt: HTTP $response"

# Path traversal attempt
echo "  Testing Path Traversal detection:"
response=$(curl -s -o /dev/null -w "%{http_code}" "https://useshiftly.com/api/../../../etc/passwd")
echo "  Path Traversal attempt: HTTP $response"
echo

# Test Security Headers
echo "4. Testing Security Headers:"
headers=$(curl -s -I https://useshiftly.com/api/auth/login | grep -E "(X-Content-Type-Options|X-Frame-Options|X-XSS-Protection|Cache-Control)")
echo "$headers"
echo

# Test Admin Security API (should require authentication)
echo "5. Testing Security Management API (should return 401/403):"
response=$(curl -s -o /dev/null -w "%{http_code}" https://useshiftly.com/api/admin/security/blocked-ips)
echo "Get blocked IPs (no auth): HTTP $response"

response=$(curl -s -o /dev/null -w "%{http_code}" https://useshiftly.com/api/admin/security/rate-limit-stats)
echo "Get rate limit stats (no auth): HTTP $response"
echo

# Test with legitimate browser-like request
echo "6. Testing with browser-like User-Agent:"
response=$(curl -s -o /dev/null -w "%{http_code}" -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" https://useshiftly.com/api/auth/login)
echo "Browser-like request: HTTP $response"
echo

echo "=== Security Testing Complete ==="
echo "Check the application logs for security events and IP detection details."
echo "Note: Admin endpoints should return 401/403 without authentication (this is correct behavior)"
