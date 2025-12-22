#!/bin/bash

# Nominatim Service Test Script
# =============================
# Tests forward geocoding, reverse geocoding, and service health

set -e

NOMINATIM_URL="${NOMINATIM_URL:-http://localhost:8088}"
PASS_COUNT=0
FAIL_COUNT=0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "Nominatim Service Test Suite"
echo "Testing: $NOMINATIM_URL"
echo "========================================"
echo ""

# Function to print test results
print_result() {
	local test_name="$1"
	local success="$2"
	local details="$3"

	if [ "$success" = "true" ]; then
		echo -e "${GREEN}[PASS]${NC} $test_name"
		((PASS_COUNT++))
	else
		echo -e "${RED}[FAIL]${NC} $test_name"
		echo -e "       ${details}"
		((FAIL_COUNT++))
	fi
}

# Test 1: Health Check / Status
echo "Test 1: Service Status"
echo "----------------------"
STATUS_RESPONSE=$(curl -s -w "\n%{http_code}" "$NOMINATIM_URL/status" 2>/dev/null || echo "000")
HTTP_CODE=$(echo "$STATUS_RESPONSE" | tail -n1)
BODY=$(echo "$STATUS_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
	print_result "Service is running" "true"
	echo "       Status: $BODY"
else
	print_result "Service is running" "false" "HTTP $HTTP_CODE - Service may not be ready"
	echo ""
	echo -e "${YELLOW}Hint: If import is in progress, wait for it to complete.${NC}"
	echo "      Check logs with: docker compose logs -f nominatim"
	exit 1
fi
echo ""

# Test 2: Forward Geocoding - St. Louis City Hall
echo "Test 2: Forward Geocoding"
echo "-------------------------"
echo "Query: 'City Hall, St Louis, MO'"

FORWARD_RESPONSE=$(curl -s "$NOMINATIM_URL/search?q=City+Hall,+St+Louis,+MO&format=json&limit=1" 2>/dev/null)

if echo "$FORWARD_RESPONSE" | grep -q '"lat"'; then
	LAT=$(echo "$FORWARD_RESPONSE" | grep -o '"lat":"[^"]*"' | head -1 | cut -d'"' -f4)
	LON=$(echo "$FORWARD_RESPONSE" | grep -o '"lon":"[^"]*"' | head -1 | cut -d'"' -f4)
	DISPLAY=$(echo "$FORWARD_RESPONSE" | grep -o '"display_name":"[^"]*"' | head -1 | cut -d'"' -f4)
	print_result "Forward geocoding works" "true"
	echo "       Coordinates: ($LAT, $LON)"
	echo "       Display: ${DISPLAY:0:80}..."
else
	print_result "Forward geocoding works" "false" "No results returned"
fi
echo ""

# Test 3: Forward Geocoding - Specific Address
echo "Test 3: Forward Geocoding (Specific Address)"
echo "---------------------------------------------"
echo "Query: '1 Gateway Drive, St Louis, MO' (Busch Stadium area)"

ADDRESS_RESPONSE=$(curl -s "$NOMINATIM_URL/search?q=1+Gateway+Drive,+St+Louis,+MO&format=json&limit=1" 2>/dev/null)

if echo "$ADDRESS_RESPONSE" | grep -q '"lat"'; then
	LAT=$(echo "$ADDRESS_RESPONSE" | grep -o '"lat":"[^"]*"' | head -1 | cut -d'"' -f4)
	LON=$(echo "$ADDRESS_RESPONSE" | grep -o '"lon":"[^"]*"' | head -1 | cut -d'"' -f4)
	print_result "Address geocoding works" "true"
	echo "       Coordinates: ($LAT, $LON)"
else
	print_result "Address geocoding works" "false" "No results returned"
fi
echo ""

# Test 4: Reverse Geocoding - Gateway Arch coordinates
echo "Test 4: Reverse Geocoding"
echo "-------------------------"
echo "Coordinates: (38.6247, -90.1848) - Gateway Arch area"

REVERSE_RESPONSE=$(curl -s "$NOMINATIM_URL/reverse?lat=38.6247&lon=-90.1848&format=json" 2>/dev/null)

if echo "$REVERSE_RESPONSE" | grep -q '"display_name"'; then
	DISPLAY=$(echo "$REVERSE_RESPONSE" | grep -o '"display_name":"[^"]*"' | cut -d'"' -f4)
	print_result "Reverse geocoding works" "true"
	echo "       Address: ${DISPLAY:0:80}..."
else
	print_result "Reverse geocoding works" "false" "No results returned"
fi
echo ""

# Test 5: Response Format Compatibility
echo "Test 5: Response Format (OSM API Compatibility)"
echo "------------------------------------------------"

COMPAT_RESPONSE=$(curl -s "$NOMINATIM_URL/search?q=St+Louis&format=json&limit=1&addressdetails=1" 2>/dev/null)

# Check for expected fields
HAS_PLACE_ID=$(echo "$COMPAT_RESPONSE" | grep -c '"place_id"' || true)
HAS_LAT=$(echo "$COMPAT_RESPONSE" | grep -c '"lat"' || true)
HAS_LON=$(echo "$COMPAT_RESPONSE" | grep -c '"lon"' || true)
HAS_ADDRESS=$(echo "$COMPAT_RESPONSE" | grep -c '"address"' || true)
HAS_BOUNDINGBOX=$(echo "$COMPAT_RESPONSE" | grep -c '"boundingbox"' || true)

if [ "$HAS_PLACE_ID" -ge 1 ] && [ "$HAS_LAT" -ge 1 ] && [ "$HAS_LON" -ge 1 ]; then
	print_result "Has required fields (place_id, lat, lon)" "true"
else
	print_result "Has required fields (place_id, lat, lon)" "false" "Missing core fields"
fi

if [ "$HAS_ADDRESS" -ge 1 ]; then
	print_result "Has address details" "true"
else
	print_result "Has address details" "false" "addressdetails not working"
fi
echo ""

# Test 6: Response Time
echo "Test 6: Response Time"
echo "---------------------"

START=$(date +%s.%N)
curl -s "$NOMINATIM_URL/search?q=Clayton,+MO&format=json&limit=1" > /dev/null
END=$(date +%s.%N)
DURATION=$(echo "$END - $START" | bc)

if (( $(echo "$DURATION < 2.0" | bc -l) )); then
	print_result "Response time < 2 seconds" "true"
	echo "       Time: ${DURATION}s"
else
	print_result "Response time < 2 seconds" "false" "Time: ${DURATION}s"
fi
echo ""

# Test 7: Bounded Search (within St. Louis area)
echo "Test 7: Bounded Search"
echo "----------------------"
echo "Searching within St. Louis bounding box"

BOUNDED_RESPONSE=$(curl -s "$NOMINATIM_URL/search?q=Main+Street&format=json&limit=5&viewbox=-90.4,-38.5,-90.1,-38.8&bounded=1" 2>/dev/null)

RESULT_COUNT=$(echo "$BOUNDED_RESPONSE" | grep -o '"place_id"' | wc -l | tr -d ' ')

if [ "$RESULT_COUNT" -ge 1 ]; then
	print_result "Bounded search returns results" "true"
	echo "       Found: $RESULT_COUNT results"
else
	print_result "Bounded search returns results" "false" "No results in bounds"
fi
echo ""

# Summary
echo "========================================"
echo "Test Summary"
echo "========================================"
echo -e "${GREEN}Passed:${NC} $PASS_COUNT"
echo -e "${RED}Failed:${NC} $FAIL_COUNT"
echo ""

if [ "$FAIL_COUNT" -eq 0 ]; then
	echo -e "${GREEN}All tests passed! Nominatim is ready for use.${NC}"
	exit 0
else
	echo -e "${YELLOW}Some tests failed. Check the output above for details.${NC}"
	exit 1
fi