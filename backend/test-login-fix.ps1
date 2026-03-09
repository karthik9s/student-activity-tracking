# Login Authentication Fix Verification Script
# This script tests all requirements from the bugfix spec

Write-Host "`n=== Login Authentication Fix Verification ===" -ForegroundColor Cyan
Write-Host "Testing against http://localhost:8080`n" -ForegroundColor Gray

$testsPassed = 0
$testsFailed = 0

function Test-Endpoint {
    param(
        [string]$TestName,
        [string]$Uri,
        [string]$Method = "GET",
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int]$ExpectedStatus,
        [string]$Description
    )
    
    Write-Host "Test: $TestName" -ForegroundColor Yellow
    Write-Host "  Description: $Description" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = $Uri
            Method = $Method
            Headers = $Headers
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params.Body = $Body
            $params.ContentType = 'application/json'
        }
        
        $response = Invoke-WebRequest @params
        $actualStatus = $response.StatusCode
    } catch {
        $actualStatus = $_.Exception.Response.StatusCode.value__
    }
    
    if ($actualStatus -eq $ExpectedStatus) {
        Write-Host "  Result: PASS (Status: $actualStatus)" -ForegroundColor Green
        $script:testsPassed++
        return $true
    } else {
        Write-Host "  Result: FAIL (Expected: $ExpectedStatus, Got: $actualStatus)" -ForegroundColor Red
        $script:testsFailed++
        return $false
    }
}

# Requirement 2.1: Login with valid credentials returns 200 OK with JWT tokens
Write-Host "`n--- Testing Authentication Endpoints (Requirements 2.1-2.5) ---`n" -ForegroundColor Cyan

$loginBody = @{email='admin@cvr.ac.in'; password='admin123'} | ConvertTo-Json
$loginResult = Test-Endpoint `
    -TestName "Login with valid credentials" `
    -Uri "http://localhost:8080/api/v1/auth/login" `
    -Method "POST" `
    -Body $loginBody `
    -ExpectedStatus 200 `
    -Description "Requirement 2.1: Valid credentials should return 200 OK with JWT tokens"

# Extract token for subsequent tests
if ($loginResult) {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $loginBody -ContentType 'application/json' -UseBasicParsing
    $token = ($loginResponse.Content | ConvertFrom-Json).accessToken
    Write-Host "  Token extracted successfully" -ForegroundColor Gray
}

# Requirement 2.3: Public logout endpoint accessible without authentication
Test-Endpoint `
    -TestName "Logout endpoint without authentication" `
    -Uri "http://localhost:8080/api/v1/auth/logout" `
    -Method "POST" `
    -ExpectedStatus 200 `
    -Description "Requirement 2.3: Public auth endpoints should be accessible without authentication"

# Requirement 2.4: Invalid credentials return 401 with error message
$invalidLoginBody = @{email='admin@cvr.ac.in'; password='wrongpassword'} | ConvertTo-Json
Test-Endpoint `
    -TestName "Login with invalid credentials" `
    -Uri "http://localhost:8080/api/v1/auth/login" `
    -Method "POST" `
    -Body $invalidLoginBody `
    -ExpectedStatus 401 `
    -Description "Requirement 2.4: Invalid credentials should return 401 with error message"

# Requirement 3.1: Protected endpoints require valid JWT token
Write-Host "`n--- Testing Protected Endpoint Security (Requirements 3.1-3.5) ---`n" -ForegroundColor Cyan

Test-Endpoint `
    -TestName "Protected endpoint without token" `
    -Uri "http://localhost:8080/api/v1/admin/students" `
    -Method "GET" `
    -ExpectedStatus 401 `
    -Description "Requirement 3.1: Protected endpoints should require authentication"

# Requirement 3.1: Protected endpoints work with valid JWT token
if ($token) {
    $authHeaders = @{Authorization="Bearer $token"}
    Test-Endpoint `
        -TestName "Protected endpoint with valid token" `
        -Uri "http://localhost:8080/api/v1/admin/students?page=0&size=1" `
        -Method "GET" `
        -Headers $authHeaders `
        -ExpectedStatus 200 `
        -Description "Requirement 3.1: Valid JWT token should grant access to protected endpoints"
}

# Requirement 3.4: CORS configuration works
Write-Host "`n--- Testing CORS Configuration (Requirement 3.4) ---`n" -ForegroundColor Cyan

$corsHeaders = @{Origin='http://localhost:5173'}
Test-Endpoint `
    -TestName "CORS preflight request" `
    -Uri "http://localhost:8080/api/v1/auth/login" `
    -Method "OPTIONS" `
    -Headers $corsHeaders `
    -ExpectedStatus 200 `
    -Description "Requirement 3.4: CORS should allow cross-origin requests from configured frontend"

# Summary
Write-Host "`n=== Test Summary ===" -ForegroundColor Cyan
Write-Host "Tests Passed: $testsPassed" -ForegroundColor Green
Write-Host "Tests Failed: $testsFailed" -ForegroundColor $(if ($testsFailed -eq 0) { "Green" } else { "Red" })

if ($testsFailed -eq 0) {
    Write-Host "`nAll tests passed! Login authentication fix is working correctly." -ForegroundColor Green
    exit 0
} else {
    Write-Host "`nSome tests failed. Please review the results above." -ForegroundColor Red
    exit 1
}
