#!/bin/bash
set -e

BASE_URL="http://localhost:8080"
PASSED=0
FAILED=0

check() {
  local description=$1
  local expected_code=$2
  local method=$3
  local url=$4
  local data=$5
  local auth=$6

  local headers="-H 'Content-Type: application/json'"
  if [ -n "$auth" ]; then
    headers="$headers -H 'Authorization: Bearer $auth'"
  fi

  local actual_code
  if [ "$method" = "GET" ]; then
    actual_code=$(eval curl -s -o /tmp/smoke_response.json -w '%{http_code}' $headers "$url")
  else
    actual_code=$(eval curl -s -o /tmp/smoke_response.json -w '%{http_code}' -X $method $headers -d "'$data'" "$url")
  fi

  if [ "$actual_code" = "$expected_code" ]; then
    echo "  PASS: $description (HTTP $actual_code)"
    PASSED=$((PASSED + 1))
  else
    echo "  FAIL: $description (expected $expected_code, got $actual_code)"
    FAILED=$((FAILED + 1))
  fi
}

echo "========================================"
echo "  Digital Banking Platform Smoke Test"
echo "========================================"
echo ""

# 1. Health checks
echo "[1/10] Service Health Checks"
for port in 8080 8081 8082 8083 8084 8085 8086 8087 8088 8089 8090 8091 8092 8093 8094 8095 8096 8097; do
  code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:$port/actuator/health" 2>/dev/null || echo "000")
  if [ "$code" = "200" ]; then
    echo "  PASS: Port $port healthy"
    PASSED=$((PASSED + 1))
  else
    echo "  FAIL: Port $port returned $code"
    FAILED=$((FAILED + 1))
  fi
done

# 2. Register a user
echo ""
echo "[2/10] Register User"
check "Register new user" "201" "POST" "$BASE_URL/api/v1/auth/register" \
  '{"email":"smoketest@example.com","password":"Test1234!","fullName":"Smoke Test","phone":"+1234567890","dob":"1990-01-01","country":"US"}'

# 3. Login
echo ""
echo "[3/10] Login"
check "Login" "200" "POST" "$BASE_URL/api/v1/auth/login" \
  '{"email":"smoketest@example.com","password":"Test1234!"}'
TOKEN=$(cat /tmp/smoke_response.json | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

# 4. Submit KYC
echo ""
echo "[4/10] Submit KYC"
if [ -n "$TOKEN" ]; then
  check "Submit KYC" "200" "POST" "$BASE_URL/api/v1/kyc/submit" \
    '{"level":"BASIC","documents":[{"type":"PASSPORT","fileName":"passport.pdf","contentType":"application/pdf","fileSize":1024,"fileHash":"abc123","storageId":"s3-key"}]}' "$TOKEN"
else
  echo "  SKIP: No auth token available"
fi

# 5. Open account
echo ""
echo "[5/10] Open Account"
if [ -n "$TOKEN" ]; then
  check "Open account" "201" "POST" "$BASE_URL/api/v1/accounts" \
    '{"type":"SAVINGS","currency":"USD","name":"My Savings"}' "$TOKEN"
else
  echo "  SKIP: No auth token available"
fi

# 6. Check balance
echo ""
echo "[6/10] Check Balance"
if [ -n "$TOKEN" ]; then
  check "Check balance" "200" "GET" "$BASE_URL/api/v1/ledger/balances/00000000-0000-0000-0000-000000000001" "" "$TOKEN"
else
  echo "  SKIP: No auth token available"
fi

# 7. List cards
echo ""
echo "[7/10] List Cards"
if [ -n "$TOKEN" ]; then
  check "List cards" "200" "GET" "$BASE_URL/api/v1/cards" "" "$TOKEN"
else
  echo "  SKIP: No auth token available"
fi

# 8. Submit loan application
echo ""
echo "[8/10] Loan Application"
if [ -n "$TOKEN" ]; then
  check "Submit loan" "200" "POST" "$BASE_URL/api/v1/loan-applications" \
    '{"amount":25000,"type":"PERSONAL","termMonths":36,"annualIncome":80000}' "$TOKEN"
else
  echo "  SKIP: No auth token available"
fi

# 9. File upload (simple metadata check)
echo ""
echo "[9/10] File Service"
check "File service health" "200" "GET" "http://localhost:8097/actuator/health"

# 10. Observability
echo ""
echo "[10/10] Observability Stack"
prom_code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:9090/api/v1/targets" 2>/dev/null || echo "000")
if [ "$prom_code" = "200" ]; then
  echo "  PASS: Prometheus accessible"
  PASSED=$((PASSED + 1))
else
  echo "  FAIL: Prometheus returned $prom_code"
  FAILED=$((FAILED + 1))
fi

grafana_code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:3000/api/health" 2>/dev/null || echo "000")
if [ "$grafana_code" = "200" ]; then
  echo "  PASS: Grafana accessible"
  PASSED=$((PASSED + 1))
else
  echo "  FAIL: Grafana returned $grafana_code"
  FAILED=$((FAILED + 1))
fi

zipkin_code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:9411/health" 2>/dev/null || echo "000")
if [ "$zipkin_code" = "200" ]; then
  echo "  PASS: Zipkin accessible"
  PASSED=$((PASSED + 1))
else
  echo "  FAIL: Zipkin returned $zipkin_code"
  FAILED=$((FAILED + 1))
fi

# Summary
echo ""
echo "========================================"
echo "  Results: $PASSED passed, $FAILED failed"
echo "========================================"

if [ $FAILED -gt 0 ]; then
  exit 1
fi
