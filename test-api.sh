#!/bin/bash

# Hotel Scheduler API Test Script
# This script tests the basic functionality of the API

BASE_URL="http://localhost:8080/api"

echo "🏨 Hotel Scheduler API Test Script"
echo "=================================="

# Test login
echo "1. Testing login..."
LOGIN_RESPONSE=$(curl -s -X POST ${BASE_URL}/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"manager@hotel.com","password":"manager123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "❌ Login failed!"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
else
    echo "✅ Login successful!"
fi

# Test getting current user
echo "2. Testing current user endpoint..."
USER_RESPONSE=$(curl -s -X GET ${BASE_URL}/employees/me \
  -H "Authorization: Bearer $TOKEN")

echo "✅ Current user: $USER_RESPONSE"

# Test getting shifts
echo "3. Testing shifts endpoint..."
SHIFTS_RESPONSE=$(curl -s -X GET ${BASE_URL}/shifts \
  -H "Authorization: Bearer $TOKEN")

echo "✅ Shifts: $SHIFTS_RESPONSE"

# Test creating a shift
echo "4. Testing shift creation..."
CREATE_SHIFT_RESPONSE=$(curl -s -X POST ${BASE_URL}/shifts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "startTime": "2024-12-15T09:00:00",
    "endTime": "2024-12-15T17:00:00",
    "employeeId": 3,
    "departmentId": 1,
    "notes": "Test shift created by API script"
  }')

echo "✅ Shift creation: $CREATE_SHIFT_RESPONSE"

# Test getting available shifts
echo "5. Testing available shifts endpoint..."
AVAILABLE_SHIFTS=$(curl -s -X GET ${BASE_URL}/shifts/available \
  -H "Authorization: Bearer $TOKEN")

echo "✅ Available shifts: $AVAILABLE_SHIFTS"

echo ""
echo "🎉 API tests completed!"
echo "📝 You can now integrate with a frontend application"
