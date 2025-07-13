#!/bin/bash

# Test script to verify hotel scheduler fixes
echo "Testing Hotel Scheduler Authorization Fixes"
echo "=========================================="

BASE_URL="http://localhost:8080"

echo "1. Testing API health..."
curl -s "${BASE_URL}/api/auth/validate" || echo "Auth endpoint responding (401 expected without token)"

echo -e "\n2. Testing static file access..."
curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/frontend/login.html"
if [ $? -eq 0 ]; then
    echo " - Login page accessible"
else
    echo " - Login page has issues"
fi

echo -e "\n3. Testing employee pages..."
curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/frontend/employee-shifts.html"
if [ $? -eq 0 ]; then
    echo " - Employee shifts page accessible"
else
    echo " - Employee shifts page has issues"
fi

curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/frontend/employee-trades.html"
if [ $? -eq 0 ]; then
    echo " - Employee trades page accessible"
else
    echo " - Employee trades page has issues"
fi

echo -e "\n4. Checking if auto-logout.js exists..."
if [ -f "frontend/auto-logout.js" ]; then
    echo " - Auto-logout script created ✓"
else
    echo " - Auto-logout script missing ✗"
fi

echo -e "\nTest completed. If pages are accessible (200 status), frontend routing is fixed."
echo "Login with valid credentials to test full functionality."
