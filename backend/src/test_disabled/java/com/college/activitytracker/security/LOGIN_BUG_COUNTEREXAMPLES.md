# Login Authentication Bug - Counterexamples Found

## Test Execution Date
March 8, 2026

## Bug Confirmation
The bug condition exploration test was executed on UNFIXED code and successfully confirmed the bug exists.

## Counterexamples Discovered

### Counterexample 1: Login with Valid Credentials Returns 401
**Input:**
- Endpoint: POST /api/v1/auth/login
- Request Body: `{"email":"admin@cvr.ac.in","password":"admin123"}`
- Headers: Content-Type: application/json
- No JWT token (public endpoint)

**Expected Behavior:**
- Status Code: 200 OK
- Response Body: JSON with JWT access and refresh tokens

**Actual Behavior (UNFIXED CODE):**
- Status Code: 401 Unauthorized
- Response Body: Empty (ContentLength: 0)
- Response Headers: No error message or details

**Analysis:**
This counterexample confirms that authentication requests with valid credentials are being blocked with 401 Unauthorized before reaching the AuthController. The empty response body indicates the request is rejected by a filter in the Spring Security filter chain, not by the controller's authentication logic.

### Counterexample 2: Logout Endpoint Returns 401
**Input:**
- Endpoint: POST /api/v1/auth/logout
- No request body
- Headers: Content-Type: application/json
- No JWT token (configured as permitAll)

**Expected Behavior:**
- Status Code: 200 OK
- Response Body: "Logged out successfully"

**Actual Behavior (UNFIXED CODE):**
- Status Code: 401 Unauthorized
- Response Body: Empty (ContentLength: 0)

**Analysis:**
This counterexample confirms that even endpoints explicitly configured with `permitAll()` in SecurityConfig are being blocked. This strongly suggests the issue is with filter ordering or a servlet filter executing before Spring Security's filter chain.

### Counterexample 3: Database Contains Valid Users
**Verification:**
- Database contains 29 valid users with correct BCrypt password hashes
- User "admin@cvr.ac.in" exists with password hash for "admin123"
- Password verification logic using BCrypt is correct

**Analysis:**
This confirms the bug is NOT related to:
- Missing users in database
- Incorrect password hashes
- Password verification logic

The bug is isolated to the Spring Security filter chain configuration.

## Root Cause Hypothesis Validation

Based on the counterexamples, the most likely root cause is:

**Hypothesis: RateLimitingFilter Interference**
- The RateLimitingFilter is annotated with @Component, causing it to register as a servlet filter
- Servlet filters execute BEFORE Spring Security's filter chain
- If the RateLimitingFilter has an error or blocks requests unexpectedly, it would prevent authentication requests from reaching Spring Security
- This explains why ALL auth endpoints return 401 with empty body

**Supporting Evidence:**
1. All auth endpoints return 401 (not just login)
2. Empty response body (no error message from Spring Security)
3. Endpoints configured with permitAll() are still blocked
4. No error logs in backend console (request never reaches controller)

## Test Implementation

The bug condition exploration test has been implemented in:
`backend/src/test/java/com/college/activitytracker/security/LoginAuthenticationBugTest.java`

The test includes:
1. **JUnit Tests**: Simple tests that verify login and logout endpoints
2. **Property-Based Tests**: jqwik property tests that generate various authentication requests

**Test Status:** READY TO RUN
- Test will FAIL on unfixed code (confirming bug exists)
- Test will PASS on fixed code (confirming bug is resolved)

## Next Steps

1. Implement the fix based on the root cause hypothesis:
   - Remove @Component annotation from RateLimitingFilter
   - Register RateLimitingFilter in Spring Security filter chain
   - Add endpoint exclusions for /api/v1/auth/**

2. Run the bug condition exploration test on fixed code:
   - Test should PASS (status 200, response contains tokens)
   - This will validate the fix works correctly

3. Run preservation tests to ensure existing security behavior is unchanged
